const fs = require('fs');
const EPSILON = 1e-9;


// ================= P2 Solver (locked) =================
function parseMachineInstructionsFromSplit(arr) {
  // Convert [part1, part2, part3] into wiring + joltage format
  return arr.map(([pattern, buttonLists, targets]) => {
    const wiringSchematics = buttonLists.map(bl => ({
      wiredButtons: bl.map(i => ({ lightIndex: i })),
      label: ""
    }));
    const joltageRequirements = targets;
    return { wiringSchematics, joltageRequirements };
  });
}

// --- LP / ILP utilities ---
function deepCopyConstraints(constraints) {
  return constraints.map(c => ({ relation: c.relation, rhs: c.rhs, coeffs: [...c.coeffs] }));
}

function normalizeModel(model) {
  return {
    numVars: model.numVars,
    objective: { sense: model.objective.sense || 'min', coeffs: [...model.objective.coeffs] },
    constraints: deepCopyConstraints(model.constraints)
  };
}

function cloneModelWithConstraint(model, varIndex, relation, rhs) {
  const copy = normalizeModel(model);
  const coeffs = Array(copy.numVars).fill(0);
  coeffs[varIndex] = 1;
  copy.constraints.push({ relation, rhs, coeffs });
  return copy;
}

// --- Tableau / Simplex ---
class Tableau {
  constructor(model) {
    this.nC = model.constraints.length;
    this.nV = model.numVars;
    this.tbl = [];
    this.basis = Array(this.nC).fill(-1);

    this.slack = 0; this.artificial = 0;
    model.constraints.forEach(c => {
      if (c.relation === '<=') this.slack++;
      else if (c.relation === '>=') { this.slack++; this.artificial++; }
      else if (c.relation === '=') this.artificial++;
    });

    this.totalVars = this.nV + this.slack + this.artificial;
    this.rhsCol = this.totalVars;
    this.artStart = this.nV + this.slack;
    this.phase1 = this.artificial > 0;

    this.tbl = Array.from({ length: this.nC + 1 }, () => Array(this.totalVars + 1).fill(0));

    let sOff = this.nV, aOff = this.nV + this.slack;
    model.constraints.forEach((c, r) => {
      c.coeffs.forEach((v, i) => this.tbl[r][i] = v);

      if (c.relation === '<=') { this.tbl[r][sOff] = 1; this.basis[r] = sOff++; }
      else if (c.relation === '>=') { this.tbl[r][sOff] = -1; this.tbl[r][aOff] = 1; this.basis[r] = aOff++; sOff++; }
      else if (c.relation === '=') { this.tbl[r][aOff] = 1; this.basis[r] = aOff++; }

      this.tbl[r][this.rhsCol] = c.rhs;
    });
  }

  pivot(r, c) {
    const w = this.tbl[0].length, pv = this.tbl[r][c];
    for (let col = 0; col < w; col++) this.tbl[r][col] /= pv;
    for (let row = 0; row < this.tbl.length; row++) {
      if (row === r) continue;
      const f = this.tbl[row][c];
      if (Math.abs(f) < EPSILON) continue;
      for (let col = 0; col < w; col++) this.tbl[row][col] -= f * this.tbl[r][col];
    }
    this.basis[r] = c;
  }

  simplex(colCount) {
    const rows = this.tbl.length;
    while (true) {
      let enter = -1, mostNeg = -EPSILON;
      for (let c = 0; c < colCount; c++) if (this.tbl[rows - 1][c] < mostNeg) { mostNeg = this.tbl[rows - 1][c]; enter = c; }
      if (enter === -1) return { status: 'optimal' };

      let leave = -1, bestRatio = Infinity;
      for (let r = 0; r < rows - 1; r++) {
        const coeff = this.tbl[r][enter];
        if (coeff > EPSILON) {
          const ratio = this.tbl[r][this.rhsCol] / coeff;
          if (ratio < bestRatio - EPSILON) { bestRatio = ratio; leave = r; }
        }
      }
      if (leave === -1) return { status: 'unbounded' };
      this.pivot(leave, enter);
    }
  }

  setObjective(coeffs, colCount) {
    const rows = this.tbl.length, rhsCol = this.rhsCol, last = rows - 1;
    for (let c = 0; c < colCount; c++) this.tbl[last][c] = -(coeffs[c] || 0);
    this.tbl[last][rhsCol] = 0;
    for (let r = 0; r < rows - 1; r++) {
      const bv = this.basis[r];
      if (bv < 0) continue;
      const coef = coeffs[bv] || 0;
      if (Math.abs(coef) < EPSILON) continue;
      for (let c = 0; c <= rhsCol; c++) this.tbl[last][c] += coef * this.tbl[r][c];
    }
  }

  eliminateArtificial() {
    for (let r = 0; r < this.basis.length; r++) {
      const v = this.basis[r];
      if (v >= this.artStart) {
        let enter = -1;
        for (let c = 0; c < this.artStart; c++) if (Math.abs(this.tbl[r][c]) > EPSILON) { enter = c; break; }
        if (enter !== -1) this.pivot(r, enter); else this.basis[r] = -1;
      }
    }
  }

  removeArtificialCols() {
    if (this.totalVars === this.artStart) return;
    const keep = [...Array(this.artStart).keys(), this.rhsCol];
    this.tbl = this.tbl.map(r => keep.map(c => r[c]));
    this.totalVars = this.artStart;
    this.rhsCol = this.artStart;
  }

  extractSolution(numVars) {
    const sol = Array(numVars).fill(0);
    for (let r = 0; r < this.basis.length; r++) {
      const v = this.basis[r];
      if (v >= 0 && v < numVars) sol[v] = this.tbl[r][this.rhsCol];
    }
    return sol;
  }
}

// --- LP / ILP solver ---
function solveLP(model) {
  const m = normalizeModel(model);
  const tableau = new Tableau(m);

  if (tableau.phase1) {
    const p1 = Array(tableau.totalVars).fill(0);
    for (let c = tableau.artStart; c < tableau.totalVars; c++) p1[c] = -1;
    tableau.setObjective(p1, tableau.totalVars);
    const r1 = tableau.simplex(tableau.totalVars);
    if (r1.status !== 'optimal' || Math.abs(tableau.tbl[tableau.tbl.length - 1][tableau.rhsCol]) > EPSILON) return { feasible: false };
    tableau.eliminateArtificial();
    tableau.removeArtificialCols();
  } else for (let c = 0; c <= tableau.rhsCol; c++) tableau.tbl[tableau.tbl.length - 1][c] = 0;

  const obj = Array(tableau.totalVars).fill(0);
  for (let c = 0; c < m.numVars; c++) obj[c] = -(m.objective.coeffs[c] || 0);
  tableau.setObjective(obj, tableau.totalVars);

  const r2 = tableau.simplex(tableau.totalVars);
  if (r2.status !== 'optimal') return { feasible: false };

  return { feasible: true, solution: tableau.extractSolution(m.numVars), objective: -tableau.tbl[tableau.tbl.length - 1][tableau.rhsCol] };
}

function findFractionalIndex(values) {
  let idx = -1, maxF = 0;
  values.forEach((v, i) => {
    const f = Math.abs(v - Math.round(v));
    if (f > EPSILON && f > maxF) { maxF = f; idx = i; }
  });
  return idx;
}

function solveIntegerProgram(model) {
  let best = null;

  function branch(m) {
    const lp = solveLP(m);
    if (!lp.feasible) return;
    if (best && lp.objective >= best.objective - EPSILON) return;

    const fi = findFractionalIndex(lp.solution);
    if (fi === -1) { best = !best || lp.objective < best.objective - EPSILON ? lp : best; return; }

    const v = lp.solution[fi];
    if (Math.floor(v) >= 0) branch(cloneModelWithConstraint(m, fi, '<=', Math.floor(v)));
    branch(cloneModelWithConstraint(m, fi, '>=', Math.ceil(v)));
  }

  branch(normalizeModel(model));
  if (!best) return { feasible: false };
  return { feasible: true, solution: best.solution, objective: best.objective };
}

function solveMachineForJoltage(machine) {
  const t = machine.joltageRequirements;
  if (!t || !t.length) return { bestSolution: 0 };

  const buttons = machine.wiringSchematics.map(s => {
    const effect = Array(t.length).fill(0);
    s.wiredButtons.forEach(b => effect[b.lightIndex] = 1);
    return { effect, coverage: s.wiredButtons.length };
  }).filter(b => b.coverage > 0);

  if (!buttons.length) return { bestSolution: null };

  const numButtons = buttons.length;
  const constraints = [];
  t.forEach((v, i) => constraints.push({ relation: '=', rhs: v, coeffs: buttons.map(b => b.effect[i] || 0) }));
  for (let i = 0; i < numButtons; i++) constraints.push({ relation: '>=', rhs: 0, coeffs: Array.from({ length: numButtons }, (_, j) => i === j ? 1 : 0) });

  const model = { numVars: numButtons, objective: { sense: 'min', coeffs: Array(numButtons).fill(1) }, constraints };
  const sol = solveIntegerProgram(model);
  if (!sol.feasible) return { bestSolution: null };
  return { bestSolution: sol.solution.reduce((a, b) => a + b, 0) };
}

// ================= Main integration =================
function run() {
  const data = fs.readFileSync('input.txt', 'utf8')
    .split(/\r?\n/)
    .filter(Boolean);

  const split = (line) => {
    const part1 = line.match(/^\[([#.]+)\]/)[1];

    const part2 = [...line.matchAll(/\(([\d,]+)\)/g)]
      .map(m => m[1].split(",").map(Number));

    const part3 = line.match(/\{([\d,]+)\}$/)[1]
      .split(",").map(Number);

    return [part1, part2, part3];
  };

  const arr = data.map(x => split(x));

  // ---------- Part 1: lights (BFS) ----------
  function minPresses(target, buttons, type = 1) {
    const n = target.length;
    const targetBits = type === 1
      ? target.split("").map(c => c === "#" ? 1 : 0)
      : target;

    const start = Array(n).fill(0);
    const seen = new Map();
    seen.set(start.join(","), 0);

    const queue = [start];

    while (queue.length) {
      const state = queue.shift();
      const steps = seen.get(state.join(","));
      if (state.join(",") === targetBits.join(",")) return steps;

      for (const btn of buttons) {
        const newState = [...state];
        for (const idx of btn) {
          if (type === 1) newState[idx] ^= 1;
          else newState[idx] += 1;
        }
        if (type === 2) {
          let valid = true;
          for (let i = 0; i < n; i++) if (newState[i] > targetBits[i]) { valid = false; break; }
          if (!valid) continue;
        }
        const key = newState.join(",");
        if (!seen.has(key)) { seen.set(key, steps + 1); queue.push(newState); }
      }
    }
  }

  const minCount1 = arr.reduce((sum, [target, buttons]) => sum + minPresses(target, buttons, 1), 0);

  // ---------- Part 2: counters (ILP) ----------
  const machines = parseMachineInstructionsFromSplit(arr);
  const minCount2 = machines.reduce((sum, m) => {
    const r = solveMachineForJoltage(m);
    if (r.bestSolution == null) throw new Error('Machine cannot be configured');
    return sum + r.bestSolution;
  }, 0);

  return [minCount1, minCount2];
}

console.log(run());
