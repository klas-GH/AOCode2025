import java.io.*;
import java.util.*;

public class day08AppJava {

    static class Edge {
        int i, j;
        long d;
        Edge(int i, int j, long d) {
            this.i = i; this.j = j; this.d = d;
        }
    }

    static class UnionFind {
        int[] parent;
        UnionFind(int n) {
            parent = new int[n];
            for (int i=0; i<n; i++) parent[i] = i;
        }
        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }
        boolean union(int a, int b) {
            int pa = find(a), pb = find(b);
            if (pa == pb) return false;
            parent[pa] = pb;
            return true;
        }
    }

    public static long dist(int[] x, int[] y) {
        long dx = y[0]-x[0];
        long dy = y[1]-x[1];
        long dz = y[2]-x[2];
        return dx*dx + dy*dy + dz*dz;
    }

    public static void main(String[] args) throws Exception {
        List<int[]> arr = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                arr.add(new int[]{Integer.parseInt(parts[0]),
                                  Integer.parseInt(parts[1]),
                                  Integer.parseInt(parts[2])});
            }
        }
        int n = arr.size();

        // Build all edges
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingLong(e -> e.d));
        for (int i=0; i<n; i++) {
            for (int j=i+1; j<n; j++) {
                pq.add(new Edge(i, j, dist(arr.get(i), arr.get(j))));
            }
        }

        // Answer 1: pop 1000 edges, build graph, compute product of top 3 sizes
        List<Set<Integer>> graph = new ArrayList<>();
        for (int i=0; i<n; i++) graph.add(new HashSet<>());
        for (int k=0; k<1000 && !pq.isEmpty(); k++) {
            Edge e = pq.poll();
            graph.get(e.i).add(e.j);
            graph.get(e.j).add(e.i);
        }

        boolean[] visited = new boolean[n];
        List<Integer> sizes = new ArrayList<>();
        for (int i=0; i<n; i++) {
            if (!visited[i]) {
                int size = dfs(i, graph, visited);
                sizes.add(size);
            }
        }
        sizes.sort(Collections.reverseOrder());
        long sum1 = sizes.get(0) * sizes.get(1) * sizes.get(2);

        // Answer 2: continue popping edges until all connected
        UnionFind uf = new UnionFind(n);
        // union the first 1000 edges already used
        for (int i=0; i<n; i++) {
            for (int j : graph.get(i)) uf.union(i,j);
        }

        int groupsCount = countGroups(uf, n);
        Edge lastEdge = null;
        while (groupsCount > 1 && !pq.isEmpty()) {
            Edge e = pq.poll();
            if (uf.union(e.i, e.j)) {
                groupsCount = countGroups(uf, n);
                lastEdge = e;
            }
        }

        if (lastEdge == null) throw new RuntimeException("No edge found to connect all groups");
        long sum2 = (long)arr.get(lastEdge.i)[0] * arr.get(lastEdge.j)[0];

        System.out.println("Answer1: " + sum1);
        System.out.println("Answer2: " + sum2);
    }

    static int dfs(int start, List<Set<Integer>> graph, boolean[] visited) {
        int count = 0;
        Stack<Integer> stack = new Stack<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            int node = stack.pop();
            if (visited[node]) continue;
            visited[node] = true;
            count++;
            for (int nei : graph.get(node)) {
                if (!visited[nei]) stack.push(nei);
            }
        }
        return count;
    }

    static int countGroups(UnionFind uf, int n) {
        Set<Integer> roots = new HashSet<>();
        for (int i=0; i<n; i++) roots.add(uf.find(i));
        return roots.size();
    }
}
