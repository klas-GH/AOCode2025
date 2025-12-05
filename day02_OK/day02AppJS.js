function run() {
  const fs = require('fs');

  // we read input file synchronously
  const data = fs.readFileSync('input.txt', 'utf8');

  // we split by comma, dash & convert to numbers
  const arr = data.split(/,/).map(s => s.trim().split(/-/).map(Number));

  let sum = 0;
  let sum2 = 0;

  for (let [L, R] of arr) {
    sum += test1(L, R);
    sum2 += test2(L, R);
  }

  // test1: mirrored "double numbers" of even length (arithmetic candidate)
  function test1(l, r) {
    let total = 0;
    const lenL = String(l).length;
    const lenR = String(r).length;

    // we start from the next even length >= lenL
    let startLen = (lenL % 2 === 0) ? lenL : lenL + 1;

    for (let len = startLen; len <= lenR; len += 2) {
      const halfLen = len / 2;
      const start = Math.pow(10, halfLen - 1);
      const end = Math.pow(10, halfLen) - 1;
      const mul = Math.pow(10, halfLen); // multiplier to shift half left

      for (let half = start; half <= end; half++) {
        // arithmetic concatenation: half * 10^halfLen + half
        const candidate = half * mul + half;
        if (candidate > r) break; // early exit (monotonic in half)
        if (candidate >= l) total += candidate;
      }
    }
    return total;
  }

  // test2: generate repeated-pattern numbers only, deduplicate, optimized
  function test2(l, r) {
    const lenL = String(l).length;
    const lenR = String(r).length;
    const seen = new Set();

    // we precompute powers of 10 up to lenR
    const pow10 = [1];
    for (let i = 1; i <= lenR; i++) pow10[i] = pow10[i - 1] * 10;

    // we cache repFactor(d,k) = (10^(d*k) - 1) / (10^d - 1)
    const factorCache = new Map();
    function repFactor(d, k) {
      const key = d + ',' + k;
      if (factorCache.has(key)) return factorCache.get(key);
      // use precomputed pow10 to avoid Math.pow calls
      const pow_d = pow10[d];           // 10^d
      const pow_dk = pow10[d * k];     // 10^(d*k)
      const factor = (pow_dk - 1) / (pow_d - 1);
      factorCache.set(key, factor);
      return factor;
    }

    for (let n = lenL; n <= lenR; n++) {
      const half = Math.floor(n / 2);
      for (let d = 1; d <= half; d++) {
        if (n % d !== 0) continue;
        const k = n / d; // repeat count >= 2

        const startBase = pow10[d - 1];
        const endBase = pow10[d] - 1;

        const factor = repFactor(d, k);

        // we skip if even the largest base produces candidate < l
        if (endBase * factor < l) continue;
        // we skip if even the smallest base produces candidate > r
        if (startBase * factor > r) continue;

        // tight base bounds so base*factor in [l, r]
        let baseMin = Math.ceil(l / factor);
        if (baseMin < startBase) baseMin = startBase;

        let baseMax = Math.floor(r / factor);
        if (baseMax > endBase) baseMax = endBase;

        if (baseMin > baseMax) continue;

        // we add candidates; candidate increases by factor each step
        for (let base = baseMin, candidate = base * factor; base <= baseMax; base++, candidate += factor) {
          seen.add(candidate);
        }
      }
    }

    // we sum unique candidates
    let total = 0;
    for (const v of seen) 
		total += v;
    return total;
  }

  return [sum, sum2];
}

console.log(run());
