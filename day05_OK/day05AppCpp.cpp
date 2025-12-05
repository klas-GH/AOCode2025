#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <utility>
#include <algorithm>

using namespace std;

vector<pair<long long,long long>> arrMrg;

// binary search: check if nr lies in any merged interval
bool searchNrBN(long long nr) {
    int lo = 0, hi = (int)arrMrg.size() - 1;
    while (lo <= hi) {
        int mid = (lo + hi) / 2;
        long long l = arrMrg[mid].first;
        long long r = arrMrg[mid].second;
        if (nr < l) {
            hi = mid - 1;
        } else if (nr > r) {
            lo = mid + 1;
        } else {
            return true; // inside interval
        }
    }
    return false;
}

// insert [l,r] into arrMrg, merging overlaps
void pushMergeBinSrch(long long l, long long r) {
    int lo = 0, hi = (int)arrMrg.size() - 1;
    int pos = arrMrg.size();
    while (lo <= hi) {
        int mid = (lo + hi) / 2;
        if (arrMrg[mid].second >= l) {
            pos = mid;
            hi = mid - 1;
        } else {
            lo = mid + 1;
        }
    }

    if (pos == (int)arrMrg.size() || arrMrg[pos].first > r) {
        arrMrg.insert(arrMrg.begin() + pos, {l,r});
        return;
    }

    long long newL = min(arrMrg[pos].first, l);
    long long newR = max(arrMrg[pos].second, r);
    int j = pos + 1;
    while (j < (int)arrMrg.size() && arrMrg[j].first <= newR) {
        newR = max(newR, arrMrg[j].second);
        j++;
    }
    arrMrg.erase(arrMrg.begin() + pos, arrMrg.begin() + j);
    arrMrg.insert(arrMrg.begin() + pos, {newL,newR});
}

int main() {
    ifstream infile("input.txt");
    vector<string> arr;
    string line;
    while (getline(infile, line)) {
        arr.push_back(line);
    }

    // find blank line index
    int idx = -1;
    for (int i=0; i<(int)arr.size(); i++) {
        if (arr[i] == "") { idx = i; break; }
    }

    // build merged intervals from arr[0..idx-1]
    for (int i=0; i<idx; i++) {
        long long l,r;
        char dash;
        stringstream ss(arr[i]);
        ss >> l >> dash >> r;
        pushMergeBinSrch(l,r);
    }

    // test1: count numbers inside intervals
    long long sum = 0;
    for (int i=idx+1; i<(int)arr.size(); i++) {
        long long nr = stoll(arr[i]); // use stoll for long long
        if (searchNrBN(nr)) sum++;
    }

    // test2: total length of merged intervals
    long long sum2 = 0;
    for (auto &p : arrMrg) {
        sum2 += p.second - p.first + 1;
    }

    cout << "[" << sum << ", " << sum2 << "]\n";
    return 0;
}
