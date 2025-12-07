import java.io.*;
import java.util.*;

public class day03AppJava {

    public static void main(String[] args) throws IOException {
        long[] result = run("input.txt");
        System.out.println("sum1 = " + result[0]);
        System.out.println("sum2 = " + result[1]);
    }

    public static long[] run(String filename) throws IOException {
        // Read all non-empty lines
        List<String> arr = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for (String line; (line = br.readLine()) != null; ) {
                line = line.trim();
                if (!line.isEmpty()) arr.add(line);
            }
        }

        long sum1 = 0;
        long sum2 = 0;

        for (String s : arr) {
            sum1 += test1(s);
            sum2 += test2(s);
        }

        return new long[]{sum1, sum2};
    }

    // test1: keep largest possible 2-digit number
    private static long test1(String s) {
        int k = 2;
        int toRemove = s.length() - k;
        Deque<Character> stack = new ArrayDeque<>();

        for (char digit : s.toCharArray()) {
            while (!stack.isEmpty() && toRemove > 0 && stack.peekLast() < digit) {
                stack.pollLast();
                toRemove--;
            }
            stack.addLast(digit);
        }

        // ensure exactly k digits
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (char c : stack) {
            if (count == k) break;
            sb.append(c);
            count++;
        }
        return Long.parseLong(sb.toString());
    }

    // test2: keep largest possible 12-digit number
    private static long test2(String s) {
        int k = 12;
        int toRemove = s.length() - k;
        Deque<Character> stack = new ArrayDeque<>();

        for (char digit : s.toCharArray()) {
            while (!stack.isEmpty() && toRemove > 0 && stack.peekLast() < digit) {
                stack.pollLast();
                toRemove--;
            }
            stack.addLast(digit);
        }

        // ensure exactly k digits
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (char c : stack) {
            if (count == k) break;
            sb.append(c);
            count++;
        }
        return Long.parseLong(sb.toString());
    }
}
