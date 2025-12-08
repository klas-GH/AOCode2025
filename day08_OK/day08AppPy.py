import heapq

def run():
    # read file and parse coordinates
    with open("input.txt") as f:
        data = [line.strip() for line in f if line.strip()]
    arr = [list(map(int, line.split(","))) for line in data]
    n = len(arr)

    # squared Euclidean distance
    def dist(x, y):
        return (y[0]-x[0])**2 + (y[1]-x[1])**2 + (y[2]-x[2])**2

    # --- Build all edges ---
    edges = []
    for i in range(n):
        for j in range(i+1, n):
            edges.append((dist(arr[i], arr[j]), i, j))

    # --- Min-heap ---
    heapq.heapify(edges)

    # --- Answer 1: pop 1000 edges, build graph, compute product of top 3 sizes ---
    graph = [set() for _ in range(n)]
    for _ in range(1000):
        if not edges: break
        d, i, j = heapq.heappop(edges)
        graph[i].add(j)
        graph[j].add(i)

    visited = set()
    def dfs(x, group):
        if x in visited:
            return
        visited.add(x)
        group.append(x)
        for y in graph[x]:
            dfs(y, group)

    groups = []
    for i in range(n):
        if i not in visited:
            g = []
            dfs(i, g)
            groups.append(g)

    sizes = sorted([len(g) for g in groups], reverse=True)
    sum1 = sizes[0] * sizes[1] * sizes[2]

    # --- Answer 2: continue popping edges until all connected ---
    parent = list(range(n))
    def find(x):
        if parent[x] != x:
            parent[x] = find(parent[x])
        return parent[x]
    def union(a, b):
        pa, pb = find(a), find(b)
        if pa != pb:
            parent[pa] = pb
            return True
        return False

    # union the first 1000 edges already used
    for i in range(n):
        for j in graph[i]:
            union(i, j)

    groups_count = len({find(i) for i in range(n)})
    last_edge = None

    while groups_count > 1 and edges:
        d, i, j = heapq.heappop(edges)
        if union(i, j):
            groups_count = len({find(k) for k in range(n)})
            last_edge = (i, j)

    if not last_edge:
        raise RuntimeError("No edge found to connect all groups")

    i, j = last_edge
    sum2 = arr[i][0] * arr[j][0]

    return sum1, sum2

if __name__ == "__main__":
    print(run())
