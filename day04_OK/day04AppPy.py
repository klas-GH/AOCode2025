def run():
    # Read the file
    with open("input.txt", "r", encoding="utf-8") as f:
        arr = [list(line.strip()) for line in f if line.strip()]

    DIR = [(1, 0), (-1, 0), (1, 1), (-1, 1),
           (0, 1), (0, -1), (-1, -1), (1, -1)]

    sum1 = 0
    sum2 = 0
    queue = []

    def isValid(x, y):
        if x < 0 or x >= len(arr) or y < 0 or y >= len(arr[0]) or arr[x][y] != '@':
            return False

        count = 0
        for dx, dy in DIR:
            nx, ny = x + dx, y + dy
            if nx < 0 or nx >= len(arr) or ny < 0 or ny >= len(arr[0]) or arr[nx][ny] != '@':
                continue
            count += 1
        return count < 4

    # test1
    def test1():
        nonlocal sum1
        for i in range(len(arr)):
            for j in range(len(arr[0])):
                if arr[i][j] != '@':
                    continue
                if isValid(i, j):
                    sum1 += 1
                    queue.append((i, j))

    test1()

    # test2
    def test2():
        nonlocal sum2
        visited = set()
        idx = 0

        while idx < len(queue):
            i, j = queue[idx]
            idx += 1

            key = f"{i}#{j}"
            if key in visited:
                continue
            visited.add(key)

            if arr[i][j] == '@' and isValid(i, j):
                arr[i][j] = "."
                sum2 += 1

                for dx, dy in DIR:
                    nx, ny = i + dx, j + dy
                    if isValid(nx, ny):
                        queue.append((nx, ny))

    test2()

    return sum1, sum2


if __name__ == "__main__":
    print(run())
