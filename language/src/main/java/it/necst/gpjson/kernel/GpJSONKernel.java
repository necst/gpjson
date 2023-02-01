package it.necst.gpjson.kernel;

public enum GpJSONKernel {
    COUNT_NEWLINES("count_newlines", "count_newlines(file: inout pointer char, n: sint64, result: inout pointer sint32)", "it/necst/gpjson/kernels/uncombined/count_newlines.cu"),
    CREATE_NEWLINE_INDEX("create_newline_index", "create_newline_index(file: inout pointer char, n: sint64, indices: inout pointer sint32, result: inout pointer sint64)", "it/necst/gpjson/kernels/uncombined/create_newline_index.cu"),
    CREATE_ESCAPE_CARRY_INDEX("create_escape_carry_index", "create_escape_carry_index(file: inout pointer char, n: sint64, escape_carry_index: inout pointer char)", "it/necst/gpjson/kernels/uncombined/create_escape_carry_index.cu"),
    CREATE_ESCAPE_INDEX("create_escape_index", "create_escape_index(file: inout pointer char, n: sint64, escape_carry_index: inout pointer char, escape_index: inout pointer sint64, escape_index_size: sint64)", "it/necst/gpjson/kernels/uncombined/create_escape_index.cu"),
    CREATE_QUOTE_INDEX("create_quote_index", "create_quote_index(file: inout pointer char, n: sint64, escape_index: inout pointer sint64, quote_index: inout pointer sint64, quote_carry_index: inout pointer char, quote_index_size: sint64)", "it/necst/gpjson/kernels/create_quote_index.cu"),
    CREATE_STRING_INDEX("create_string_index", "create_string_index(n: sint64, quote_index: inout pointer sint64, quote_counts: inout pointer char)", "it/necst/gpjson/kernels/create_string_index.cu"),
    CREATE_LEVELED_BITMAPS_CARRY_INDEX("create_leveled_bitmaps_carry_index", "create_leveled_bitmaps_carry_index(file: inout pointer char, n: sint64, string_index: inout pointer sint64, level_carry_index: inout pointer sint8)", "it/necst/gpjson/kernels/create_leveled_bitmaps_carry_index.cu"),
    CREATE_LEVELED_BITMAPS("create_leveled_bitmaps", "create_leveled_bitmaps(file: inout pointer char, n: sint64, string_index: inout pointer sint64, carry_index: inout pointer sint8, leveled_bitmaps_index: inout pointer sint64, leveled_bitmaps_index_size: sint64, level_size: sint64, num_levels: sint32)", "it/necst/gpjson/kernels/create_leveled_bitmaps.cu"),
    FIND_VALUE("find_value", "find_value(file: inout pointer char, n: sint64, new_line_index: in pointer sint64, new_line_index_size: sint64, string_index: in pointer sint64, leveled_bitmaps_index: in pointer sint64, leveled_bitmaps_index_size: sint64, level_size: sint64, query: in pointer char, result_size: sint32, result: out pointer sint64)", "it/necst/gpjson/kernels/find_value.cu"),
    CREATE_COMBINED_ESCAPE_CARRY_NEWLINE_COUNT_INDEX("create_combined_escape_carry_newline_count_index", "create_combined_escape_carry_newline_count_index(file: inout pointer char, n: sint64, escape_carry_index: inout pointer char, newline_count_index: inout pointer sint32)", "it/necst/gpjson/kernels/create_combined_escape_carry_newline_count_index.cu"),
    CREATE_COMBINED_ESCAPE_NEWLINE_INDEX("create_combined_escape_newline_index", "create_combined_escape_newline_index(file: inout pointer char, n: sint64, escape_carry_index: inout pointer char, newline_count_index: inout pointer sint32, escape_index: inout pointer sint64, escape_index_size: sint64, newline_index: inout pointer sint64)", "it/necst/gpjson/kernels/create_combined_escape_newline_index.cu"),
    INITIALIZE("initialize", "initialize(arr:out pointer sint64, n: sint64, value: sint64)", "it/necst/gpjson/kernels/initialize.cu"),
    SUM1("sum1", "sum1(arr: inout pointer sint32, n: sint32, sum: out pointer sint32)", "it/necst/gpjson/kernels/sum1.cu"),
    SUM2("sum2", "sum2(arr: in pointer sint32, n: sint32, stride: sint32, base: out pointer sint32)", "it/necst/gpjson/kernels/sum2.cu"),
    SUM3("sum3", "sum3(arr: in pointer sint32, n: sint32, base: in pointer sint32, newArr: out pointer sint32)", "it/necst/gpjson/kernels/sum3.cu"),
    XOR1("xor1", "xor1(arr: inout pointer char, n: sint32)", "it/necst/gpjson/kernels/xor1.cu"),
    XOR2("xor2", "xor2(arr: in pointer char, n: sint32, stride: sint32, base: out pointer char)", "it/necst/gpjson/kernels/xor2.cu"),
    XOR3("xor3", "xor3(arr: inout pointer char, n: sint32, base: in pointer char)", "it/necst/gpjson/kernels/xor3.cu");

    private final String name;
    private final String parameterSignature;
    private final String filename;

    GpJSONKernel(String name, String parameterSignature, String filename) {
        this.name = name;
        this.parameterSignature = parameterSignature;
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public String getParameterSignature() {
        return parameterSignature;
    }

    public String getFilename() {
        return filename;
    }
}

