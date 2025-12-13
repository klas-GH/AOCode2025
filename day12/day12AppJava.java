import java.io.*;
import java.util.*;
import java.util.stream.*;

class Grid {
    String data;
    int width;
    int height;

    Grid() {
        this.data = "";
        this.width = 0;
        this.height = 0;
    }

    Grid(String data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    char get(int x, int y) {
        return data.charAt(y * width + x);
    }

    char get(int idx) {
        return data.charAt(idx);
    }

    void set(int idx, char c) {
        StringBuilder sb = new StringBuilder(data);
        sb.setCharAt(idx, c);
        data = sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Grid)) return false;
        Grid other = (Grid) o;
        return this.data.equals(other.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}

class Region {
    int width, height;
    List<Integer> quantity = new ArrayList<>();
}

class Situation {
    List<Grid> shapes = new ArrayList<>();
    List<Region> regions = new ArrayList<>();
}

public class day12AppJava {

    static Situation loadInput(String file) throws IOException {
        Situation ret = new Situation();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        boolean parseShapes = true;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) continue;
            if (line.contains("x")) {
                parseShapes = false;
            }
            if (parseShapes) {
                if (line.endsWith(":")) {
                    ret.shapes.add(new Grid());
                    continue;
                }
                Grid g = ret.shapes.get(ret.shapes.size() - 1);
                g.data += line;
                g.width = line.length();
                g.height++;
            } else {
                Region region = new Region();
                ret.regions.add(region);
                String dims = line.substring(0, line.indexOf(':'));
                String data = line.substring(line.indexOf(':') + 2);

                region.width = Integer.parseInt(dims.substring(0, dims.indexOf('x')));
                region.height = Integer.parseInt(dims.substring(dims.indexOf('x') + 1));

                Scanner sc = new Scanner(data);
                while (sc.hasNextInt()) {
                    region.quantity.add(sc.nextInt());
                }
            }
        }
        return ret;
    }

    static Grid transform(Grid in, int[] map) {
        StringBuilder sb = new StringBuilder(in.data);
        for (int i = 0; i < 9; i++) {
            sb.setCharAt(i, in.data.charAt(map[i]));
        }
        return new Grid(sb.toString(), in.width, in.height);
    }

    static boolean place(List<List<Grid>> shapesList, Grid grid, int xSteps, int ySteps, int depth, int maxDepth) {
        List<Integer> modified = new ArrayList<>(9);
        List<Grid> shapes = shapesList.get(depth);

        for (int y = 0; y < ySteps; y++) {
            for (int x = 0; x < xSteps; x++) {
                for (Grid shape : shapes) {
                    for (int idx : modified) grid.set(idx, '.');
                    modified.clear();

                    boolean valid = true;
                    for (int py = 0; py < 3 && valid; py++) {
                        for (int px = 0; px < 3; px++) {
                            if (shape.get(px, py) == '#') {
                                int gx = x + px, gy = y + py;
                                int idx = gy * grid.width + gx;
                                if (grid.get(idx) == '#') {
                                    valid = false;
                                    break;
                                }
                                modified.add(idx);
                                grid.set(idx, '#');
                            }
                        }
                    }

                    if (!valid) continue;

                    if (depth + 1 == maxDepth) return true;
                    if (place(shapesList, grid, xSteps, ySteps, depth + 1, maxDepth)) return true;
                }
            }
        }
        return false;
    }
    
    static long part1(Situation situation) {
        List<Integer> shapeAreas = situation.shapes.stream()
                .map(s -> (int) s.data.chars().filter(c -> c == '#').count())
                .collect(Collectors.toList());

        long sum = 0;
        for (Region region : situation.regions) {
            int regionSpaces = region.width * region.height;
            int spacesNeeded = 0;
            for (int i = 0; i < region.quantity.size(); i++) {
                spacesNeeded += shapeAreas.get(i) * region.quantity.get(i);
            }
            if (spacesNeeded <= regionSpaces) sum++;
        }
        return sum;
    }

    public static void main(String[] args) throws IOException {
        Situation actualValues = loadInput("input.txt");
        System.out.println("part1: " + part1(actualValues));
    }
}
