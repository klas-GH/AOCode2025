#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <map>
#include <tuple>
#include <algorithm>

using namespace std;

// Build graph from input lines
map<string, vector<string>> buildGraph(const vector<string>& edges) {
    map<string, vector<string>> graph;
    for (const auto &e : edges) {
        size_t pos = e.find(':');
        string node = e.substr(0, pos);
        string rest = e.substr(pos + 1);
        stringstream ss(rest);
        string neighbor;
        while (ss >> neighbor) {
            graph[node].push_back(neighbor);
        }
    }
    return graph;
}

// DFS for test1: count all paths from start to end
void dfs(const map<string, vector<string>>& graph, const string& node,
         const string& end, long long &count) {
    if (node == end) {
        count++;
        return;
    }
    auto it = graph.find(node);
    if (it == graph.end()) return;
    for (const auto &neighbor : it->second) {
        dfs(graph, neighbor, end, count);
    }
}

// DFS with memoization for test2
long long dfs2(const map<string, vector<string>>& graph, const string& node,
               const string& end, const string& cond1, const string& cond2,
               bool has1, bool has2,
               map<tuple<string,bool,bool>, long long>& memo) {
    auto key = make_tuple(node, has1, has2);
    if (memo.count(key)) return memo[key];

    if (node == cond1) has1 = true;
    if (node == cond2) has2 = true;

    if (node == end) {
        long long result = (has1 && has2) ? 1LL : 0LL;
        memo[key] = result;
        return result;
    }

    long long total = 0;
    auto it = graph.find(node);
    if (it != graph.end()) {
        for (const auto &nxt : it->second) {
            total += dfs2(graph, nxt, end, cond1, cond2, has1, has2, memo);
        }
    }

    memo[key] = total;
    return total;
}

int main() {
    // Read input file
    ifstream fin("input.txt");
    vector<string> data;
    string line;
    while (getline(fin, line)) {
        if (!line.empty()) {
            // trim trailing spaces
            line.erase(line.find_last_not_of(" \r\n") + 1);
            data.push_back(line);
        }
    }

    auto graph = buildGraph(data);

    // Test1: count all paths from "you" to "out"
    string start = "you";
    string end = "out";
    long long count = 0;
    dfs(graph, start, end, count);
    long long ans1 = count;

    // Test2: count paths from "svr" to "out" that pass through both cond1 and cond2
    string start2 = "svr";
    string cond1 = "dac";
    string cond2 = "fft";
    map<tuple<string,bool,bool>, long long> memo;
    long long ans2 = dfs2(graph, start2, end, cond1, cond2, false, false, memo);

    cout << "[" << ans1 << ", " << ans2 << "]" << endl;
    return 0;
}
