#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <string>
#include <queue>
#include <stack>
#include <set>
#include <unordered_set>
#include <algorithm>
#include <numeric>

struct Edge {
    int i, j;
    long long d;
    Edge(int i, int j, long long d) : i(i), j(j), d(d) {}
};

// comparator for min-heap
struct Compare {
    bool operator()(const Edge& a, const Edge& b) {
        return a.d > b.d; // smallest distance first
    }
};

struct UnionFind {
    std::vector<int> parent;
    UnionFind(int n) {
        parent.resize(n);
        std::iota(parent.begin(), parent.end(), 0);
    }
    int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }
    bool unite(int a, int b) {
        int pa = find(a), pb = find(b);
        if (pa == pb) return false;
        parent[pa] = pb;
        return true;
    }
};

long long dist(const std::vector<int>& x, const std::vector<int>& y) {
    long long dx = y[0]-x[0];
    long long dy = y[1]-x[1];
    long long dz = y[2]-x[2];
    return dx*dx + dy*dy + dz*dz;
}

int dfs(int start, std::vector<std::vector<int>>& graph, std::vector<bool>& visited) {
    int count = 0;
    std::stack<int> st;
    st.push(start);
    while (!st.empty()) {
        int node = st.top(); st.pop();
        if (visited[node]) continue;
        visited[node] = true;
        count++;
        for (int nei : graph[node]) {
            if (!visited[nei]) st.push(nei);
        }
    }
    return count;
}

int countGroups(UnionFind& uf, int n) {
    std::unordered_set<int> roots;
    for (int i=0; i<n; i++) roots.insert(uf.find(i));
    return (int)roots.size();
}

int main() {
    std::ifstream infile("input.txt");
    std::string line;
    std::vector<std::vector<int>> arr;
    while (std::getline(infile, line)) {
        if (line.empty()) continue;
        std::stringstream ss(line);
        std::string part;
        std::vector<int> coords;
        while (std::getline(ss, part, ',')) {
            coords.push_back(std::stoi(part));
        }
        arr.push_back(coords);
    }
    int n = arr.size();

    // build all edges
    std::priority_queue<Edge, std::vector<Edge>, Compare> pq;
    for (int i=0; i<n; i++) {
        for (int j=i+1; j<n; j++) {
            pq.emplace(i, j, dist(arr[i], arr[j]));
        }
    }

    // Answer 1: pop 1000 edges, build graph, compute product of top 3 sizes
    std::vector<std::vector<int>> graph(n);
    for (int k=0; k<1000 && !pq.empty(); k++) {
        Edge e = pq.top(); pq.pop();
        graph[e.i].push_back(e.j);
        graph[e.j].push_back(e.i);
    }

    std::vector<bool> visited(n,false);
    std::vector<int> sizes;
    for (int i=0; i<n; i++) {
        if (!visited[i]) {
            int size = dfs(i, graph, visited);
            sizes.push_back(size);
        }
    }
    std::sort(sizes.rbegin(), sizes.rend());
    long long sum1 = 1LL * sizes[0] * sizes[1] * sizes[2];

    // Answer 2: continue popping edges until all connected
    UnionFind uf(n);
    // union the first 1000 edges already used
    for (int i=0; i<n; i++) {
        for (int j : graph[i]) uf.unite(i,j);
    }

    int groupsCount = countGroups(uf, n);
    Edge lastEdge(0,0,0);
    while (groupsCount > 1 && !pq.empty()) {
        Edge e = pq.top(); pq.pop();
        if (uf.unite(e.i, e.j)) {
            groupsCount = countGroups(uf, n);
            lastEdge = e;
        }
    }

    if (groupsCount > 1) {
        std::cerr << "No edge found to connect all groups\n";
        return 1;
    }

    long long sum2 = 1LL * arr[lastEdge.i][0] * arr[lastEdge.j][0];

    std::cout << "Answer1: " << sum1 << "\n";
    std::cout << "Answer2: " << sum2 << "\n";

    return 0;
}
