function run() {

  const fs = require('fs');

  // we read the file synchronously and we split into rows by newline
  const data = fs.readFileSync('input.txt', 'utf8').split(/\r?\n/).filter(Boolean);

  const arr = data.map(x => x.split(",").map(Number));
  const n = arr.length;

  // squared Euclidean distance
  const dist = (x , y)=>{ 
      const d = Math.pow(y[0] - x[0],2) + Math.pow(y[1] - x[1],2) + Math.pow(y[2] - x[2],2); 
      return d; 
  }

  // --- Build all edges ---
  const edges = [];
  for (let i=0; i<n; i++) {
    for (let j=i+1; j<n; j++) {
      edges.push({i, j, d: dist(arr[i], arr[j])});
    }
  }

  // --- Min-heap implementation ---
  class MinHeap {
    constructor() { this.data = []; }
    push(e) {
      this.data.push(e);
      this._bubbleUp(this.data.length-1);
    }
    pop() {
      if (this.data.length === 0) return null;
      const min = this.data[0];
      const end = this.data.pop();
      if (this.data.length > 0) {
        this.data[0] = end;
        this._sinkDown(0);
      }
      return min;
    }
    _bubbleUp(idx) {
      const e = this.data[idx];
      while (idx > 0) {
        let parent = Math.floor((idx-1)/2);
        if (this.data[parent].d <= e.d) break;
        this.data[idx] = this.data[parent];
        idx = parent;
      }
      this.data[idx] = e;
    }
    _sinkDown(idx) {
      const length = this.data.length;
      const e = this.data[idx];
      while (true) {
        let left = 2*idx+1, right = 2*idx+2;
        let swap = null;
        if (left < length && this.data[left].d < e.d) swap = left;
        if (right < length && this.data[right].d < (swap===null?e.d:this.data[swap].d)) swap = right;
        if (swap===null) break;
        this.data[idx] = this.data[swap];
        idx = swap;
      }
      this.data[idx] = e;
    }
  }

  // --- Fill heap ---
  const heap = new MinHeap();
  for (let e of edges) heap.push(e);

  // --- Answer 1: pop 1000 edges, build graph, compute product of top 3 sizes ---
  const graph = Array.from({length:n}, () => new Set());
  for (let k=0; k<1000; k++) {
    let e = heap.pop();
    if (!e) break; // safety guard
    graph[e.i].add(e.j);
    graph[e.j].add(e.i);
  }

  const visited = new Set();
  const DFSCollect = (x, group=[]) => {
    if (visited.has(x)) 
      return group;
    visited.add(x);
    group.push(x);
    for (let y of graph[x]) 
      DFSCollect(y, group);
    return group;
  };

  const groups = [];
  for (let i=0; i<n; i++) {
    if (!visited.has(i)) 
      groups.push(DFSCollect(i, []));
  }
  const sizes = groups.map(g => g.length).sort((a,b)=>b-a);
  const sum = sizes[0] * sizes[1] * sizes[2];

  // --- Answer 2: continue popping edges until all connected ---
  const parent = Array.from({length:n}, (_,i)=>i);
  const find = (x) => parent[x]===x ? x : parent[x]=find(parent[x]);
  const union = (a,b) => {
    let pa=find(a), pb=find(b);
    if (pa!==pb) parent[pa]=pb;
  };

  let lastEdge = null;
  let roots = new Set();
  // union the first 1000 edges already used
  for (let i=0; i<n; i++) {
    for (let j of graph[i]) union(i,j);
    roots.add(find(i));
  }
  let groupsCount = roots.size; // update groups count

  while (groupsCount > 1) {
    let e = heap.pop();
    if (!e) break; // prevent null error
    if (find(e.i) !== find(e.j)) {
      union(e.i, e.j);
      groupsCount--;
      lastEdge = e;
    }
  }

  if (!lastEdge) {
    throw new Error("No edge found to connect all groups");
  }

  let sum2 = arr[lastEdge.i][0] * arr[lastEdge.j][0];

  return [sum, sum2];
}

console.log(run());