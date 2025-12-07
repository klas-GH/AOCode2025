#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <string>

using namespace std;

pair<long long, long long> run(const string& filename) {
    // Read all non-empty lines
    ifstream infile(filename);
    if (!infile) {
        cerr << "Failed to open " << filename << endl;
        return {0LL, 0LL};
    }

    vector<string> rowsStr;
    string line;
    while (getline(infile, line)) {
        // keep lines that are not all whitespace
        bool hasNonSpace = false;
        for (char ch : line) {
            if (!isspace(static_cast<unsigned char>(ch))) { hasNonSpace = true; break; }
        }
        if (hasNonSpace) rowsStr.push_back(line);
    }
    infile.close();

    if (rowsStr.empty()) return {0LL, 0LL};

    // Last line is operators (by character)
    string operLine = rowsStr.back();
    rowsStr.pop_back();

    // Build arrOper: (index, operator char) for non-space characters
    vector<pair<int,char>> arrOper;
    for (int i = 0; i < static_cast<int>(operLine.size()); ++i) {
        char ch = operLine[i];
        if (ch != ' ') arrOper.emplace_back(i, ch);
    }

    // For test1: tokenize numeric rows
    vector<vector<long long>> arrNums;
    arrNums.reserve(rowsStr.size());
    for (const string& r : rowsStr) {
        istringstream iss(r);
        vector<long long> row;
        long long x;
        while (iss >> x) row.push_back(x);
        arrNums.push_back(move(row));
    }

    // Validate dimensions for test1
    if (arrNums.empty() || arrNums[0].empty() || static_cast<int>(arrOper.size()) < static_cast<int>(arrNums[0].size())) {
        cerr << "Input shape mismatch for test1." << endl;
        return {0LL, 0LL};
    }

    long long sum1 = 0;
    long long sum2 = 0;

    // ---------- test1 (column-wise numbers with operator) ----------
    {
        int cols = static_cast<int>(arrNums[0].size());
        int rows = static_cast<int>(arrNums.size());
        for (int j = 0; j < cols; ++j) {
            long long colTotal = arrNums[0][j];
            for (int i = 1; i < rows; ++i) {
                if (arrOper[j].second == '+') {
                    colTotal += arrNums[i][j];
                } else {
                    colTotal *= arrNums[i][j];
                }
            }
            sum1 += colTotal;
        }
    }

    // ---------- test2 (character-wise concatenation like JS) ----------
    {
        if (rowsStr.empty()) {
            sum2 = 0;
        } else {
            int numRows = static_cast<int>(rowsStr.size());
            int numColsChars = static_cast<int>(rowsStr[0].size());

            // Optional: ensure all rows have the same width
            for (const auto& r : rowsStr) {
                if (static_cast<int>(r.size()) != numColsChars) {
                    cerr << "Row widths differ; test2 expects fixed-width lines." << endl;
                    // You can choose to handle ragged rows by per-row bounds checks.
                }
            }

            for (int z = 0; z < static_cast<int>(arrOper.size()); ++z) {
                int idx = arrOper[z].first;
                char op = arrOper[z].second;

                int lastpos = (z != static_cast<int>(arrOper.size()) - 1)
                              ? (arrOper[z + 1].first - 2)
                              : (numColsChars - 1);

                long long colTotal = (op == '+') ? 0LL : 1LL;

                for (int pos = lastpos; pos >= idx; --pos) {
                    if (pos < 0 || pos >= numColsChars) continue;
                    // Build the column string across all rows
                    string s;
                    s.reserve(numRows);
                    for (int i = 0; i < numRows; ++i) {
                        // Bounds guard (in case of ragged rows)
                        if (pos < static_cast<int>(rowsStr[i].size()))
                            s.push_back(rowsStr[i][pos]);
                    }
                    // Convert to integer
                    long long val = stoll(s);
                    if (op == '+') {
                        colTotal += val;
                    } else {
                        colTotal *= val;
                    }
                }
                sum2 += colTotal;
            }
        }
    }

    return {sum1, sum2};
}

int main() {
    auto result = run("input.txt");
    cout << "sum1 = " << result.first << "\n";
    cout << "sum2 = " << result.second << "\n";
    return 0;
}
