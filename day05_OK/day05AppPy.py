def run():
    # read file
    with open("input.txt", "r", encoding="utf8") as f:
        data = f.read()

    # split into rows
    arr = data.splitlines()

    # find blank line index
    idx = arr.index("")

    arrMrg = []

    def searchNrBN(nr):
        lo, hi = 0, len(arrMrg) - 1
        while lo <= hi:
            mid = (lo + hi) // 2
            l, r = arrMrg[mid]
            if nr < l:
                hi = mid - 1
            elif nr > r:
                lo = mid + 1
            else:
                return True
        return False

    def pushMergeBinSrch(l, r):
        lo, hi = 0, len(arrMrg) - 1
        pos = len(arrMrg)
        while lo <= hi:
            mid = (lo + hi) // 2
            if arrMrg[mid][1] >= l:
                pos = mid
                hi = mid - 1
            else:
                lo = mid + 1

        if pos == len(arrMrg) or arrMrg[pos][0] > r:
            arrMrg.insert(pos, [l, r])
            return

        newL = min(arrMrg[pos][0], l)
        newR = max(arrMrg[pos][1], r)
        j = pos + 1
        while j < len(arrMrg) and arrMrg[j][0] <= newR:
            newR = max(newR, arrMrg[j][1])
            j += 1

        arrMrg[pos:j] = [[newL, newR]]

    # build merged intervals
    for i in range(idx):
        l, r = map(int, arr[i].split("-"))
        pushMergeBinSrch(l, r)

    # test1
    sum1 = 0
    for i in range(idx + 1, len(arr)):
        if arr[i].strip():  # skip empty lines
            sum1 += 1 if searchNrBN(int(arr[i])) else 0

    # test2
    sum2 = 0
    for l, r in arrMrg:
        sum2 += r - l + 1

    return [sum1, sum2]


if __name__ == "__main__":
    print(run())
