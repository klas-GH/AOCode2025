import java.io.*;
import java.util.*;

public class day06AppJava {

    public static void main(String[] args) throws IOException {
        Result res = run("input.txt");
        System.out.println("sum1 = " + res.sum1);
        System.out.println("sum2 = " + res.sum2);
    }

    static class Result {
        long sum1;
        long sum2;
        Result(long a, long b) { sum1 = a; sum2 = b; }
    }

    public static Result run(String filename) throws IOException {
        // Read all non-empty lines (keep raw for test2)
        List<String> rowsStr = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.trim().isEmpty()) continue;
                rowsStr.add(line);
            }
        }
        if (rowsStr.isEmpty()) return new Result(0L, 0L);

        // Last line is operators (by character positions)
        String operLine = rowsStr.remove(rowsStr.size() - 1);

        // arrOper: (index, operator char) for non-space characters
        List<int[]> arrOper = new ArrayList<>();
        for (int i = 0; i < operLine.length(); i++) {
            char ch = operLine.charAt(i);
            if (ch != ' ') arrOper.add(new int[]{i, ch});
        }

        // For test1: tokenize numeric rows (split by whitespace)
        List<List<Long>> arrNums = new ArrayList<>();
        for (String r : rowsStr) {
            String[] parts = r.trim().split("\\s+");
            List<Long> row = new ArrayList<>(parts.length);
            for (String p : parts) row.add(Long.parseLong(p));
            arrNums.add(row);
        }

        // Validate dimensions for test1
        if (arrNums.isEmpty() || arrNums.get(0).isEmpty()
                || arrOper.size() < arrNums.get(0).size()) {
            return new Result(0L, 0L);
        }

        long sum1 = 0L;
        long sum2 = 0L;

        // ---------- test1 (column-wise numbers with operator) ----------
        {
            int rows = arrNums.size();
            int cols = arrNums.get(0).size();
            for (int j = 0; j < cols; j++) {
                long colTotal = arrNums.get(0).get(j);
                for (int i = 1; i < rows; i++) {
                    char op = (char) arrOper.get(j)[1];
                    if (op == '+') {
                        colTotal += arrNums.get(i).get(j);
                    } else {
                        colTotal *= arrNums.get(i).get(j);
                    }
                }
                sum1 += colTotal;
            }
        }

        // ---------- test2 (character-wise concatenation like JS, digits-only) ----------
        {
            if (!rowsStr.isEmpty()) {
                int numRows = rowsStr.size();
                int numColsChars = rowsStr.get(0).length();

                for (int z = 0; z < arrOper.size(); z++) {
                    int idx = arrOper.get(z)[0];
                    char op = (char) arrOper.get(z)[1];

                    int lastpos = (z != arrOper.size() - 1)
                            ? (arrOper.get(z + 1)[0] - 2)
                            : (numColsChars - 1);

                    long colTotal = (op == '+') ? 0L : 1L;

                    for (int pos = lastpos; pos >= idx; pos--) {
                        if (pos < 0 || pos >= numColsChars) continue;

                        StringBuilder sb = new StringBuilder(numRows);
                        for (int i = 0; i < numRows; i++) {
                            String row = rowsStr.get(i);
                            if (pos < row.length()) {
                                char ch = row.charAt(pos);
                                // Append only digits; skip spaces or other characters
                                if (ch >= '0' && ch <= '9') {
                                    sb.append(ch);
                                }
                            }
                        }

                        if (sb.length() == 0) continue; // nothing to parse

                        long val = Long.parseLong(sb.toString());

                        if (op == '+') {
                            colTotal += val;
                        } else {
                            colTotal *= val;
                        }
                    }

                    sum2 += colTotal;
                }
            }
        }

        return new Result(sum1, sum2);
    }
}
