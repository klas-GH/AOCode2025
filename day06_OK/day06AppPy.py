def run():
    # Read file and split into rows (keep raw strings for test2)
    with open("input.txt", "r", encoding="utf-8") as f:
        rowsStr = [line.rstrip("\n") for line in f if line.strip()]

    # Last line contains operators (by character positions)
    oper_line = rowsStr.pop()
    arrOper = [(i, ch) for i, ch in enumerate(oper_line) if ch != " "]

    # For test1: numeric matrix (split by whitespace)
    arrNums = [list(map(int, row.split())) for row in rowsStr]

    sum1 = 0
    sum2 = 0

    # ---------- test1 (column-wise numbers with operator) ----------
    for j in range(len(arrNums[0])):
        colTotal = arrNums[0][j]
        for i in range(1, len(arrNums)):
            if arrOper[j][1] == '+':
                colTotal += arrNums[i][j]
            else:
                colTotal *= arrNums[i][j]
        sum1 += colTotal

    # ---------- test2 (character-wise concatenation like JS) ----------
    # rowsStr are the original non-empty lines before oper_line, un-split strings
    numRows = len(rowsStr)
    numColsChars = len(rowsStr[0])  # character width of rows

    for z in range(len(arrOper)):
        idx, op = arrOper[z]
        if z != len(arrOper) - 1:
            lastpos = arrOper[z + 1][0] - 2
        else:
            lastpos = numColsChars - 1

        colTotal = 0 if op == '+' else 1

        # Descend from lastpos down to idx (inclusive), by character position
        for pos in range(lastpos, idx - 1, -1):
            # Bounds guard in case of ragged lines
            if pos < 0 or pos >= numColsChars:
                continue
            # Concatenate the character at 'pos' from each row
            s = "".join(rowsStr[i][pos] for i in range(numRows))
            # Convert the concatenated string to int and apply operator
            val = int(s)
            if op == '+':
                colTotal += val
            else:
                colTotal *= val

        sum2 += colTotal

    return sum1, sum2


if __name__ == "__main__":
    print(run())
