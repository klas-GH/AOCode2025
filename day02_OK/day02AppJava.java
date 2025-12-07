import java.io.*;
import java.util.*;

public class day02AppJava {

    public static void main(String[] args) throws IOException {
        long[] result = run("input.txt");
        System.out.println("sum1 = " + result[0]);
        System.out.println("sum2 = " + result[1]);
    }

    public static long[] run(String filename) throws IOException {
        // Read input file
        String data;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }
            data = sb.toString();
        }

        // Split by comma, dash & convert to numbers
        String[] parts = data.split(",");
        List<long[]> arr = new ArrayList<>();
        for (String s : parts) {
            String[] lr = s.trim().split("-");
            arr.add(new long[]{Long.parseLong(lr[0]), Long.parseLong(lr[1])});
        }

        long sum1 = 0;
        long sum2 = 0;

        for (long[] range : arr) {
            long L = range[0], R = range[1];
            sum1 += test1(L, R);
            sum2 += test2(L, R);
        }

        return new long[]{sum1, sum2};
    }

    // test1: mirrored "double numbers" of even length
    private static long test1(long l, long r) {
        long total = 0;
        int lenL = String.valueOf(l).length();
        int lenR = String.valueOf(r).length();

        int startLen = (lenL % 2 == 0) ? lenL : lenL + 1;

        for (int len = startLen; len <= lenR; len += 2) {
            int halfLen = len / 2;
            long start = (long) Math.pow(10, halfLen - 1);
            long end = (long) Math.pow(10, halfLen) - 1;
            long mul = (long) Math.pow(10, halfLen);

            for (long half = start; half <= end; half++) {
                long candidate = half * mul + half;
                if (candidate > r) break;
                if (candidate >= l) total += candidate;
            }
        }
        return total;
    }

    // test2: generate repeated-pattern numbers only, deduplicate
    private static long test2(long l, long r) {
        int lenL = String.valueOf(l).length();
        int lenR = String.valueOf(r).length();
        Set<Long> seen = new HashSet<>();

        // precompute powers of 10
        long[] pow10 = new long[lenR + 1];
        pow10[0] = 1;
        for (int i = 1; i <= lenR; i++) pow10[i] = pow10[i - 1] * 10;

        Map<String, Long> factorCache = new HashMap<>();

        java.util.function.BiFunction<Integer, Integer, Long> repFactor = (d, k) -> {
            String key = d + "," + k;
            if (factorCache.containsKey(key)) return factorCache.get(key);
            long pow_d = pow10[d];
            long pow_dk = pow10[d * k];
            long factor = (pow_dk - 1) / (pow_d - 1);
            factorCache.put(key, factor);
            return factor;
        };

        for (int n = lenL; n <= lenR; n++) {
            int half = n / 2;
            for (int d = 1; d <= half; d++) {
                if (n % d != 0) continue;
                int k = n / d;

                long startBase = pow10[d - 1];
                long endBase = pow10[d] - 1;
                long factor = repFactor.apply(d, k);

                if (endBase * factor < l) continue;
                if (startBase * factor > r) continue;

                long baseMin = Math.max(startBase, (l + factor - 1) / factor);
                long baseMax = Math.min(endBase, r / factor);

                if (baseMin > baseMax) continue;

                for (long base = baseMin; base <= baseMax; base++) {
                    long candidate = base * factor;
                    seen.add(candidate);
                }
            }
        }

        long total = 0;
        for (long v : seen) total += v;
        return total;
    }
}
