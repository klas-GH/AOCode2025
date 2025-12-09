const fs = require('fs');

function run() {
  
  const data = fs.readFileSync('input.txt', 'utf8')
    .split(/\r?\n/)
    .filter(Boolean)
    .map(line => line.split(',').map(Number));

  const cells = data;

  const xSet = new Set(cells.map(c => c[0]));
  const ySet = new Set(cells.map(c => c[1]));

  const xList = Array.from(xSet).sort((a,b)=>a-b);
  const yList = Array.from(ySet).sort((a,b)=>a-b);

  const xMap = Object.fromEntries(xList.map((v,i)=>[v,i]));
  const yMap = Object.fromEntries(yList.map((v,i)=>[v,i]));

  const mappedCells = cells.map(([x,y]) => [xMap[x], yMap[y]]);
  const W = xList.length;
  const H = yList.length;

  // blocked set for polygon edges
  const blocked = new Set();
  const N = mappedCells.length;
  for (let i = 0; i < N; i++) {
    const [x1, y1] = mappedCells[i];
    const [x2, y2] = mappedCells[(i+1)%N];

    if (x1 === x2) {
      for (let y = Math.min(y1,y2); y <= Math.max(y1,y2); y++) {
        blocked.add(`${x1},${y}`);
      }
    } else if (y1 === y2) {
      for (let x = Math.min(x1,x2); x <= Math.max(x1,x2); x++) {
        blocked.add(`${x},${y1}`);
      }
    } else throw new Error("Non-rectilinear polygon");
  }

  // flood-fill to mark outside
  const DIRS = [[-1,0],[1,0],[0,-1],[0,1]];
  const bad = new Set();
  const stack = [[-1,-1]];

  while(stack.length) {
    const [cx, cy] = stack.pop();
    for (const [dx, dy] of DIRS) {
      const nx = cx + dx;
      const ny = cy + dy;
      const key = `${nx},${ny}`;
      if (bad.has(key) || blocked.has(key)) continue;
      if (nx < -1 || nx > W || ny < -1 || ny > H) continue;
      bad.add(key);
      stack.push([nx, ny]);
    }
  }

  // check if rectangle is fully inside
  function checkInside(x1_, y1_, x2_, y2_) {
    for (let x = x1_; x <= x2_; x++) {
      for (let y = y1_; y <= y2_; y++) {
        if (bad.has(`${x},${y}`)) return false;
      }
    }
    return true;
  }

  let maxTotal = 0; // largest rectangle ignoring polygon
  let maxInside = 0; // largest rectangle fully inside

  for (let i = 0; i < cells.length; i++) {
    const [x1, y1] = cells[i];
    const x1_ = xMap[x1], y1_ = yMap[y1];

    for (let j = 0; j < i; j++) {
      const [x2, y2] = cells[j];
      const x2_ = xMap[x2], y2_ = yMap[y2];

      const minX = Math.min(x1, x2);
      const maxX = Math.max(x1, x2);
      const minY = Math.min(y1, y2);
      const maxY = Math.max(y1, y2);

      const area = (maxX - minX + 1) * (maxY - minY + 1);
      if (area > maxTotal) maxTotal = area;

      const minXi = Math.min(x1_, x2_), maxXi = Math.max(x1_, x2_);
      const minYi = Math.min(y1_, y2_), maxYi = Math.max(y1_, y2_);
      if (area > maxInside && checkInside(minXi, minYi, maxXi, maxYi)) {
        maxInside = area;
      }
    }
  }

  return [maxTotal, maxInside];
}

console.log(run());
