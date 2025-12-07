#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <set>
#include <algorithm>

using namespace std;

pair<long long, long long> run(const vector<string>& grid) {
    int m = grid.size();
    int n = grid[0].size();
    int sourceIdx = grid[0].find('S');

    long long sumEncounters = 0;
    long long sumTimelines = 0;

    // ---------- Test1: encounters ----------
    {
        set<int> beams;
        beams.insert(sourceIdx);

        for (int r = 1; r < m; r++) {
            set<int> newBeams;
            for (int c : beams) {
                if (grid[r][c] == '^') {
                    sumEncounters++;
                    if (c - 1 >= 0) newBeams.insert(c - 1);
                    if (c + 1 < n) newBeams.insert(c + 1);
                } else {
                    newBeams.insert(c);
                }
            }
            beams = newBeams;
        }
    }

    // ---------- Test2: timelines ----------
    {
        vector<long long> paths(n, 0);
        paths[sourceIdx] = 1;
        set<int> beams;
        beams.insert(sourceIdx);

        for (int r = 1; r < m; r++) {
            set<int> newBeams;
            vector<long long> nextPaths = paths;

            for (int c : beams) {
                if (grid[r][c] == '^') {
                    if (c - 1 >= 0) {
                        nextPaths[c - 1] += paths[c];
                        newBeams.insert(c - 1);
                    }
                    if (c + 1 < n) {
                        nextPaths[c + 1] += paths[c];
                        newBeams.insert(c + 1);
                    }
                    nextPaths[c] -= paths[c]; // remove from current column
                } else {
                    newBeams.insert(c);
                }
            }
            paths = nextPaths;
            beams = newBeams;
        }

        for (long long val : paths) {
            sumTimelines += val;
        }
    }

    return {sumEncounters, sumTimelines};
}

int main() {
    ifstream infile("input.txt");
    vector<string> grid;
    string line;
    while (getline(infile, line)) {
        if (!line.empty()) {
            grid.push_back(line);
        }
    }

    auto result = run(grid);
    cout << "Encounters = " << result.first << "\n";
    cout << "Timelines  = " << result.second << "\n";

    return 0;
}
