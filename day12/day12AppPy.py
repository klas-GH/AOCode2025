from typing import List, Tuple
import itertools

class Grid:
    def __init__(self, data: str = "", width: int = 0, height: int = 0):
        self.data = data
        self.width = width
        self.height = height

    def __getitem__(self, idx: int) -> str:
        return self.data[idx]

    def get(self, x: int, y: int) -> str:
        return self.data[y * self.width + x]

    def set(self, idx: int, c: str):
        self.data = self.data[:idx] + c + self.data[idx+1:]

    def __eq__(self, other):
        return isinstance(other, Grid) and self.data == other.data

    def __hash__(self):
        return hash(self.data)


class Region:
    def __init__(self, width: int = 0, height: int = 0, quantity: List[int] = None):
        self.width = width
        self.height = height
        self.quantity = quantity or []


class Situation:
    def __init__(self):
        self.shapes: List[Grid] = []
        self.regions: List[Region] = []


def load_input(file: str) -> Situation:
    ret = Situation()
    parse_shapes = True
    with open(file) as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            if "x" in line:
                parse_shapes = False
            if parse_shapes:
                if line.endswith(":"):
                    ret.shapes.append(Grid())
                    continue
                g = ret.shapes[-1]
                g.data += line
                g.width = len(line)
                g.height += 1
            else:
                dims, data = line.split(":", 1)
                w, h = map(int, dims.split("x"))
                region = Region(w, h, list(map(int, data.strip().split())))
                ret.regions.append(region)
    return ret


def transform(grid: Grid, mapping: List[int]) -> Grid:
    out_data = list(grid.data)
    for i in range(9):
        out_data[i] = grid.data[mapping[i]]
    return Grid("".join(out_data), grid.width, grid.height)


def place(shapes_list: List[List[Grid]], grid: Grid, x_steps: int, y_steps: int, depth: int, max_depth: int) -> bool:
    modified: List[int] = []
    shapes = shapes_list[depth]

    for y in range(y_steps):
        for x in range(x_steps):
            for shape in shapes:
                for idx in modified:
                    grid.set(idx, ".")
                modified.clear()

                valid = True
                for py in range(3):
                    if not valid: break
                    for px in range(3):
                        if shape.get(px, py) == "#":
                            gx, gy = x + px, y + py
                            idx = gy * grid.width + gx
                            if grid[idx] == "#":
                                valid = False
                                break
                            modified.append(idx)
                            grid.set(idx, "#")

                if not valid:
                    continue

                if depth + 1 == max_depth:
                    return True
                if place(shapes_list, grid, x_steps, y_steps, depth + 1, max_depth):
                    return True
    return False

def part1(situation: Situation) -> int:
    shape_areas = [shape.data.count("#") for shape in situation.shapes]

    sum_val = 0
    for region in situation.regions:
        region_spaces = region.width * region.height
        spaces_needed = sum(shape_areas[i] * region.quantity[i] for i in range(len(region.quantity)))
        if spaces_needed <= region_spaces:
            sum_val += 1
    return sum_val


if __name__ == "__main__":
    actual_values = load_input("input.txt")
    print("part1:", part1(actual_values))
