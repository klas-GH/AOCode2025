def run():
    # Read input file
    with open("input.txt", "r", encoding="utf-8") as f:
        data = f.read().strip()

    # Split by comma, dash & convert to numbers
    arr = [list(map(int, s.strip().split("-"))) for s in data.split(",")]

    sum1 = 0
    sum2 = 0

    for L, R in arr:
        sum1 += test1(L, R)
        sum2 += test2(L, R)

    return sum1, sum2


def test1(l, r):
    total = 0
    lenL = len(str(l))
    lenR = len(str(r))

    # start from next even length >= lenL
    startLen = lenL if lenL % 2 == 0 else lenL + 1

    for length in range(startLen, lenR + 1, 2):
        halfLen = length // 2
        start = 10 ** (halfLen - 1)
        end = 10 ** halfLen - 1
        mul = 10 ** halfLen

        for half in range(start, end + 1):
            candidate = half * mul + half
            if candidate > r:
                break
            if candidate >= l:
                total += candidate
    return total


def test2(l, r):
    lenL = len(str(l))
    lenR = len(str(r))
    seen = set()

    # precompute powers of 10
    pow10 = [1]
    for i in range(1, lenR + 1):
        pow10.append(pow10[-1] * 10)

    factorCache = {}

    def repFactor(d, k):
        key = (d, k)
        if key in factorCache:
            return factorCache[key]
        pow_d = pow10[d]
        pow_dk = pow10[d * k]
        factor = (pow_dk - 1) // (pow_d - 1)
        factorCache[key] = factor
        return factor

    for n in range(lenL, lenR + 1):
        half = n // 2
        for d in range(1, half + 1):
            if n % d != 0:
                continue
            k = n // d
            startBase = pow10[d - 1]
            endBase = pow10[d] - 1
            factor = repFactor(d, k)

            if endBase * factor < l:
                continue
            if startBase * factor > r:
                continue

            baseMin = max(startBase, (l + factor - 1) // factor)
            baseMax = min(endBase, r // factor)

            if baseMin > baseMax:
                continue

            for base in range(baseMin, baseMax + 1):
                candidate = base * factor
                seen.add(candidate)

    return sum(seen)


if __name__ == "__main__":
    print(run())
