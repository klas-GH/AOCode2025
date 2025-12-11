def run():
    # read file synchronously and split into rows
    with open("input.txt", "r", encoding="utf8") as f:
        data = [line.strip() for line in f if line.strip()]

    # split into [node, neighbors]
    arr = []
    for e in data:
        node, neighbors = e.split(":")
        arr.append((node.strip(), neighbors.strip().split()))

    # build graph as dict
    graph = {node: neighbors for node, neighbors in arr}

    # test1: count all paths from "you" to "out"
    start = "you"
    end = "out"
    count = 0

    def dfs(node):
        nonlocal count
        if node == end:
            count += 1
            return
        for neighbor in graph.get(node, []):
            dfs(neighbor)

    dfs(start)
    ans1 = count

    # test2: count paths from "svr" to "out" that pass through both cond1 and cond2
    start2 = "svr"
    cond1 = "dac"
    cond2 = "fft"

    memo = {}

    def dfs2(node, has1, has2):
        key = (node, has1, has2)
        if key in memo:
            return memo[key]

        if node == cond1:
            has1 = True
        if node == cond2:
            has2 = True

        if node == end:
            result = 1 if (has1 and has2) else 0
            memo[key] = result
            return result

        total = 0
        for nxt in graph.get(node, []):
            total += dfs2(nxt, has1, has2)

        memo[key] = total
        return total

    ans2 = dfs2(start2, False, False)

    return [ans1, ans2]


if __name__ == "__main__":
    print(run())
