package com.koenv.gpjson.functions;

import com.koenv.gpjson.GPJSONContext;
import com.koenv.gpjson.GPJSONException;
import com.koenv.gpjson.gpu.*;
import com.koenv.gpjson.jsonpath.JSONPathLexer;
import com.koenv.gpjson.jsonpath.JSONPathParser;
import com.koenv.gpjson.jsonpath.JSONPathResult;
import com.koenv.gpjson.kernel.GPJSONKernel;
import com.koenv.gpjson.stages.CombinedIndex;
import com.koenv.gpjson.stages.CombinedIndexResult;
import com.koenv.gpjson.stages.LeveledBitmapsIndex;
import com.koenv.gpjson.util.FormatUtil;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QueryGPUFunction extends Function {
    private final GPJSONContext context;

    public QueryGPUFunction(GPJSONContext context) {
        super("queryGpu");
        this.context = context;
    }

    @Override
    @CompilerDirectives.TruffleBoundary
    public Object call(Object[] arguments) throws UnsupportedTypeException, ArityException {
        long start = System.nanoTime();

        context.getCudaRuntime().timings.start("compile_kernels");
        for (GPJSONKernel kernel : GPJSONKernel.values()) {
            context.getCudaRuntime().getKernel(kernel);
        }
        context.getCudaRuntime().timings.end();

        long end = System.nanoTime();
        System.out.printf("Compiling kernels done in %dms%n", TimeUnit.NANOSECONDS.toMillis(end - start));

        context.getCudaRuntime().timings.start("queryGPU");

        checkArgumentLength(arguments, 2);
        String filename = expectString(arguments[0], "expected filename");
        String query = expectString(arguments[1], "expected query");

        Path file = Paths.get(filename);

        long size;
        try {
            size = Files.size(file);
        } catch (IOException e) {
            throw new GPJSONException("Failed to get size of file", e, AbstractTruffleException.UNLIMITED_STACK_TRACE, null);
        }

        JSONPathResult compiledQuery = new JSONPathParser(new JSONPathLexer(query)).compile();
        ByteBuffer compiledQueryBuffer = compiledQuery.getIr().toByteBuffer();

        StringBuilder returnValue = new StringBuilder();

        try (
                ManagedGPUPointer fileMemory = context.getCudaRuntime().allocateUnmanagedMemory(size);
                ManagedGPUPointer queryMemory = context.getCudaRuntime().allocateUnmanagedMemory(compiledQueryBuffer.capacity())
        ) {
            start = System.nanoTime();
            readFile(fileMemory, file, size);

            end = System.nanoTime();
            long duration = end - start;
            double durationSeconds = duration / (double) TimeUnit.SECONDS.toNanos(1);
            double speed = size / durationSeconds;

            System.out.printf("Reading file done in %dms, %s/second%n", TimeUnit.NANOSECONDS.toMillis(duration), FormatUtil.humanReadableByteCountSI((long) speed));

            queryMemory.loadFrom(compiledQueryBuffer);

            start = System.nanoTime();

            CombinedIndex combinedIndexCreator = new CombinedIndex(context.getCudaRuntime(), fileMemory);
            try (CombinedIndexResult combinedIndexResult = combinedIndexCreator.create()) {
                ManagedGPUPointer stringIndex = combinedIndexResult.stringIndex;
                ManagedGPUPointer newlineIndex = combinedIndexResult.newlineIndex;

                end = System.nanoTime();
                duration = end - start;
                durationSeconds = duration / (double) TimeUnit.SECONDS.toNanos(1);
                speed = size / durationSeconds;

                System.out.printf("Creating newline and string index done in %dms, %s/second%n", TimeUnit.NANOSECONDS.toMillis(duration), FormatUtil.humanReadableByteCountSI((long) speed));

                start = System.nanoTime();

                LeveledBitmapsIndex leveledBitmapsIndexCreator = new LeveledBitmapsIndex(context.getCudaRuntime(), fileMemory, stringIndex, (byte) compiledQuery.getMaxDepth());

                try (ManagedGPUPointer leveledBitmapIndex = leveledBitmapsIndexCreator.create()) {
                    end = System.nanoTime();
                    duration = end - start;
                    durationSeconds = duration / (double) TimeUnit.SECONDS.toNanos(1);
                    speed = size / durationSeconds;

                    System.out.printf("Creating leveled bitmaps index done in %dms, %s/second%n", TimeUnit.NANOSECONDS.toMillis(duration), FormatUtil.humanReadableByteCountSI((long) speed));

                    try (ManagedGPUPointer result = context.getCudaRuntime().allocateUnmanagedMemory(newlineIndex.numberOfElements(), Type.SINT64)) {
                        Kernel kernel = context.getCudaRuntime().getKernel(GPJSONKernel.FIND_VALUE);

                        List<UnsafeHelper.MemoryObject> kernelArguments = new ArrayList<>();
                        kernelArguments.add(UnsafeHelper.createPointerObject(fileMemory));
                        kernelArguments.add(UnsafeHelper.createInteger64Object(fileMemory.numberOfElements()));
                        kernelArguments.add(UnsafeHelper.createPointerObject(newlineIndex));
                        kernelArguments.add(UnsafeHelper.createInteger64Object(newlineIndex.numberOfElements()));
                        kernelArguments.add(UnsafeHelper.createPointerObject(stringIndex));
                        kernelArguments.add(UnsafeHelper.createPointerObject(leveledBitmapIndex));
                        kernelArguments.add(UnsafeHelper.createInteger64Object(leveledBitmapIndex.numberOfElements()));

                        long levelSize = (fileMemory.size() + 64 - 1) / 64;

                        kernelArguments.add(UnsafeHelper.createInteger64Object(levelSize));

                        kernelArguments.add(UnsafeHelper.createPointerObject(queryMemory));
                        kernelArguments.add(UnsafeHelper.createInteger32Object(compiledQueryBuffer.capacity()));

                        kernelArguments.add(UnsafeHelper.createPointerObject(result));

                        start = System.nanoTime();

                        kernel.execute(new Dim3(8), new Dim3(1024), 0, 0, kernelArguments);

                        end = System.nanoTime();
                        duration = end - start;
                        durationSeconds = duration / (double) TimeUnit.SECONDS.toNanos(1);
                        speed = size / durationSeconds;

                        System.out.printf("Finding values done in %dms, %s/second%n", TimeUnit.NANOSECONDS.toMillis(duration), FormatUtil.humanReadableByteCountSI((long) speed));

                        /*context.getCudaRuntime().timings.start("write_result");
                        byte[] values = GPUUtils.readBytes(fileMemory);

                        long[] returnValues = GPUUtils.readLongs(result);
                        for (long value : returnValues) {
                            returnValue.append(value);

                            if (value > -1) {
                                returnValue.append(": ");

                                for (int m = 0; m < 8; m++) {
                                    returnValue.append((char) values[(int) value + m]);
                                }
                            }

                            returnValue.append('\n');
                        }
                        context.getCudaRuntime().timings.end();*/
                    }
                }
            }
        }

        // queryGPU
        context.getCudaRuntime().timings.end();

        return returnValue.toString();
    }

    private void readFile(ManagedGPUPointer memory, Path file, long expectedSize) {
        context.getCudaRuntime().timings.start("readFile");

        try (FileChannel channel = FileChannel.open(file)) {
            if (channel.size() != expectedSize) {
                throw new GPJSONException("Size of file has changed while reading");
            }

            MappedByteBuffer mappedBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            mappedBuffer.load();

            UnsafeHelper.ByteArray byteArray = UnsafeHelper.createByteArray(mappedBuffer);

            context.getCudaRuntime().cudaMemcpy(memory.getPointer().getRawPointer(), byteArray.getAddress(), channel.size(), CUDAMemcpyKind.HOST_TO_DEVICE);
        } catch (IOException e) {
            throw new GPJSONException("Failed to open file", e, AbstractTruffleException.UNLIMITED_STACK_TRACE, null);
        } finally {
            context.getCudaRuntime().timings.end();
        }
    }
}
