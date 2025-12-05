function run() {

    const fs = require('fs');

    // we read the file synchronously
    const data = fs.readFileSync('input.txt', 'utf8');

    // we split into rows by newline and filter out empty lines
    let  arr = data.split(/\r?\n/).map(s => s.trim()).filter(Boolean).map(e=>e.split(""));
    const DIR = [[1, 0], [-1, 0], [1, 1], [-1, 1], [0, 1], [0, -1], [-1, -1], [1, -1]];

    let sum = 0;
    let sum2= 0;
    const queue = [];
    
    function isValid(x, y) {

        if (x < 0 || x >= arr.length || y < 0 || y >= arr[0].length || arr[x][y] !== '@') return false;
        
        let count = 0;
        for (let [dx, dy] of DIR) {
            let nx = x + dx;
            let ny = y + dy;
            if (nx < 0 || nx >= arr.length || ny < 0 || ny >= arr[0].length || arr[nx][ny] !== '@')
                continue;
            count++;
        }

        return count < 4;
    }
            
    //test1
    function test1(arr) {
        
        for (let i=0; i<arr.length; i++) {
            for (let j=0; j<arr[0].length; j++) {
                        if(arr[i][j]!=='@') continue;
                        if(isValid(i,j)){
                            sum++;
                            queue.push([i,j]);
                        }               
            }
        }    
    }
    test1(arr);
    
    //test2
    function test2(arr) {
        const visited = new Set();
        let idx = 0;

        while (idx < queue.length) {
            const [i, j] = queue[idx++];

            // we skip if already visited
            const key = `${i}#${j}`;
            if (visited.has(key)) continue;
            visited.add(key);

            // If valid '@', we remove it
            if (arr[i][j] === '@' && isValid(i, j)) {
                arr[i][j] = ".";
                sum2++;

                // we add neighbors to queue
                for (let [dx, dy] of DIR) {
                    const nx = i + dx, ny = j + dy;
                    
                    if (isValid(nx, ny)) {
                        queue.push([nx, ny]);
                    }
                    
                }
            }
        }
    }
    test2(arr);

    return [sum, sum2];
}

console.log(run());