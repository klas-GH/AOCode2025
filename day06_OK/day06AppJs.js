function run() {

    const fs = require('fs');

    // we read the file synchronously and we split into rows by newline
    const data = fs.readFileSync('input.txt', 'utf8').split(/\r?\n/).filter(Boolean);
    const arrOper = [...data.pop()].map((e,i)=>[i,e]).filter((e,i)=>e[1]!==" ");

    const arr1 = data.map(e=>e.split(/\s+/).filter(Boolean).map(Number));
    let sum = 0;
    let sum2= 0;
  
    
    //test1
    function test1(arr1) {
        
        for (let j=0; j<arr1[0].length; j++) {
            let colTotal= arr1[0][j] ;
            
            for (let i=1; i<arr1.length; i++) {
                if(arrOper[j][1] === '+')
                    colTotal += arr1[i][j];
                else 
                    colTotal *= arr1[i][j];
            }
            sum += colTotal;
        }    
    }
    test1(arr1);

    
    //test2
    function test2(data) {
           for( let z=0; z<arrOper.length; z++){
            
            let [idx, op] = arrOper[z];
            let lastpos = z!==arrOper.length-1? arrOper[z+1][0]-2 : data[0].length-1;
            let colTotal = op ==='+'? 0 :1 ;
            for(let pos = lastpos; pos>= idx; pos--){
                let str = "";
                for (let i=0; i<data.length; i++) {
                    str += data[i][pos];   
                }
                if(op === '+')
                    colTotal += (+str);
                else 
                    colTotal *= (+str);

            } 
            sum2 += colTotal;
        }
    }
    test2(data);

    return [sum, sum2];
}

console.log(run());