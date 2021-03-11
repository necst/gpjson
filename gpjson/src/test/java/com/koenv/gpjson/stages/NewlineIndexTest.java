package com.koenv.gpjson.stages;

import com.koenv.gpjson.KernelTest;
import com.koenv.gpjson.debug.GPUUtils;
import com.koenv.gpjson.gpu.ManagedGPUPointer;
import com.koenv.gpjson.gpu.Type;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewlineIndexTest extends KernelTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "even_positions",
            "odd_positions",
            "random_data",
            "aligned_random_data"
    })
    public void create(String name) throws IOException {
        try (
                ManagedGPUPointer fileMemory = readFileToGPU("stages/newline_index/" + name + ".txt")
        ) {
            NewlineIndex newlineIndex = new NewlineIndex(cudaRuntime, fileMemory);

            long[] index = newlineIndex.create();
            long[] expectedIndex = createExpectedIndex(name);

            assertArrayEquals(expectedIndex, index);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "even_positions",
            "odd_positions",
            "random_data",
            "aligned_random_data"
    })
    public void newlineIndex(String name) throws IOException {
        try (
                ManagedGPUPointer fileMemory = readFileToGPU("stages/newline_index/" + name + ".txt");
                ManagedGPUPointer countMemory = cudaRuntime.allocateUnmanagedMemory(NewlineIndex.COUNT_INDEX_SIZE, Type.SINT32)
        ) {
            NewlineIndex newlineIndex = new NewlineIndex(cudaRuntime, fileMemory);

            newlineIndex.countNewlines(countMemory);

            int[] counts = GPUUtils.readInts(cudaRuntime, countMemory);
            int[] expectedCounts = createExpectedCounts(name);

            assertArrayEquals(expectedCounts, counts);

            int sum = newlineIndex.createOffsetsIndex(countMemory);

            assertEquals(IntStream.of(expectedCounts).sum(), sum);

            int[] offsets = GPUUtils.readInts(cudaRuntime, countMemory);
            int[] expectedOffsets = createExpectedOffsets(counts);

            assertArrayEquals(expectedOffsets, offsets);

            try (ManagedGPUPointer indexMemory = cudaRuntime.allocateUnmanagedMemory(sum, Type.SINT64)) {
                long[] index = newlineIndex.createIndex(sum, countMemory, indexMemory);
                long[] expectedIndex = createExpectedIndex(name);

                assertArrayEquals(expectedIndex, index);
            }
        }
    }

    private int[] createExpectedCounts(String name) throws IOException {
        byte[] bytes = readFile("stages/newline_index/" + name + ".txt");

        int charactersPerThread = (bytes.length + StringIndex.CARRY_INDEX_SIZE - 1) / StringIndex.CARRY_INDEX_SIZE;

        int[] index = new int[StringIndex.CARRY_INDEX_SIZE];

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != '\n') {
                continue;
            }

            index[i / charactersPerThread]++;
        }

        return index;
    }

    private int[] createExpectedOffsets(int[] counts) {
        int[] index = new int[counts.length];

        for (int i = 1; i < counts.length; i++) {
            index[i] = index[i - 1] + counts[i - 1];
        }

        return index;
    }

    private long[] createExpectedIndex(String name) throws IOException {
        byte[] bytes = readFile("stages/newline_index/" + name + ".txt");

        List<Long> index = new ArrayList<>();

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != '\n') {
                continue;
            }

            index.add((long) i);
        }

        return index.stream().mapToLong(value -> value).toArray();
    }
}
