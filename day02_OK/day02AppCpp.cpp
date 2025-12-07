#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <set>
#include <map>
#include <utility>
#include <cmath>
#include <algorithm>

using namespace std;

long long test1(long long l, long long r) {
    long long total = 0;
    int lenL = to_string(l).size();
    int lenR = to_string(r).size();

    int startLen = (lenL % 2 == 0) ? lenL : lenL + 1;

    for (int len = startLen; len <= lenR; len += 2) {
        int halfLen = len / 2;
        long long start = (long long) pow(10, halfLen - 1);
        long long end   = (long long) pow(10, halfLen) - 1;
        long long mul   = (long long) pow(10, halfLen);

        for (long long half = start; half <= end; ++half) {
            long long candidate = half * mul + half;
            if (candidate > r) break;
            if (candidate >= l) total += candidate;
        }
    }
    return total;
}

long long test2(long long l, long long r) {
    int lenL = to_string(l).size();
    int lenR = to_string(r).size();
    set<long long> seen;

    // precompute powers of 10
    vector<long long> pow10(lenR + 1);
    pow10[0] = 1;
    for (int i = 1; i <= lenR; i++) pow10[i] = pow10[i - 1] * 10;

    map<pair<int,int>, long long> factorCache;
    auto repFactor = [&](int d, int k) {
        pair<int,int> key = {d,k};
        if (factorCache.count(key)) return factorCache[key];
        long long pow_d  = pow10[d];
        long long pow_dk = pow10[d * k];
        long long factor = (pow_dk - 1) / (pow_d - 1);
        factorCache[key] = factor;
        return factor;
    };

    for (int n = lenL; n <= lenR; n++) {
        int half = n / 2;
        for (int d = 1; d <= half; d++) {
            if (n % d != 0) continue;
            int k = n / d;

            long long startBase = pow10[d - 1];
            long long endBase   = pow10[d] - 1;
            long long factor    = repFactor(d, k);

            if (endBase * factor < l) continue;
            if (startBase * factor > r) continue;

            long long baseMin = max(startBase, (l + factor - 1) / factor);
            long long baseMax = min(endBase, r / factor);

            if (baseMin > baseMax) continue;

            for (long long base = baseMin; base <= baseMax; base++) {
                long long candidate = base * factor;
                seen.insert(candidate);
            }
        }
    }

    long long total = 0;
    for (auto v : seen) total += v;
    return total;
}

int main() {
    ifstream infile("input.txt");
    string data;
    getline(infile, data); // assume single line input
    infile.close();

    // split by comma, then dash
    vector<pair<long long,long long>> arr;
    stringstream ss(data);
    string token;
    while (getline(ss, token, ',')) {
        stringstream ss2(token);
        string left, right;
        getline(ss2, left, '-');
        getline(ss2, right, '-');
        long long L = stoll(left);
        long long R = stoll(right);
        arr.push_back({L,R});
    }

    long long sum1 = 0, sum2 = 0;
    for (auto &p : arr) {
        sum1 += test1(p.first, p.second);
        sum2 += test2(p.first, p.second);
    }

    cout << "sum1 = " << sum1 << "\n";
    cout << "sum2 = " << sum2 << "\n";
    return 0;
}
