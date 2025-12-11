import java.io.*;
import java.util.*;

public class day11AppJava {

    public static void main(String[] args) throws IOException {
        System.out.println(run());
    }

    public static List<Long> run() throws IOException {
        // read file synchronously and split into rows
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    data.add(line);
                }
            }
        }

        // build graph
        Map<String, List<String>> graph = buildGraph(data);

        // test1: count all paths from "you" to "out"
        String start = "you";
        String end = "out";
        Counter counter = new Counter();
        dfs(graph, start, end, counter);
        long ans1 = counter.value;

        // test2: count paths from "svr" to "out" that pass through both cond1 and cond2
        String start2 = "svr";
        String cond1 = "dac";
        String cond2 = "fft";
        Map<String, Long> memo = new HashMap<>();
        long ans2 = dfs2(graph, start2, end, cond1, cond2, false, false, memo);

        return Arrays.asList(ans1, ans2);
    }

    // helper to build graph
    private static Map<String, List<String>> buildGraph(List<String> edges) {
        Map<String, List<String>> graph = new HashMap<>();
        for (String e : edges) {
            String[] parts = e.split("\\s*:\\s*");
            String node = parts[0];
            String[] neighbors = parts[1].split("\\s+");
            graph.put(node, Arrays.asList(neighbors));
        }
        return graph;
    }

    // simple counter class
    static class Counter {
        long value = 0;
    }

    // dfs for test1
    private static void dfs(Map<String, List<String>> graph, String node, String end, Counter counter) {
        if (node.equals(end)) {
            counter.value++;
            return;
        }
        for (String neighbor : graph.getOrDefault(node, Collections.emptyList())) {
            dfs(graph, neighbor, end, counter);
        }
    }

    // dfs with memoization for test2
    private static long dfs2(Map<String, List<String>> graph, String node, String end,
                             String cond1, String cond2,
                             boolean has1, boolean has2,
                             Map<String, Long> memo) {
        String key = node + "|" + has1 + "|" + has2;
        if (memo.containsKey(key)) return memo.get(key);

        if (node.equals(cond1)) has1 = true;
        if (node.equals(cond2)) has2 = true;

        if (node.equals(end)) {
            long result = (has1 && has2) ? 1L : 0L;
            memo.put(key, result);
            return result;
        }

        long total = 0;
        for (String nxt : graph.getOrDefault(node, Collections.emptyList())) {
            total += dfs2(graph, nxt, end, cond1, cond2, has1, has2, memo);
        }

        memo.put(key, total);
        return total;
    }
}
