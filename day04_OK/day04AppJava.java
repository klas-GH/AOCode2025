import java.io.*;
import java.util.*;

public class day04AppJava {

    static final int[][] DIR = {
        {1, 0}, {-1, 0}, {1, 1}, {-1, 1},
        {0, 1}, {0, -1}, {-1, -1}, {1, -1}
    };

    static int sum1 = 0;
    static int sum2 = 0;
    static List<int[]> queue = new ArrayList<>();
    static char[][] arr;

    public static void main(String[] args) throws IOException {
        long[] result = run("input.txt");
        System.out.println("sum1 = " + result[0]);
        System.out.println("sum2 = " + result[1]);
    }

    public static long[] run(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for (String line; (line = br.readLine()) != null; ) {
                line = line.trim();
                if (!line.isEmpty()) lines.add(line);
            }
        }

        int rows = lines.size();
        int cols = lines.get(0).length();
        arr = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            arr[i] = lines.get(i).toCharArray();
        }

        test1();
        test2();

        return new long[]{sum1, sum2};
    }

    static boolean isValid(int x, int y) {
        if (x < 0 || x >= arr.length || y < 0 || y >= arr[0].length || arr[x][y] != '@') {
            return false;
        }
        int count = 0;
        for (int[] d : DIR) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx < 0 || nx >= arr.length || ny < 0 || ny >= arr[0].length || arr[nx][ny] != '@') {
                continue;
            }
            count++;
        }
        return count < 4;
    }

    static void test1() {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                if (arr[i][j] != '@') continue;
                if (isValid(i, j)) {
                    sum1++;
                    queue.add(new int[]{i, j});
                }
            }
        }
    }

    static void test2() {
        Set<String> visited = new HashSet<>();
        int idx = 0;

        while (idx < queue.size()) {
            int[] pos = queue.get(idx++);
            int i = pos[0], j = pos[1];

            String key = i + "#" + j;
            if (visited.contains(key)) continue;
            visited.add(key);

            if (arr[i][j] == '@' && isValid(i, j)) {
                arr[i][j] = '.';
                sum2++;

                for (int[] d : DIR) {
                    int nx = i + d[0], ny = j + d[1];
                    if (isValid(nx, ny)) {
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
    }
}
