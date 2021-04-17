package com.koenv.gpjson.functions;

import com.koenv.gpjson.GPJSONContext;
import com.koenv.gpjson.jsonpath.JSONPathException;
import com.koenv.gpjson.jsonpath.JSONPathLexer;
import com.koenv.gpjson.jsonpath.JSONPathParser;
import com.koenv.gpjson.jsonpath.ReadableIRByteBuffer;
import com.koenv.gpjson.sequential.Sequential;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class QuerySequentialFunction extends Function {
    private final GPJSONContext context;

    public QuerySequentialFunction(GPJSONContext context) {
        super("querySequential");
        this.context = context;
    }

    @Override
    @CompilerDirectives.TruffleBoundary
    public Object call(Object[] arguments) throws UnsupportedTypeException, ArityException {
        context.getCudaRuntime().timings.start("querySequential");

        checkArgumentLength(arguments, 2);
        String filename = expectString(arguments[0], "expected filename");
        String query = expectString(arguments[1], "expected query");

        byte[] file;
        try {
            file = Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ReadableIRByteBuffer compiledQuery = new JSONPathParser(new JSONPathLexer(query)).compile().toReadable();

        long[] newlineIndex = Sequential.createNewlineIndex(file);
        long[] stringIndex = Sequential.createStringIndex(file);
        long[] leveledBitmapsIndex = Sequential.createLeveledBitmapsIndex(file, stringIndex);
        long[] result = Sequential.findValue(file, newlineIndex, stringIndex, leveledBitmapsIndex, compiledQuery);

        // queryGPU
        context.getCudaRuntime().timings.end();

        return "";
    }
}
