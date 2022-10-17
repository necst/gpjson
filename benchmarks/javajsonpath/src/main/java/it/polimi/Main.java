package it.polimi;

public class Main {
    private final static int warmup_query = 5;
    private final static int repeat_query = 10;
    private final static String baseDir = "../../../datasets/";

    interface I {
        public int query(String dataset);
    }

    public static void main(String[] args) {
        Execute exe = new Execute();
        String test = "test_large_record.json";
        String twitter_small = "twitter_small_records.json";
        String twitter_smaller = "twitter_small_records_smaller.json";
        String bestbuy_small = "bestbuy_small_records.json";
        String walmart_small = "walmart_small_records.json";

        exe.execute(warmup_query, repeat_query, twitter_small, baseDir, Query::TT1, "TT1");
        exe.execute(warmup_query, repeat_query, twitter_small, baseDir, Query::TT2, "TT2");
        exe.execute(warmup_query, repeat_query, twitter_small, baseDir, Query::TT3, "TT3");
        exe.execute(warmup_query, repeat_query, twitter_small, baseDir, Query::TT4, "TT4");

        exe.execute(warmup_query, repeat_query, walmart_small, baseDir, Query::WM, "WM");

        exe.execute(warmup_query, repeat_query, bestbuy_small, baseDir, Query::BB, "BB");
    }
}