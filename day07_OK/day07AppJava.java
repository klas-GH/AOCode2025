import java.io.*;
import java.util.*;

public class day07AppJava {

    public static void main(String[] args) throws IOException {
        List<String> grid = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    grid.add(line);
                }
            }
        }

        long[] result = run(grid);
        System.out.println("Encounters = " + result[0]);
        System.out.println("Timelines  = " + result[1]);
    }

    public static long[] run(List<String> grid) {
        int m = grid.size();
        int n = grid.get(0).length();
        int sourceIdx = grid.get(0).indexOf('S');

        long sumEncounters = 0;
        long sumTimelines = 0;

        // ---------- Test1: encounters ----------
        {
            Set<Integer> beams = new HashSet<>();
            beams.add(sourceIdx);

            for (int r = 1; r < m; r++) {
                Set<Integer> newBeams = new HashSet<>();
                for (int c : beams) {
                    if (grid.get(r).charAt(c) == '^') {
                        sumEncounters++;
                        if (c - 1 >= 0) newBeams.add(c - 1);
                        if (c + 1 < n) newBeams.add(c + 1);
                    } else {
                        newBeams.add(c);
                    }
                }
                beams = newBeams;
            }
        }

        // ---------- Test2: timelines ----------
        {
            long[] paths = new long[n];
            paths[sourceIdx] = 1;
            Set<Integer> beams = new HashSet<>();
            beams.add(sourceIdx);

            for (int r = 1; r < m; r++) {
                Set<Integer> newBeams = new HashSet<>();
                long[] nextPaths = Arrays.copyOf(paths, n);

                for (int c : beams) {
                    if (grid.get(r).charAt(c) == '^') {
                        if (c - 1 >= 0) {
                            nextPaths[c - 1] += paths[c];
                            newBeams.add(c - 1);
                        }
                        if (c + 1 < n) {
                            nextPaths[c + 1] += paths[c];
                            newBeams.add(c + 1);
                        }
                        nextPaths[c] -= paths[c]; // remove from current column
                    } else {
                        newBeams.add(c);
                    }
                }
                paths = nextPaths;
                beams = newBeams;
            }

            for (long val : paths) {
                sumTimelines += val;
            }
        }

        return new long[]{sumEncounters, sumTimelines};
    }
}
