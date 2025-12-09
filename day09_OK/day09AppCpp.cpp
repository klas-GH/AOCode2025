#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <set>
#include <map>
#include <algorithm>
#include <stdexcept>

using namespace std;

bool checkInside(const set<pair<int,int>> &bad, int x1, int y1, int x2, int y2) {
    for (int x = x1; x <= x2; x++)
        for (int y = y1; y <= y2; y++)
            if (bad.count({x, y})) return false;
    return true;
}

pair<long long, long long> run(const string &filename) {
    ifstream fin(filename);
    vector<pair<int,int>> cells;
    string line;

    while (getline(fin, line)) {
        if (line.empty()) continue;
        stringstream ss(line);
        int x, y;
        char comma;
        ss >> x >> comma >> y;
        cells.push_back({x, y});
    }

    int N = cells.size();

    // Unique coordinates
    set<int> xSet, ySet;
    for (auto &c : cells) {
        xSet.insert(c.first);
        ySet.insert(c.second);
    }

    vector<int> xList(xSet.begin(), xSet.end());
    vector<int> yList(ySet.begin(), ySet.end());

    map<int,int> xMap, yMap;
    for (int i = 0; i < xList.size(); i++) xMap[xList[i]] = i;
    for (int i = 0; i < yList.size(); i++) yMap[yList[i]] = i;

    int W = xList.size();
    int H = yList.size();

    vector<pair<int,int>> mappedCells(N);
    for (int i = 0; i < N; i++) {
        mappedCells[i] = {xMap[cells[i].first], yMap[cells[i].second]};
    }

    // Blocked set
    set<pair<int,int>> blocked;
    for (int i = 0; i < N; i++) {
        int x1 = mappedCells[i].first, y1 = mappedCells[i].second;
        int x2 = mappedCells[(i+1)%N].first, y2 = mappedCells[(i+1)%N].second;

        if (x1 == x2) {
            for (int y = min(y1,y2); y <= max(y1,y2); y++)
                blocked.insert({x1, y});
        } else if (y1 == y2) {
            for (int x = min(x1,x2); x <= max(x1,x2); x++)
                blocked.insert({x, y1});
        } else {
            throw runtime_error("Non-rectilinear polygon detected");
        }
    }

    // Flood-fill outside
    set<pair<int,int>> bad;
    vector<pair<int,int>> stack = {{-1,-1}};
    int dirs[4][2] = {{-1,0},{1,0},{0,-1},{0,1}};

    while (!stack.empty()) {
        pair<int,int> cur = stack.back(); stack.pop_back();
        int cx = cur.first, cy = cur.second;
        for (int i = 0; i < 4; i++) {
            int nx = cx + dirs[i][0], ny = cy + dirs[i][1];
            pair<int,int> key = {nx, ny};
            if (bad.count(key) || blocked.count(key)) continue;
            if (nx < -1 || nx > W || ny < -1 || ny > H) continue;
            bad.insert(key);
            stack.push_back(key);
        }
    }

    // Rectangle checks
    long long maxTotal = 0;
    long long maxInside = 0;

    for (int i = 0; i < N; i++) {
        int x1 = cells[i].first, y1 = cells[i].second;
        int x1_ = mappedCells[i].first, y1_ = mappedCells[i].second;

        for (int j = 0; j < i; j++) {
            int x2 = cells[j].first, y2 = cells[j].second;
            int x2_ = mappedCells[j].first, y2_ = mappedCells[j].second;

            int minX = min(x1,x2), maxX = max(x1,x2);
            int minY = min(y1,y2), maxY = max(y1,y2);

            long long area = (long long)(maxX - minX + 1) * (maxY - minY + 1);
            maxTotal = max(maxTotal, area);

            int minXi = min(x1_, x2_), maxXi = max(x1_, x2_);
            int minYi = min(y1_, y2_), maxYi = max(y1_, y2_);
            if (area > maxInside && checkInside(bad, minXi, minYi, maxXi, maxYi))
                maxInside = area;
        }
    }

    return {maxTotal, maxInside};
}

int main() {
    pair<long long,long long> result = run("input.txt");
    cout << result.first << ", " << result.second << endl;
    return 0;
}
