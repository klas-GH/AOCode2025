function run() {

    const fs = require('fs');

    // we read the file synchronously and we split into rows by newline
    const data = fs.readFileSync('input.txt', 'utf8').split(/\r?\n/).filter(Boolean);

    const grid = data;
    let sum = 0;
    let sum2 = 0;
    const m = grid.length;
    const n = grid[0].length;
    const sourceIdx = grid[0].indexOf('S');
    
    //test1
    function test1(grid) {

      // we find source column in row 0
      let beams = new Set();
      beams.add(sourceIdx);

      // we iterate through rows 1..m-1
      for (let r = 1; r < m; r++) {
        const newBeams = new Set();

        for (const c of beams) {
          if (grid[r][c] === '^') {
            // Encounter
            sum++;
            // Split into left and right beams
            if (c - 1 >= 0) newBeams.add(c - 1);
            if (c + 1 < n) newBeams.add(c + 1);
          } else {
            // Beam continues straight down
            newBeams.add(c);
          }
        }

        beams = newBeams; // update for next row
      }
    } 
    test1(grid);

    //test2
    function test2(data) { 
      let paths = Array(n).fill(0);
      paths[sourceIdx] = 1
      let beams = new Set();
      beams.add(sourceIdx);

      for (let r = 1; r < m; r++) {
        const newBeams = new Set();

        for (const c of beams) {
          if (grid[r][c] === '^') {
            //  Encounter we split into left and right beams
            if (c - 1 >= 0) {
              paths[c - 1] += paths[c] ;
              newBeams.add(c-1);
            }
            if (c + 1 < n) {
                paths[c + 1] += paths[c] ;
                newBeams.add(c+1);
            }
            paths[c] = 0;
          }  else {
            // Beam continues straight down
            newBeams.add(c);
          }
        }

        beams = newBeams; // update for next row
     }

      // Total timelines = sum of paths in last row
      sum2 = paths.reduce((a, b) => a + b, 0);
    }
    test2(data);

    return [sum, sum2];
}

console.log(run());