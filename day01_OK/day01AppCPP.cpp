#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <sstream>
#include <cmath>

using namespace std;

// Helper: read lines from file, trim, and filter empty
vector<string> readInput(const string &filename) {
    ifstream file(filename);
    vector<string> rows;
    string line;
    while (getline(file, line)) {
        // trim whitespace
        size_t start = line.find_first_not_of(" \t\r\n");
        size_t end = line.find_last_not_of(" \t\r\n");
        if (start != string::npos && end != string::npos) {
            string trimmed = line.substr(start, end - start + 1);
            if (!trimmed.empty()) {
                rows.push_back(trimmed);
            }
        }
    }
    return rows;
}

// test1: count landings on position 0
int test1(const vector<string> &rows) {
    int pos = 50; // initial position
    int count = 0;
    for (const string &val : rows) {
        char dir = val[0];
        int nr = abs(stoi(val.substr(1)));

        if (dir == 'R') {
            pos = (pos + nr) % 100;
        } else {
            pos = (100 + pos - nr) % 100;
        }

        if (pos == 0) count++;
    }
    return count;
}

// test2: count crossings through position 0
int test2(const vector<string> &rows) {
    int count = 0;
    int pos = 50; // initial position

    for (const string &val : rows) {
        char dir = val[0];
        int nr = abs(stoi(val.substr(1)));

        int stepsToFirstZero;
        if (dir == 'R') {
            stepsToFirstZero = (pos == 0 ? 100 : 100 - pos);
            if (nr >= stepsToFirstZero) {
                count += (nr - stepsToFirstZero) / 100 + 1;
            }
            pos = (pos + nr) % 100;
        } else {
            stepsToFirstZero = (pos == 0 ? 100 : pos);
            if (nr >= stepsToFirstZero) {
                count += (nr - stepsToFirstZero) / 100 + 1;
            }
            pos = ((pos - nr) % 100 + 100) % 100; // safe modulo
        }
    }
    return count;
}

int main() {
    vector<string> rows = readInput("input.txt");

    int sum1 = test1(rows);
    int sum2 = test2(rows);

    cout << "[" << sum1 << ", " << sum2 << "]" << endl;
    return 0;
}
