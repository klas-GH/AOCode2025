const fs = require("fs");

class Grid {
  constructor(data = "", width = 0, height = 0) {
    this.data = data;
    this.width = width;
    this.height = height;
  }

  get(x, y) {
    return this.data[y * this.width + x];
  }

  getIdx(idx) {
    return this.data[idx];
  }

  set(idx, c) {
    this.data =
      this.data.substring(0, idx) + c + this.data.substring(idx + 1);
  }

  equals(other) {
    return this.data === other.data;
  }

  hash() {
    return this.data;
  }
}

class Region {
  constructor(width = 0, height = 0, quantity = []) {
    this.width = width;
    this.height = height;
    this.quantity = quantity;
  }
}

class Situation {
  constructor() {
    this.shapes = [];
    this.regions = [];
  }
}

function loadInput(file) {
  const ret = new Situation();
  const lines = fs.readFileSync(file, "utf-8").split(/\r?\n/);
  let parseShapes = true;

  for (let line of lines) {
    if (!line) continue;
    if (line.includes("x")) parseShapes = false;

    if (parseShapes) {
      if (line.endsWith(":")) {
        ret.shapes.push(new Grid());
        continue;
      }
      const g = ret.shapes[ret.shapes.length - 1];
      g.data += line;
      g.width = line.length;
      g.height++;
    } else {
      const region = new Region();
      ret.regions.push(region);
      const dims = line.substring(0, line.indexOf(":"));
      const data = line.substring(line.indexOf(":") + 2);

      region.width = parseInt(dims.substring(0, dims.indexOf("x")));
      region.height = parseInt(dims.substring(dims.indexOf("x") + 1));

      region.quantity = data.split(/\s+/).filter(Boolean).map(Number);
    }
  }
  return ret;
}

function transform(grid, map) {
  let outData = grid.data.split("");
  for (let i = 0; i < 9; i++) {
    outData[i] = grid.data[map[i]];
  }
  return new Grid(outData.join(""), grid.width, grid.height);
}

function place(shapesList, grid, xSteps, ySteps, depth, maxDepth) {
  let modified = [];
  const shapes = shapesList[depth];

  for (let y = 0; y < ySteps; y++) {
    for (let x = 0; x < xSteps; x++) {
      for (let shape of shapes) {
        for (let idx of modified) grid.set(idx, ".");
        modified = [];

        let valid = true;
        for (let py = 0; py < 3 && valid; py++) {
          for (let px = 0; px < 3; px++) {
            if (shape.get(px, py) === "#") {
              const gx = x + px,
                gy = y + py;
              const idx = gy * grid.width + gx;
              if (grid.getIdx(idx) === "#") {
                valid = false;
                break;
              }
              modified.push(idx);
              grid.set(idx, "#");
            }
          }
        }

        if (!valid) continue;

        if (depth + 1 === maxDepth) return true;
        if (place(shapesList, grid, xSteps, ySteps, depth + 1, maxDepth))
          return true;
      }
    }
  }
  return false;
}


function part1(situation) {
  const shapeAreas = situation.shapes.map(
    (s) => [...s.data].filter((c) => c === "#").length
  );

  let sum = 0;
  for (let region of situation.regions) {
    const regionSpaces = region.width * region.height;
    let spacesNeeded = 0;
    for (let i = 0; i < region.quantity.length; i++) {
      spacesNeeded += shapeAreas[i] * region.quantity[i];
    }
    if (spacesNeeded <= regionSpaces) sum++;
  }
  return sum;
}

// Main
const actualValues = loadInput("input.txt");
console.log("part1:", part1(actualValues));
