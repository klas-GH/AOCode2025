function run() {

    const fs = require('fs');

    // we read the file synchronously and split into rows by newline
    const data = fs.readFileSync('input.txt', 'utf8').split(/\r?\n/).map(line => line.trim()).filter(Boolean);

    // we split into rows by ":" and then by " "
    const arr = data.map(e=>e.split(/\s*:\s*/)).map(e=>[e[0],e[1].split(/\s+/)]);

    const start = "you";
    const end = "out"
    let count = 0;

    const graph = buildGraph(arr);

    function buildGraph(edges) {
        const graph = new Map();
        for (const [node, neighbors] of edges) {
            graph.set(node, neighbors);
        }
        return graph;
    }

     //test1
    function dfs(start, end) {
        function explore(node) {
            if (node === end) {
                count++;
                return;
            }
            for (let neighbor of (graph.get(node) || [])) {
                explore(neighbor);
            }
        }
        explore(start);
    }
    dfs(start, end);
    let ans1 = count;

    //test2;
    
    function dfs2(start, end) {
        const memo = new Map();

        function explore(node, has1, has2) {
            const k = `${node}|${has1}|${has2}`;
            if (memo.has(k)) return memo.get(k);

            if (node === cond1) has1 = true;
            if (node === cond2) has2 = true;

            // if reached end with both conditions
            if (node === end) {
                const result = (has1 && has2) ? 1 : 0;
                memo.set(k, result);
                return result;
            }

            let total = 0;
            for (const nxt of (graph.get(node) || [])) {
                total += explore(nxt, has1, has2);
            }

            memo.set(k, total);
            return total;
        }

        return explore(start, false, false);
    }
    const start2 = "svr";
    const cond1 = "dac";
    const cond2 = "fft";
    
    let ans2= dfs2(start2, end);; 

    return [ans1, ans2];
}

console.log(run());