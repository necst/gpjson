package com.koenv.gpjson.stages;

import com.koenv.gpjson.KernelTest;
import com.koenv.gpjson.debug.FormatUtils;
import com.koenv.gpjson.debug.GPUUtils;
import com.koenv.gpjson.gpu.ManagedGPUPointer;
import com.koenv.gpjson.gpu.Type;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StructuralIndexTest extends KernelTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "simple",
    })
    public void create(String name) throws IOException {
        try (
                ManagedGPUPointer fileMemory = readFileToGPU("stages/structural_index/" + name + ".json");
                ManagedGPUPointer stringIndexMemory = cudaRuntime.allocateUnmanagedMemory((fileMemory.size() + 64 - 1) / 64, Type.SINT64);
        ) {
            stringIndexMemory.loadFrom(createStringIndex(name));

            StructuralIndex structuralIndex = new StructuralIndex(cudaRuntime, fileMemory, stringIndexMemory);
            try (ManagedGPUPointer indexMemory = structuralIndex.create()) {
                byte[] structural = GPUUtils.readBytes(cudaRuntime, indexMemory);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "simple",
    })
    public void structuralIndexSteps(String name) throws IOException {
        try (
                ManagedGPUPointer fileMemory = readFileToGPU("stages/structural_index/" + name + ".json");
                ManagedGPUPointer stringIndexMemory = cudaRuntime.allocateUnmanagedMemory((fileMemory.size() + 64 - 1) / 64, Type.SINT64);
                ManagedGPUPointer structuralIndexMemory = cudaRuntime.allocateUnmanagedMemory(fileMemory.size(), Type.CHAR);
        ) {
            stringIndexMemory.loadFrom(createStringIndex(name));

            StructuralIndex structuralIndex = new StructuralIndex(cudaRuntime, fileMemory, stringIndexMemory);

            FormatUtils.formatFileWithLongIndex(cudaRuntime, fileMemory, stringIndexMemory);
            structuralIndex.createStructuralIndex(structuralIndexMemory);

            FormatUtils.formatFileWithByteIndex(cudaRuntime, fileMemory, structuralIndexMemory);
        }
    }

    private ByteBuffer createStringIndex(String name) throws IOException {
        byte[] bytes = readFile("stages/structural_index/" + name + ".json");

        long[] index = new long[(bytes.length + 64 - 1) / 64];
        byte escaped = 0;
        boolean inString = false;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == '"' && escaped != 1) {
                inString = !inString;
            }

            if (inString) {
                index[i / 64] = index[i / 64] | (1L << (i % 64));
            }

            if (bytes[i] == '\\') {
                escaped = (byte) (escaped ^ 1);
            } else {
                escaped = 0;
            }
        }

        System.out.println(Long.toHexString(index[0]));

        ByteBuffer buffer = ByteBuffer.allocateDirect(index.length * 8).order(ByteOrder.LITTLE_ENDIAN);
        for (long l : index) {
            buffer.putLong(l);
        }

        return buffer;
    }
}
