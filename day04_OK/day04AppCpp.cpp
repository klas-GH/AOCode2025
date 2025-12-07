#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <set>

using namespace std;

const int DIR[8][2] = {
    {1, 0}, {-1, 0}, {1, 1}, {-1, 1},
    {0, 1}, {0, -1}, {-1, -1}, {1, -1}
};

vector<string> arr;
long long sum1 = 0;
long long sum2 = 0;
vector<pair<int,int>> queueCells;

bool isValid(int x, int y) {
    if (x < 0 || x >= (int)arr.size() || y < 0 || y >= (int)arr[0].size() || arr[x][y] != '@')
        return false;

    int count = 0;
    for (auto &d : DIR) {
        int nx = x + d[0];
        int ny = y + d[1];
        if (nx < 0 || nx >= (int)arr.size() || ny < 0 || ny >= (int)arr[0].size() || arr[nx][ny] != '@')
            continue;
        count++;
    }
    return count < 4;
}

void test1() {
    for (int i = 0; i < (int)arr.size(); i++) {
        for (int j = 0; j < (int)arr[0].size(); j++) {
            if (arr[i][j] != '@') continue;
            if (isValid(i, j)) {
                sum1++;
                queueCells.push_back({i, j});
            }
        }
    }
}

void test2() {
    set<string> visited;
    size_t idx = 0;

    while (idx < queueCells.size()) {
        auto [i, j] = queueCells[idx++];
        string key = to_string(i) + "#" + to_string(j);
        if (visited.count(key)) continue;
        visited.insert(key);

        if (arr[i][j] == '@' && isValid(i, j)) {
            arr[i][j] = '.';
            sum2++;

            for (auto &d : DIR) {
                int nx = i + d[0], ny = j + d[1];
                if (isValid(nx, ny)) {
                    queueCells.push_back({nx, ny});
                }
            }
        }
    }
}

int main() {
    ifstream infile("input.txt");
    string line;
    while (getline(infile, line)) {
        if (!line.empty()) {
            // trim
            size_t start = line.find_first_not_of(" \t\r\n");
            size_t end   = line.find_last_not_of(" \t\r\n");
            if (start != string::npos && end != string::npos) {
                arr.push_back(line.substr(start, end - start + 1));
            }
        }
    }
    infile.close();

    test1();
    test2();

    cout << "sum1 = " << sum1 << "\n";
    cout << "sum2 = " << sum2 << "\n";
    return 0;
}
