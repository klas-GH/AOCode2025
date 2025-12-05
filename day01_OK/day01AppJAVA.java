import java.io.*;
import java.util.*;

public class day01AppJAVA {

    // Read input file and return non-empty trimmed lines
    private static List<String> readInput(String filename) throws IOException {
        List<String> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    rows.add(line);
                }
            }
        }
        return rows;
    }

    // test1: count landings on position 0
    private static int test1(List<String> rows) {
        int pos = 50; // initial position
        int count = 0;
        for (String val : rows) {
            char dir = val.charAt(0);
            int nr = Math.abs(Integer.parseInt(val.substring(1)));

            if (dir == 'R') {
                pos = (pos + nr) % 100;
            } else {
                pos = (100 + pos - nr) % 100;
            }

            if (pos == 0) {
                count++;
            }
        }
        return count;
    }

    // test2: count crossings through position 0
    private static int test2(List<String> rows) {
        int count = 0;
        int pos = 50; // initial position

        for (String val : rows) {
            char dir = val.charAt(0);
            int nr = Math.abs(Integer.parseInt(val.substring(1)));

            int stepsToFirstZero;
            if (dir == 'R') {
                stepsToFirstZero = (pos == 0 ? 100 : 100 - pos);
                if (nr >= stepsToFirstZero) {
                    count += (nr - stepsToFirstZero) / 100 + 1;
                }
                pos = (pos + nr) % 100;
            } else {
                stepsToFirstZero = (pos == 0 ? 100 : pos);
                if (nr >= stepsToFirstZero) {
                    count += (nr - stepsToFirstZero) / 100 + 1;
                }
                pos = ((pos - nr) % 100 + 100) % 100; // safe modulo
            }
        }
        return count;
    }

    public static void main(String[] args) {
        try {
            List<String> rows = readInput("input.txt");
            int sum1 = test1(rows);
            int sum2 = test2(rows);

            System.out.println("[" + sum1 + ", " + sum2 + "]");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
