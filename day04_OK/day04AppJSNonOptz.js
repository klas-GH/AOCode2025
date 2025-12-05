function run() {

    const fs = require('fs');

    // we read the file synchronously
    const data = fs.readFileSync('input.txt', 'utf8');

    // we split into rows by newline and filter out empty lines
    const rows = data.split(/\r?\n/).map(s => s.trim()).filter(Boolean);
    let arr =  rows.map(e=>e.split(""));


    let sum = 0;
    let sum2= 0;
    
    const isValid = (x, y) =>{
        const DIR = [[1,0],[-1,0],[1,1],[-1,1],[0,1],[0,-1],[-1,-1],[1,-1]];
        let count = 0;
        for(let[dx, dy] of DIR){
            let nx = x + dx;
            let ny = y + dy;
            if(nx<0 || nx>=arr.length || ny<0 || ny>= arr[0].length || arr[nx][ny]!=='@')
                    continue;
            count++;
        }
        
        return count<4;
    }
            
    //test1
    function test1(arr) {
        
        for (let i=0; i<arr.length; i++) {
            for (let j=0; j<arr[0].length; j++) {
                        if(arr[i][j]!=='@') continue;
                        if(isValid(i,j))
                            sum++;
            }
        }
        
    }

    test1(arr);
    
    //test2
    let found = true;
    let arr2 = null;

    function test2(arr) {
        
        let sum22 = 0;
        found = false;
        
        for (let i=0; i<arr.length; i++) {
            for (let j=0; j<arr[0].length; j++) {
                        if(arr[i][j]!=='@') continue;
                        if(isValid(i,j)){
                            sum22++;
                            arr2[i][j]=".";
                        }              
            }

        }
        if(sum22)
            found = true;
        sum2 += sum22;
    }
    while(found){
        arr2 =  structuredClone(arr);
        test2(arr);
        arr =  structuredClone(arr2);
    }
    test2(arr);

    return [sum, sum2];
}


console.log(run());