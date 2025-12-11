import re
from collections import deque
import copy
import math

EPSILON = 1e-9

# ================= P2 Solver (locked) =================
def parse_machine_instructions_from_split(arr):
    machines = []
    for pattern, button_lists, targets in arr:
        wiring_schematics = [{"wiredButtons": [{"lightIndex": i} for i in bl], "label": ""} for bl in button_lists]
        joltage_requirements = targets
        machines.append({"wiringSchematics": wiring_schematics, "joltageRequirements": joltage_requirements})
    return machines

# --- LP / ILP utilities ---
def deep_copy_constraints(constraints):
    return [{"relation": c["relation"], "rhs": c["rhs"], "coeffs": c["coeffs"][:]} for c in constraints]

def normalize_model(model):
    return {
        "numVars": model["numVars"],
        "objective": {"sense": model.get("objective", {}).get("sense", "min"), "coeffs": model["objective"]["coeffs"][:]},
        "constraints": deep_copy_constraints(model["constraints"])
    }

def clone_model_with_constraint(model, var_index, relation, rhs):
    copy_model = normalize_model(model)
    coeffs = [0]*copy_model["numVars"]
    coeffs[var_index] = 1
    copy_model["constraints"].append({"relation": relation, "rhs": rhs, "coeffs": coeffs})
    return copy_model

# --- Tableau / Simplex ---
class Tableau:
    def __init__(self, model):
        self.nC = len(model["constraints"])
        self.nV = model["numVars"]
        self.basis = [-1]*self.nC
        self.slack = 0
        self.artificial = 0

        for c in model["constraints"]:
            if c["relation"] == "<=": self.slack += 1
            elif c["relation"] == ">=": self.slack += 1; self.artificial += 1
            elif c["relation"] == "=": self.artificial += 1

        self.totalVars = self.nV + self.slack + self.artificial
        self.rhsCol = self.totalVars
        self.artStart = self.nV + self.slack
        self.phase1 = self.artificial > 0

        self.tbl = [[0]*(self.totalVars+1) for _ in range(self.nC+1)]

        sOff = self.nV
        aOff = self.nV + self.slack
        for r, c in enumerate(model["constraints"]):
            for i, v in enumerate(c["coeffs"]): self.tbl[r][i] = v
            if c["relation"] == "<=": self.tbl[r][sOff] = 1; self.basis[r] = sOff; sOff += 1
            elif c["relation"] == ">=": self.tbl[r][sOff] = -1; self.tbl[r][aOff] = 1; self.basis[r] = aOff; aOff += 1; sOff += 1
            elif c["relation"] == "=": self.tbl[r][aOff] = 1; self.basis[r] = aOff; aOff += 1
            self.tbl[r][self.rhsCol] = c["rhs"]

    def pivot(self, r, c):
        w = len(self.tbl[0])
        pv = self.tbl[r][c]
        for col in range(w): self.tbl[r][col] /= pv
        for row in range(len(self.tbl)):
            if row == r: continue
            f = self.tbl[row][c]
            if abs(f) < EPSILON: continue
            for col in range(w): self.tbl[row][col] -= f * self.tbl[r][col]
        self.basis[r] = c

    def simplex(self, col_count):
        rows = len(self.tbl)
        while True:
            enter = -1
            most_neg = -EPSILON
            for c in range(col_count):
                if self.tbl[rows-1][c] < most_neg:
                    most_neg = self.tbl[rows-1][c]
                    enter = c
            if enter == -1: return {"status": "optimal"}

            leave = -1
            best_ratio = float("inf")
            for r in range(rows-1):
                coeff = self.tbl[r][enter]
                if coeff > EPSILON:
                    ratio = self.tbl[r][self.rhsCol]/coeff
                    if ratio < best_ratio - EPSILON:
                        best_ratio = ratio
                        leave = r
            if leave == -1: return {"status": "unbounded"}
            self.pivot(leave, enter)

    def set_objective(self, coeffs, col_count):
        rows = len(self.tbl)
        last = rows-1
        rhsCol = self.rhsCol
        for c in range(col_count): self.tbl[last][c] = -(coeffs[c] if c < len(coeffs) else 0)
        self.tbl[last][rhsCol] = 0
        for r in range(rows-1):
            bv = self.basis[r]
            if bv < 0: continue
            coef = coeffs[bv] if bv < len(coeffs) else 0
            if abs(coef) < EPSILON: continue
            for c in range(rhsCol+1): self.tbl[last][c] += coef*self.tbl[r][c]

    def eliminate_artificial(self):
        for r, v in enumerate(self.basis):
            if v >= self.artStart:
                enter = -1
                for c in range(self.artStart):
                    if abs(self.tbl[r][c]) > EPSILON:
                        enter = c
                        break
                if enter != -1: self.pivot(r, enter)
                else: self.basis[r] = -1

    def remove_artificial_cols(self):
        if self.totalVars == self.artStart: return
        keep = list(range(self.artStart)) + [self.rhsCol]
        self.tbl = [[r[c] for c in keep] for r in self.tbl]
        self.totalVars = self.artStart
        self.rhsCol = self.artStart

    def extract_solution(self, num_vars):
        sol = [0]*num_vars
        for r, v in enumerate(self.basis):
            if 0 <= v < num_vars: sol[v] = self.tbl[r][self.rhsCol]
        return sol

# --- LP / ILP solver ---
def solveLP(model):
    m = normalize_model(model)
    tableau = Tableau(m)
    if tableau.phase1:
        p1 = [0]*tableau.totalVars
        for c in range(tableau.artStart, tableau.totalVars): p1[c] = -1
        tableau.set_objective(p1, tableau.totalVars)
        r1 = tableau.simplex(tableau.totalVars)
        if r1["status"] != "optimal" or abs(tableau.tbl[-1][tableau.rhsCol]) > EPSILON: return {"feasible": False}
        tableau.eliminate_artificial()
        tableau.remove_artificial_cols()
    else:
        for c in range(tableau.rhsCol+1): tableau.tbl[-1][c] = 0
    obj = [0]*tableau.totalVars
    for c in range(m["numVars"]): obj[c] = -(m["objective"]["coeffs"][c] if c < len(m["objective"]["coeffs"]) else 0)
    tableau.set_objective(obj, tableau.totalVars)
    r2 = tableau.simplex(tableau.totalVars)
    if r2["status"] != "optimal": return {"feasible": False}
    return {"feasible": True, "solution": tableau.extract_solution(m["numVars"]), "objective": -tableau.tbl[-1][tableau.rhsCol]}

def find_fractional_index(values):
    idx, max_f = -1, 0
    for i, v in enumerate(values):
        f = abs(v - round(v))
        if f > EPSILON and f > max_f: idx, max_f = i, f
    return idx

def solveIntegerProgram(model):
    best = None
    def branch(m):
        nonlocal best
        lp = solveLP(m)
        if not lp["feasible"]: return
        if best and lp["objective"] >= best["objective"]-EPSILON: return
        fi = find_fractional_index(lp["solution"])
        if fi == -1:
            if not best or lp["objective"] < best["objective"]-EPSILON: best = lp
            return
        v = lp["solution"][fi]
        if math.floor(v) >= 0: branch(clone_model_with_constraint(m, fi, "<=", math.floor(v)))
        branch(clone_model_with_constraint(m, fi, ">=", math.ceil(v)))
    branch(normalize_model(model))
    if not best: return {"feasible": False}
    return {"feasible": True, "solution": best["solution"], "objective": best["objective"]}

def solve_machine_for_joltage(machine):
    t = machine["joltageRequirements"]
    if not t: return {"bestSolution": 0}
    buttons = []
    for s in machine["wiringSchematics"]:
        effect = [0]*len(t)
        for b in s["wiredButtons"]: effect[b["lightIndex"]] = 1
        buttons.append({"effect": effect, "coverage": len(s["wiredButtons"])})
    buttons = [b for b in buttons if b["coverage"] > 0]
    if not buttons: return {"bestSolution": None}
    numButtons = len(buttons)
    constraints = []
    for i, v in enumerate(t):
        constraints.append({"relation": "=", "rhs": v, "coeffs": [b["effect"][i] for b in buttons]})
    for i in range(numButtons):
        coeffs = [1 if i==j else 0 for j in range(numButtons)]
        constraints.append({"relation": ">=", "rhs": 0, "coeffs": coeffs})
    model = {"numVars": numButtons, "objective":{"sense":"min","coeffs":[1]*numButtons}, "constraints": constraints}
    sol = solveIntegerProgram(model)
    if not sol["feasible"]: return {"bestSolution": None}
    return {"bestSolution": sum(sol["solution"])}

# ================= Main integration =================
def run():
    with open("input.txt") as f:
        data = [line.strip() for line in f if line.strip()]
    arr = []
    for line in data:
        part1 = re.match(r"^\[([#.]+)\]", line).group(1)
        part2 = [list(map(int, m.split(","))) for m in re.findall(r"\(([\d,]+)\)", line)]
        part3 = list(map(int, re.search(r"\{([\d,]+)\}$", line).group(1).split(",")))
        arr.append([part1, part2, part3])

    # ---------- Part 1 ----------
    def min_presses(target, buttons, type=1):
        n = len(target)
        target_bits = [1 if c=="#" else 0 for c in target] if type==1 else target
        start = [0]*n
        seen = {",".join(map(str,start)):0}
        queue = deque([start])
        while queue:
            state = queue.popleft()
            steps = seen[",".join(map(str,state))]
            if state == target_bits: return steps
            for btn in buttons:
                new_state = state[:]
                for idx in btn:
                    if type==1: new_state[idx] ^= 1
                    else: new_state[idx] += 1
                if type==2:
                    if any(new_state[i] > target_bits[i] for i in range(n)): continue
                key = ",".join(map(str,new_state))
                if key not in seen:
                    seen[key] = steps+1
                    queue.append(new_state)
    minCount1 = sum(min_presses(target, buttons, 1) for target, buttons, _ in arr)

    # ---------- Part 2 ----------
    machines = parse_machine_instructions_from_split(arr)
    minCount2 = sum(solve_machine_for_joltage(m)["bestSolution"] for m in machines)
     
    return [minCount1, int(minCount2)]

if __name__ == "__main__":
    print(run())
