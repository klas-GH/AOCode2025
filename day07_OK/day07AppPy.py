def run():
    # Read the file and split into rows
    with open("input.txt", "r", encoding="utf-8") as f:
        data = [line.strip() for line in f if line.strip()]

    grid = data
    m = len(grid)
    n = len(grid[0])
    source_idx = grid[0].index("S")

    sum1 = 0
    sum2 = 0

    # test1: count encounters (merged beams)
    def test1():
        nonlocal sum1
        beams = {source_idx}

        for r in range(1, m):
            new_beams = set()
            for c in beams:
                if grid[r][c] == "^":
                    sum1 += 1
                    if c - 1 >= 0:
                        new_beams.add(c - 1)
                    if c + 1 < n:
                        new_beams.add(c + 1)
                else:
                    new_beams.add(c)
            beams = new_beams

    test1()

    # test2: count timelines (many-worlds)
    def test2():
        nonlocal sum2
        paths = [0] * n
        paths[source_idx] = 1
        beams = {source_idx}

        for r in range(1, m):
            new_beams = set()
            for c in beams:
                if grid[r][c] == "^":
                    if c - 1 >= 0:
                        paths[c - 1] += paths[c]
                        new_beams.add(c - 1)
                    if c + 1 < n:
                        paths[c + 1] += paths[c]
                        new_beams.add(c + 1)
                    paths[c] = 0
                else:
                    new_beams.add(c)
            beams = new_beams

        sum2 = sum(paths)

    test2()

    return sum1, sum2


if __name__ == "__main__":
    print(run())
