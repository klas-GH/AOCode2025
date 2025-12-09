import java.io.*;
import java.util.*;

public class day09AppJava {

    public static void main(String[] args) throws IOException {
        long[] result = run("input.txt");
        System.out.println(Arrays.toString(result));
    }

    public static long[] run(String filename) throws IOException {
        // 1. Read input
        List<int[]> cells = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                cells.add(new int[]{x, y});
            }
        }

        int N = cells.size();

        // 2. Collect unique coordinates
        TreeSet<Integer> xSet = new TreeSet<>();
        TreeSet<Integer> ySet = new TreeSet<>();
        for (int[] c : cells) {
            xSet.add(c[0]);
            ySet.add(c[1]);
        }

        List<Integer> xList = new ArrayList<>(xSet);
        List<Integer> yList = new ArrayList<>(ySet);

        Map<Integer, Integer> xMap = new HashMap<>();
        Map<Integer, Integer> yMap = new HashMap<>();
        for (int i = 0; i < xList.size(); i++) xMap.put(xList.get(i), i);
        for (int i = 0; i < yList.size(); i++) yMap.put(yList.get(i), i);

        int W = xList.size();
        int H = yList.size();

        int[][] mappedCells = new int[N][2];
        for (int i = 0; i < N; i++) {
            int[] c = cells.get(i);
            mappedCells[i][0] = xMap.get(c[0]);
            mappedCells[i][1] = yMap.get(c[1]);
        }

        // 3. Blocked set
        Set<String> blocked = new HashSet<>();
        for (int i = 0; i < N; i++) {
            int x1 = mappedCells[i][0], y1 = mappedCells[i][1];
            int x2 = mappedCells[(i+1)%N][0], y2 = mappedCells[(i+1)%N][1];

            if (x1 == x2) {
                for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                    blocked.add(x1 + "," + y);
            } else if (y1 == y2) {
                for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
                    blocked.add(x + "," + y1);
            } else {
                throw new RuntimeException("Non-rectilinear polygon detected");
            }
        }

        // 4. Flood-fill outside
        int[][] DIRS = {{-1,0},{1,0},{0,-1},{0,1}};
        Set<String> bad = new HashSet<>();
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{-1,-1});

        while (!stack.isEmpty()) {
            int[] cur = stack.pop();
            int cx = cur[0], cy = cur[1];
            for (int[] d : DIRS) {
                int nx = cx + d[0], ny = cy + d[1];
                String key = nx + "," + ny;
                if (bad.contains(key) || blocked.contains(key)) continue;
                if (nx < -1 || nx > W || ny < -1 || ny > H) continue;
                bad.add(key);
                stack.push(new int[]{nx, ny});
            }
        }

        // 5. Rectangle checks
        long maxTotal = 0L;
        long maxInside = 0L;

        for (int i = 0; i < N; i++) {
            int x1 = cells.get(i)[0], y1 = cells.get(i)[1];
            int x1_ = mappedCells[i][0], y1_ = mappedCells[i][1];
            for (int j = 0; j < i; j++) {
                int x2 = cells.get(j)[0], y2 = cells.get(j)[1];
                int x2_ = mappedCells[j][0], y2_ = mappedCells[j][1];

                int minX = Math.min(x1, x2);
                int maxX = Math.max(x1, x2);
                int minY = Math.min(y1, y2);
                int maxY = Math.max(y1, y2);

                long area = (long)(maxX - minX + 1) * (maxY - minY + 1);
                maxTotal = Math.max(maxTotal, area);

                int minXi = Math.min(x1_, x2_), maxXi = Math.max(x1_, x2_);
                int minYi = Math.min(y1_, y2_), maxYi = Math.max(y1_, y2_);

                if (area > maxInside && checkInside(bad, minXi, minYi, maxXi, maxYi)) {
                    maxInside = area;
                }
            }
        }

        return new long[]{maxTotal, maxInside};
    }

    private static boolean checkInside(Set<String> bad, int x1, int y1, int x2, int y2) {
        for (int x = x1; x <= x2; x++)
            for (int y = y1; y <= y2; y++)
                if (bad.contains(x + "," + y)) return false;
        return true;
    }
}
