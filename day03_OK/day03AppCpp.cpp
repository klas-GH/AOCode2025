#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <deque>

using namespace std;

long long test1(const string &s) {
    int k = 2;
    int toRemove = (int)s.size() - k;
    deque<char> stack;

    for (char digit : s) {
        while (!stack.empty() && toRemove > 0 && stack.back() < digit) {
            stack.pop_back();
            toRemove--;
        }
        stack.push_back(digit);
    }

    // ensure exactly k digits
    string result;
    for (int i = 0; i < k && i < (int)stack.size(); i++) {
        result.push_back(stack[i]);
    }
    return stoll(result);
}

long long test2(const string &s) {
    int k = 12;
    int toRemove = (int)s.size() - k;
    deque<char> stack;

    for (char digit : s) {
        while (!stack.empty() && toRemove > 0 && stack.back() < digit) {
            stack.pop_back();
            toRemove--;
        }
        stack.push_back(digit);
    }

    // ensure exactly k digits
    string result;
    for (int i = 0; i < k && i < (int)stack.size(); i++) {
        result.push_back(stack[i]);
    }
    return stoll(result);
}

int main() {
    ifstream infile("input.txt");
    vector<string> arr;
    string line;
    while (getline(infile, line)) {
        // trim
        if (!line.empty()) {
            // remove leading/trailing spaces
            size_t start = line.find_first_not_of(" \t\r\n");
            size_t end   = line.find_last_not_of(" \t\r\n");
            if (start != string::npos && end != string::npos) {
                arr.push_back(line.substr(start, end - start + 1));
            }
        }
    }
    infile.close();

    long long sum1 = 0, sum2 = 0;
    for (const string &s : arr) {
        sum1 += test1(s);
        sum2 += test2(s);
    }

    cout << "sum1 = " << sum1 << "\n";
    cout << "sum2 = " << sum2 << "\n";
    return 0;
}
