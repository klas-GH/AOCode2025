#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <sstream>
#include <queue>
#include <unordered_map>
#include <cmath>
#include <algorithm>
#include <map>
#include <tuple>
#include <functional>

using namespace std;

const double EPSILON = 1e-9;

// -------------------- Data Structures --------------------
struct WiringButton {
    int lightIndex;
    WiringButton(int idx) : lightIndex(idx) {}
};

struct WiringScheme {
    vector<WiringButton> wiredButtons;
    WiringScheme(const vector<WiringButton>& buttons) : wiredButtons(buttons) {}
};

struct Machine {
    vector<WiringScheme> wiringSchematics;
    vector<int> joltageRequirements;
    Machine(const vector<WiringScheme>& schematics, const vector<int>& requirements)
        : wiringSchematics(schematics), joltageRequirements(requirements) {}
};

// -------------------- LP / ILP Structures --------------------
struct Constraint {
    string relation; // "=", ">=", "<="
    double rhs;
    vector<double> coeffs;
    Constraint(const string& rel, double r, const vector<double>& c) : relation(rel), rhs(r), coeffs(c) {}
};

struct LPModel {
    int numVars;
    vector<double> objective;
    vector<Constraint> constraints;
    LPModel(int n, const vector<double>& obj, const vector<Constraint>& cons)
        : numVars(n), objective(obj), constraints(cons) {}
};

struct Tableau {
    int nC, nV, totalVars, rhsCol, artStart;
    vector<vector<double>> tbl;
    vector<int> basis;
    bool phase1;
    int slack = 0, artificial = 0;

    Tableau(LPModel& model) {
        nC = model.constraints.size();
        nV = model.numVars;
        basis.assign(nC, -1);

        for (auto& c : model.constraints) {
            if (c.relation == "<=") slack++;
            else if (c.relation == ">=") { slack++; artificial++; }
            else if (c.relation == "=") artificial++;
        }

        totalVars = nV + slack + artificial;
        rhsCol = totalVars;
        artStart = nV + slack;
        phase1 = artificial > 0;

        tbl.assign(nC + 1, vector<double>(totalVars + 1, 0.0));

        int sOff = nV, aOff = nV + slack;
        for (int r = 0; r < nC; r++) {
            auto& c = model.constraints[r];
            for (int i = 0; i < c.coeffs.size(); i++) tbl[r][i] = c.coeffs[i];

            if (c.relation == "<=") { tbl[r][sOff] = 1; basis[r] = sOff++; }
            else if (c.relation == ">=") { tbl[r][sOff] = -1; tbl[r][aOff] = 1; basis[r] = aOff++; sOff++; }
            else if (c.relation == "=") { tbl[r][aOff] = 1; basis[r] = aOff++; }

            tbl[r][rhsCol] = c.rhs;
        }
    }

    void pivot(int r, int c) {
        double pv = tbl[r][c];
        int w = tbl[0].size();
        for (int col = 0; col < w; col++) tbl[r][col] /= pv;
        for (int row = 0; row < tbl.size(); row++) {
            if (row == r) continue;
            double f = tbl[row][c];
            if (fabs(f) < EPSILON) continue;
            for (int col = 0; col < w; col++) tbl[row][col] -= f * tbl[r][col];
        }
        basis[r] = c;
    }

    map<string,string> simplex(int colCount) {
        int rows = tbl.size();
        while (true) {
            int enter = -1;
            double mostNeg = -EPSILON;
            for (int c = 0; c < colCount; c++)
                if (tbl[rows-1][c] < mostNeg) { mostNeg = tbl[rows-1][c]; enter = c; }
            if (enter == -1) return {{"status","optimal"}};

            int leave = -1;
            double bestRatio = 1e18;
            for (int r = 0; r < rows-1; r++) {
                double coeff = tbl[r][enter];
                if (coeff > EPSILON) {
                    double ratio = tbl[r][rhsCol] / coeff;
                    if (ratio < bestRatio - EPSILON) { bestRatio = ratio; leave = r; }
                }
            }
            if (leave == -1) return {{"status","unbounded"}};
            pivot(leave, enter);
        }
    }

    void setObjective(const vector<double>& coeffs, int colCount) {
        int rows = tbl.size();
        int last = rows-1;
        fill(tbl[last].begin(), tbl[last].end(), 0.0);
        for (int c=0; c<colCount; c++) tbl[last][c] = -(c<coeffs.size() ? coeffs[c] : 0.0);
        for (int r=0; r<rows-1; r++) {
            int bv = basis[r];
            if (bv<0 || bv>=coeffs.size()) continue;
            double coef = coeffs[bv];
            if (fabs(coef)<EPSILON) continue;
            for (int c=0; c<=rhsCol; c++) tbl[last][c] += coef*tbl[r][c];
        }
    }

    void eliminateArtificial() {
        for (int r=0; r<basis.size(); r++) {
            int v = basis[r];
            if (v >= artStart) {
                int enter = -1;
                for (int c=0; c<artStart; c++)
                    if (fabs(tbl[r][c])>EPSILON) { enter=c; break; }
                if (enter != -1) pivot(r, enter);
                else basis[r] = -1;
            }
        }
    }

    void removeArtificialCols() {
        if (totalVars == artStart) return;
        vector<int> keep(artStart+1);
        for (int i=0; i<artStart; i++) keep[i]=i;
        keep[artStart]=rhsCol;
        vector<vector<double>> newTbl(tbl.size(), vector<double>(artStart+1,0.0));
        for (int r=0; r<tbl.size(); r++)
            for (int i=0; i<keep.size(); i++)
                newTbl[r][i] = tbl[r][keep[i]];
        tbl = newTbl;
        totalVars = artStart;
        rhsCol = artStart;
    }

    vector<double> extractSolution(int numVars) {
        vector<double> sol(numVars,0.0);
        for (int r=0; r<basis.size(); r++) {
            int v = basis[r];
            if (v>=0 && v<numVars) sol[v] = tbl[r][rhsCol];
        }
        return sol;
    }
};

// -------------------- Helper ILP Functions --------------------
LPModel cloneModelWithConstraint(const LPModel& model, int varIndex, const string& relation, double rhs) {
    vector<Constraint> newConstraints = model.constraints;
    vector<double> coeffs(model.numVars,0.0);
    coeffs[varIndex]=1.0;
    newConstraints.emplace_back(relation,rhs,coeffs);
    return LPModel(model.numVars, model.objective, newConstraints);
}

int findFractionalIndex(const vector<double>& values) {
    int idx=-1;
    double maxF=0.0;
    for (int i=0; i<values.size(); i++) {
        double f = fabs(values[i]-round(values[i]));
        if (f>EPSILON && f>maxF) { maxF=f; idx=i; }
    }
    return idx;
}

bool solveLP(LPModel& model, vector<double>& solution, double& objective) {
    Tableau tableau(model);
    if (tableau.phase1) {
        vector<double> p1(tableau.totalVars,0.0);
        for (int c=tableau.artStart; c<tableau.totalVars; c++) p1[c]=-1.0;
        tableau.setObjective(p1, tableau.totalVars);
        auto r1 = tableau.simplex(tableau.totalVars);
        if (r1["status"] != "optimal" || fabs(tableau.tbl.back()[tableau.rhsCol])>EPSILON)
            return false;
        tableau.eliminateArtificial();
        tableau.removeArtificialCols();
    }

    vector<double> obj(tableau.totalVars,0.0);
    for (int c=0; c<model.numVars; c++) obj[c] = -model.objective[c];
    tableau.setObjective(obj, tableau.totalVars);
    auto r2 = tableau.simplex(tableau.totalVars);
    if (r2["status"] != "optimal") return false;
    solution = tableau.extractSolution(model.numVars);
    objective = -tableau.tbl.back()[tableau.rhsCol];
    return true;
}

bool solveIntegerProgram(LPModel& model, vector<double>& solution, double& objective) {
    vector<double> bestSol;
    double bestObj = 1e18;
    bool feasible = false;

    function<void(LPModel&)> branch = [&](LPModel& m) {
        vector<double> sol;
        double obj;
        if (!solveLP(m, sol, obj)) return;
        if (feasible && obj >= bestObj - EPSILON) return;
        int fi = findFractionalIndex(sol);
        if (fi==-1) {
            bestSol = sol;
            bestObj = obj;
            feasible = true;
            return;
        }
        double v = sol[fi];
        if (floor(v)>=0) { auto m1=cloneModelWithConstraint(m,fi,"<=",floor(v)); branch(m1); }
        auto m2=cloneModelWithConstraint(m,fi,">=",ceil(v)); branch(m2);
    };

    branch(model);
    if (!feasible) return false;
    solution = bestSol;
    objective = bestObj;
    return true;
}

// -------------------- Solve Machine --------------------
int safeRound(double x) {
    const double EPS = 1e-6;
    return static_cast<int>(x + EPS);
}

int solveMachineForJoltage(const Machine& m) {
    const vector<int>& t = m.joltageRequirements;
    if (t.empty()) return 0;

    vector<vector<int>> buttonsEffects;
    for (auto& ws : m.wiringSchematics) {
        vector<int> eff(t.size(), 0);
        for (auto& b : ws.wiredButtons)
            if (b.lightIndex >= 0 && b.lightIndex < t.size()) eff[b.lightIndex] = 1;
        buttonsEffects.push_back(eff);
    }
    if (buttonsEffects.empty()) return -1;

    int numButtons = buttonsEffects.size();
    vector<Constraint> constraints;
    for (size_t i = 0; i < t.size(); i++) {
        vector<double> coeffs(numButtons, 0);
        for (int j = 0; j < numButtons; j++) coeffs[j] = buttonsEffects[j][i];
        constraints.push_back({"=", static_cast<double>(t[i]), coeffs});
    }
    for (int i = 0; i < numButtons; i++) {
        vector<double> coeffs(numButtons, 0); coeffs[i] = 1;
        constraints.push_back({">=", 0.0, coeffs});
    }

    vector<double> objective(numButtons, 1.0);
    LPModel model{numButtons, objective, constraints};

    vector<double> solution;
    double obj;
    if (!solveIntegerProgram(model, solution, obj)) return -1;

    int sum = 0;
    for (double d : solution) sum += safeRound(d);
    return sum;
}

// -------------------- BFS --------------------
int minPresses(const string& target, const vector<vector<int>>& buttons) {
    int n = target.size();
    vector<int> targetBits(n);
    for (int i=0;i<n;i++) targetBits[i]=(target[i]=='#'?1:0);

    vector<int> start(n,0);
    unordered_map<string,int> seen;
    auto vecToStr = [](const vector<int>& v){ string s; for(int x:v) s+=(x?'1':'0'); return s; };
    queue<vector<int>> q;
    q.push(start);
    seen[vecToStr(start)]=0;

    while(!q.empty()) {
        auto state = q.front(); q.pop();
        int steps = seen[vecToStr(state)];
        if(state==targetBits) return steps;
        for(auto& btn: buttons) {
            vector<int> newState=state;
            for(int idx: btn) if(idx>=0 && idx<n) newState[idx]^=1;
            string key = vecToStr(newState);
            if(!seen.count(key)) { seen[key]=steps+1; q.push(newState); }
        }
    }
    return -1;
}

// -------------------- Main --------------------
int main() {
    ifstream fin("input.txt");
    vector<string> data;
    string line;
    while(getline(fin,line)) if(!line.empty()) data.push_back(line);

    vector<tuple<string, vector<vector<int>>, vector<int>>> arr;
    for(auto& line: data) {
        if(line.empty()) continue;
        size_t p1=line.find(']');
        string target=line.substr(1,p1-1);

        vector<vector<int>> buttons;
        size_t pos=0;
        while((pos=line.find('(',pos))!=string::npos) {
            size_t end=line.find(')',pos);
            string inside=line.substr(pos+1,end-pos-1);
            vector<int> btn; stringstream ss(inside); string tok;
            while(getline(ss,tok,',')) btn.push_back(stoi(tok));
            buttons.push_back(btn);
            pos=end+1;
        }

        size_t p2=line.find('{');
        vector<int> joltages;
        if(p2!=string::npos) {
            string inside=line.substr(p2+1,line.size()-p2-2);
            stringstream ss(inside); string tok;
            while(getline(ss,tok,',')) joltages.push_back(stoi(tok));
        }
        arr.emplace_back(target,buttons,joltages);
    }

    // Part1
    int minCount1=0;
    for(auto& e: arr) {
        string t; vector<vector<int>> b; vector<int> j;
        tie(t,b,j)=e;
        minCount1 += minPresses(t,b);
    }

    // Part2
    int minCount2=0;
    for(auto& e: arr) {
        string t; vector<vector<int>> b; vector<int> j;
        tie(t,b,j)=e;
        vector<WiringScheme> schematics;
        for(auto& btn: b) {
            vector<WiringButton> wb; for(int idx: btn) wb.push_back(WiringButton(idx));
            schematics.push_back(WiringScheme(wb));
        }
        Machine m(schematics,j);
        int res = solveMachineForJoltage(m);
        if(res<0) { cout<<"Warning: Machine cannot be solved!"<<endl; continue; }
        minCount2 += res;
    }

    cout << "Part1: " << minCount1 << endl;
    cout << "Part2: " << minCount2 << endl;

    return 0;
}
