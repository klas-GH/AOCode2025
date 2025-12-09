from collections import deque

def run():
    # 1. Read input
    with open("input.txt", "r") as f:
        data = [list(map(int, line.strip().split(","))) for line in f if line.strip()]

    cells = data

    # 2. Collect unique x and y coordinates
    xList = sorted({c[0] for c in cells})
    yList = sorted({c[1] for c in cells})

    xMap = {v: i for i, v in enumerate(xList)}
    yMap = {v: i for i, v in enumerate(yList)}

    mappedCells = [(xMap[x], yMap[y]) for x, y in cells]
    W, H = len(xList), len(yList)

    # 3. Build blocked set for polygon edges
    blocked = set()
    N = len(mappedCells)
    for i in range(N):
        x1, y1 = mappedCells[i]
        x2, y2 = mappedCells[(i+1) % N]

        if x1 == x2:
            for y in range(min(y1, y2), max(y1, y2) + 1):
                blocked.add((x1, y))
        elif y1 == y2:
            for x in range(min(x1, x2), max(x1, x2) + 1):
                blocked.add((x, y1))
        else:
            raise ValueError("Non-rectilinear polygon detected")

    # 4. Flood-fill to mark outside
    DIRS = [(-1,0), (1,0), (0,-1), (0,1)]
    bad = set()
    stack = deque([(-1, -1)])

    while stack:
        cx, cy = stack.pop()
        for dx, dy in DIRS:
            nx, ny = cx + dx, cy + dy
            key = (nx, ny)
            if key in bad or key in blocked:
                continue
            if nx < -1 or nx > W or ny < -1 or ny > H:
                continue
            bad.add(key)
            stack.append((nx, ny))

    # 5. Function to check if rectangle is fully inside polygon
    def checkInside(x1_, y1_, x2_, y2_):
        for x in range(x1_, x2_ + 1):
            for y in range(y1_, y2_ + 1):
                if (x, y) in bad:
                    return False
        return True

    maxTotal = 0
    maxInside = 0

    for i, (x1, y1) in enumerate(cells):
        x1_, y1_ = xMap[x1], yMap[y1]
        for j in range(i):
            x2, y2 = cells[j]
            x2_, y2_ = xMap[x2], yMap[y2]

            minX, maxX = min(x1, x2), max(x1, x2)
            minY, maxY = min(y1, y2), max(y1, y2)

            area = (maxX - minX + 1) * (maxY - minY + 1)
            if area > maxTotal:
                maxTotal = area

            minXi, maxXi = min(x1_, x2_), max(x1_, x2_)
            minYi, maxYi = min(y1_, y2_), max(y1_, y2_)
            if area > maxInside and checkInside(minXi, minYi, maxXi, maxYi):
                maxInside = area

    return maxTotal, maxInside


if __name__ == "__main__":
    print(run())
