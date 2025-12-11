import fs from "fs";
import solver from "javascript-lp-solver";

function parseMachine(line) {
  const patternMatch = line.match(/\[([.#]+)\]/);
  const pattern = patternMatch[1];

  const buttonMatches = [...line.matchAll(/\(([0-9,]+)\)/g)];
  const buttonIndices = buttonMatches.map(m =>
    m[1].split(",").map(Number)
  );

  const joltageMatch = line.match(/\{([0-9,]+)\}/);
  const joltageTarget = joltageMatch[1].split(",").map(Number);

  return { pattern, buttonIndices, joltageTarget };
}

function solveMachineWithILP(joltageTarget, buttonIndices) {
  const numButtons = buttonIndices.length;
  const numCounters = joltageTarget.length;

  // constraints: each counter must equal its target
  const constraints = {};
  for (let j = 0; j < numCounters; j++) {
    constraints["c" + j] = { equal: joltageTarget[j] };
  }

  // variables: each button contributes +1 to counters it touches
  const variables = {};
  for (let i = 0; i < numButtons; i++) {
    const varName = "b" + i;
    const varObj = { presses: 1 }; // cost coefficient
    for (const idx of buttonIndices[i]) {
      varObj["c" + idx] = (varObj["c" + idx] || 0) + 1;
    }
    variables[varName] = varObj;
  }

  // all variables must be integers ≥ 0
  const ints = {};
  for (let i = 0; i < numButtons; i++) {
    ints["b" + i] = 1;
  }

  const model = {
    optimize: "presses",
    opType: "min",
    constraints,
    variables,
    ints,
  };

  const result = solver.Solve(model);
  if (!result.feasible) throw new Error("No feasible solution");
  return result.result;
}

function puzzle2(inputPath) {
  const input = fs.readFileSync(inputPath, "utf-8").trim().split("\n");
  let totalPresses = 0;
  for (const line of input) {
    const { buttonIndices, joltageTarget } = parseMachine(line);
    totalPresses += solveMachineWithILP(joltageTarget, buttonIndices);
  }
  console.log("part 2:", totalPresses);
}

// run
puzzle2("input.txt");
