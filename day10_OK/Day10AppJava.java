import java.io.*;
import java.util.*;
import java.util.regex.*;
@SuppressWarnings("unchecked")
public class Day10AppJava {

    private static final double EPSILON = 1e-9;

    // -------------------- Data Structures --------------------
    static class WiringButton {
        int lightIndex;
        WiringButton(int idx) { lightIndex = idx; }
    }

    static class WiringScheme {
        List<WiringButton> wiredButtons;
        String label = "";
        WiringScheme(List<WiringButton> buttons) { wiredButtons = buttons; }
    }

    static class Machine {
        List<WiringScheme> wiringSchematics;
        List<Integer> joltageRequirements;
        Machine(List<WiringScheme> schematics, List<Integer> requirements) {
            wiringSchematics = schematics;
            joltageRequirements = requirements;
        }
    }

    static class Constraint {
        String relation;
        double rhs;
        double[] coeffs;
        Constraint(String relation, double rhs, double[] coeffs) {
            this.relation = relation;
            this.rhs = rhs;
            this.coeffs = coeffs;
        }
    }

    static class LPModel {
        int numVars;
        double[] objective;
        List<Constraint> constraints;
        LPModel(int numVars, double[] objective, List<Constraint> constraints) {
            this.numVars = numVars;
            this.objective = objective;
            this.constraints = constraints;
        }
    }

    // -------------------- ILP Solver --------------------
    static class Tableau {
        int nC, nV, totalVars, rhsCol, artStart;
        double[][] tbl;
        int[] basis;
        boolean phase1;
        int slack = 0, artificial = 0;

        Tableau(LPModel model) {
            nC = model.constraints.size();
            nV = model.numVars;
            basis = new int[nC];
            Arrays.fill(basis, -1);

            for (Constraint c : model.constraints) {
                if (c.relation.equals("<=")) slack++;
                else if (c.relation.equals(">=")) { slack++; artificial++; }
                else if (c.relation.equals("=")) artificial++;
            }

            totalVars = nV + slack + artificial;
            rhsCol = totalVars;
            artStart = nV + slack;
            phase1 = artificial > 0;
            tbl = new double[nC + 1][totalVars + 1];

            int sOff = nV, aOff = nV + slack;
            for (int r = 0; r < nC; r++) {
                Constraint c = model.constraints.get(r);
                for (int i = 0; i < c.coeffs.length; i++) tbl[r][i] = c.coeffs[i];

                if (c.relation.equals("<=")) { tbl[r][sOff] = 1; basis[r] = sOff++; }
                else if (c.relation.equals(">=")) { tbl[r][sOff] = -1; tbl[r][aOff] = 1; basis[r] = aOff++; sOff++; }
                else if (c.relation.equals("=")) { tbl[r][aOff] = 1; basis[r] = aOff++; }

                tbl[r][rhsCol] = c.rhs;
            }
        }

        void pivot(int r, int c) {
            double pv = tbl[r][c];
            int w = tbl[0].length;
            for (int col = 0; col < w; col++) tbl[r][col] /= pv;
            for (int row = 0; row < tbl.length; row++) {
                if (row == r) continue;
                double f = tbl[row][c];
                if (Math.abs(f) < EPSILON) continue;
                for (int col = 0; col < w; col++) tbl[row][col] -= f * tbl[r][col];
            }
            basis[r] = c;
        }

        Map<String, String> simplex(int colCount) {
            int rows = tbl.length;
            while (true) {
                int enter = -1;
                double mostNeg = -EPSILON;
                for (int c = 0; c < colCount; c++) if (tbl[rows - 1][c] < mostNeg) { mostNeg = tbl[rows - 1][c]; enter = c; }
                if (enter == -1) return Map.of("status","optimal");

                int leave = -1;
                double bestRatio = Double.POSITIVE_INFINITY;
                for (int r = 0; r < rows - 1; r++) {
                    double coeff = tbl[r][enter];
                    if (coeff > EPSILON) {
                        double ratio = tbl[r][rhsCol]/coeff;
                        if (ratio < bestRatio - EPSILON) { bestRatio = ratio; leave = r; }
                    }
                }
                if (leave == -1) return Map.of("status","unbounded");
                pivot(leave, enter);
            }
        }

        void setObjective(double[] coeffs, int colCount) {
            int rows = tbl.length, last = rows-1;
            Arrays.fill(tbl[last],0);
            for (int c=0;c<colCount;c++) tbl[last][c] = -(c<coeffs.length ? coeffs[c] : 0);
            for (int r=0;r<rows-1;r++) {
                int bv = basis[r];
                if (bv<0 || bv>=coeffs.length) continue;
                double coef = coeffs[bv];
                if (Math.abs(coef)<EPSILON) continue;
                for (int c=0;c<=rhsCol;c++) tbl[last][c] += coef*tbl[r][c];
            }
        }

        void eliminateArtificial() {
            for (int r=0;r<basis.length;r++) {
                int v = basis[r];
                if (v>=artStart) {
                    int enter=-1;
                    for (int c=0;c<artStart;c++) if (Math.abs(tbl[r][c])>EPSILON) { enter=c; break; }
                    if (enter!=-1) pivot(r,enter); else basis[r]=-1;
                }
            }
        }

        void removeArtificialCols() {
            if (totalVars==artStart) return;
            int[] keep = new int[artStart+1];
            for (int i=0;i<artStart;i++) keep[i]=i;
            keep[artStart]=rhsCol;
            double[][] newTbl = new double[tbl.length][artStart+1];
            for (int r=0;r<tbl.length;r++) for (int i=0;i<keep.length;i++) newTbl[r][i]=tbl[r][keep[i]];
            tbl=newTbl; totalVars=artStart; rhsCol=artStart;
        }

        double[] extractSolution(int numVars) {
            double[] sol = new double[numVars];
            for (int r=0;r<basis.length;r++) {
                int v = basis[r];
                if (v>=0 && v<numVars) sol[v]=tbl[r][rhsCol];
            }
            return sol;
        }
    }

    static LPModel cloneModelWithConstraint(LPModel model,int varIndex,String relation,double rhs){
        List<Constraint> newConstraints = new ArrayList<>();
        for (Constraint c : model.constraints) newConstraints.add(new Constraint(c.relation,c.rhs,c.coeffs.clone()));
        double[] coeffs = new double[model.numVars];
        coeffs[varIndex]=1;
        newConstraints.add(new Constraint(relation,rhs,coeffs));
        return new LPModel(model.numVars, model.objective.clone(), newConstraints);
    }

    static Map<String,Object> solveLP(LPModel model){
        LPModel m = model;
        Tableau tableau = new Tableau(m);
        if (tableau.phase1) {
            double[] p1 = new double[tableau.totalVars];
            for (int c=tableau.artStart;c<tableau.totalVars;c++) p1[c]=-1;
            tableau.setObjective(p1, tableau.totalVars);
            Map<String,String> r1 = tableau.simplex(tableau.totalVars);
            if (!r1.get("status").equals("optimal") || Math.abs(tableau.tbl[tableau.tbl.length-1][tableau.rhsCol])>EPSILON) return Map.of("feasible",false);
            tableau.eliminateArtificial();
            tableau.removeArtificialCols();
        }
        double[] obj = new double[tableau.totalVars];
	for (int c = 0; c < m.numVars; c++)
    		obj[c] = -m.objective[c];   // JS-compatible
	tableau.setObjective(obj, tableau.totalVars);



        Map<String,String> r2 = tableau.simplex(tableau.totalVars);
        if (!r2.get("status").equals("optimal")) return Map.of("feasible",false);
        return Map.of("feasible",true,"solution",tableau.extractSolution(m.numVars),"objective",-tableau.tbl[tableau.tbl.length-1][tableau.rhsCol]);
    }

    static int findFractionalIndex(double[] values){
        int idx=-1; double maxF=0;
        for (int i=0;i<values.length;i++){
            double f=Math.abs(values[i]-Math.round(values[i]));
            if (f>EPSILON && f>maxF){ maxF=f; idx=i; }
        }
        return idx;
    }

    static Map<String,Object> solveIntegerProgram(LPModel model){
        final Map<String,Object>[] best = new Map[]{null};

        class Branch {
            void run(LPModel m){
                Map<String,Object> lp = solveLP(m);
                if (!(Boolean)lp.get("feasible")) return;
                double obj = (double) lp.get("objective");
                if (best[0]!=null && obj>= (double)best[0].get("objective") - EPSILON) return;
                double[] sol = (double[]) lp.get("solution");
                int fi = findFractionalIndex(sol);
                if (fi==-1) {
                    if (best[0]==null || obj<(double)best[0].get("objective") - EPSILON) best[0]=lp;
                    return;
                }
                double v=sol[fi];
                if (Math.floor(v)>=0) run(cloneModelWithConstraint(m,fi,"<=",Math.floor(v)));
                run(cloneModelWithConstraint(m,fi,">=",Math.ceil(v)));
            }
        }

        new Branch().run(model);
        if (best[0]==null) return Map.of("feasible",false);
        return best[0];
    }

    static int solveMachineForJoltage(Machine machine){
        List<Integer> t = machine.joltageRequirements;
        if (t==null || t.isEmpty()) return 0;

        List<int[]> buttons = new ArrayList<>();
        for (WiringScheme s: machine.wiringSchematics){
            int[] effect = new int[t.size()];
            for (WiringButton b: s.wiredButtons) effect[b.lightIndex]=1;
            buttons.add(effect);
        }
        if (buttons.isEmpty()) return -1;

        int numButtons = buttons.size();
        List<Constraint> constraints = new ArrayList<>();
        for (int i=0;i<t.size();i++){
            double[] coeffs = new double[numButtons];
            for (int j=0;j<numButtons;j++) coeffs[j]=buttons.get(j)[i];
            constraints.add(new Constraint("=",t.get(i),coeffs));
        }
        for (int i=0;i<numButtons;i++){
            double[] coeffs = new double[numButtons];
            coeffs[i]=1;
            constraints.add(new Constraint(">=",0,coeffs));
        }
        double[] objective = new double[numButtons];
        Arrays.fill(objective,1);
        LPModel model = new LPModel(numButtons,objective,constraints);
        Map<String,Object> sol = solveIntegerProgram(model);
        if (!(Boolean)sol.get("feasible")) return -1;
        double[] solution = (double[]) sol.get("solution");
        int sum=0;
        for (double d: solution) sum+=Math.round(d);
        return sum;
    }

    // -------------------- Input Parser --------------------
    static List<Object[]> parseInput(List<String> data) {
        List<Object[]> arr = new ArrayList<>();
        Pattern pattern1 = Pattern.compile("^\\[([#.]+)\\]");
        Pattern pattern2 = Pattern.compile("\\(([\\d,]+)\\)");
        Pattern pattern3 = Pattern.compile("\\{([\\d,]+)\\}$");

        for (String line : data) {
            Matcher m1 = pattern1.matcher(line);
            if (!m1.find()) continue;
            String part1 = m1.group(1);

            Matcher m2 = pattern2.matcher(line);
            List<List<Integer>> part2 = new ArrayList<>();
            while (m2.find()) {
                String[] nums = m2.group(1).split(",");
                List<Integer> tmp = new ArrayList<>();
                for (String s : nums) tmp.add(Integer.parseInt(s));
                part2.add(tmp);
            }

            Matcher m3 = pattern3.matcher(line);
            List<Integer> part3 = new ArrayList<>();
            if (m3.find()) {
                String[] nums = m3.group(1).split(",");
                for (String s : nums) part3.add(Integer.parseInt(s));
            }

            arr.add(new Object[]{part1, part2, part3});
        }
        return arr;
    }

    static List<Machine> parseMachineInstructionsFromSplit(List<Object[]> arr) {
        List<Machine> machines = new ArrayList<>();
        for (Object[] parts : arr) {
            @SuppressWarnings("unchecked")
            List<List<Integer>> buttonLists = (List<List<Integer>>) parts[1];
            @SuppressWarnings("unchecked")
            List<Integer> targets = (List<Integer>) parts[2];
            List<WiringScheme> schematics = new ArrayList<>();
            for (List<Integer> bl : buttonLists) {
                List<WiringButton> wb = new ArrayList<>();
                for (Integer i : bl) wb.add(new WiringButton(i));
                schematics.add(new WiringScheme(wb));
            }
            machines.add(new Machine(schematics, targets));
        }
        return machines;
    }

    // -------------------- BFS --------------------
    static int minPresses(String target, List<List<Integer>> buttons) {
        int n = target.length();
        int[] targetBits = new int[n];
        for (int i = 0; i < n; i++) targetBits[i] = target.charAt(i) == '#' ? 1 : 0;

        int[] start = new int[n];
        Map<String, Integer> seen = new HashMap<>();
        seen.put(Arrays.toString(start), 0);
        Queue<int[]> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            int[] state = queue.poll();
            int steps = seen.get(Arrays.toString(state));
            if (Arrays.equals(state, targetBits)) return steps;
            for (List<Integer> btn : buttons) {
                int[] newState = Arrays.copyOf(state, n);
                for (int idx : btn) newState[idx] ^= 1;
                String key = Arrays.toString(newState);
                if (!seen.containsKey(key)) {
                    seen.put(key, steps + 1);
                    queue.add(newState);
                }
            }
        }
        return -1;
    }

    public static void main(String[] args) throws IOException {
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while ((line = br.readLine()) != null) if (!line.isEmpty()) data.add(line);
        }

        List<Object[]> arr = parseInput(data);

        // Part 1
        int minCount1 = 0;
        for (Object[] entry : arr) {
            String target = (String) entry[0];
            @SuppressWarnings("unchecked")
            List<List<Integer>> buttons = (List<List<Integer>>) entry[1];
            minCount1 += minPresses(target, buttons);
        }

        // Part 2
        List<Machine> machines = parseMachineInstructionsFromSplit(arr);
        int minCount2 = 0;
        for (Machine m : machines) {
            int res = solveMachineForJoltage(m);
            if (res < 0) throw new RuntimeException("Machine cannot be configured");
            minCount2 += res;
        }

        System.out.println("[" + minCount1 + ", " + minCount2 + "]");
    }
}
