function run() {

        const fs = require('fs');

        // we read the file synchronously
        const data = fs.readFileSync('input.txt', 'utf8');

        // we split into rows by newline
        const  arr = data.split(/\r?\n/);

    const idx = arr.indexOf("");
    const arrMrg = [];


    function searchNrBN(nr) {
        let lo = 0, hi = arrMrg.length - 1;

        while (lo <= hi) {
            let mid = Math.floor((lo + hi) / 2);
            let [l, r] = arrMrg[mid];

            if (nr < l) {
                hi = mid - 1;   // search left side
            } else if (nr > r) {
                lo = mid + 1;   // search right side
            } else {
                // nr is within [l, r]
                return true;
            }
        }

        return false;
    }

    function pushMergeBinSrch(l, r) {
        // Binary search to find first interval whose end >= l
        let lo = 0, hi = arrMrg.length - 1, pos = arrMrg.length;
        while (lo <= hi) {
            let mid = Math.floor((lo + hi) / 2);
            if (arrMrg[mid][1] >= l) {
                pos = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }

        // If pos is at end or the found interval starts after r, no overlap -> insert at pos
        if (pos === arrMrg.length || arrMrg[pos][0] > r) {
            arrMrg.splice(pos, 0, [l, r]);
            return;
        }

        // There is overlap starting at pos. Merge all overlapping intervals.
        let newL = Math.min(arrMrg[pos][0], l);
        let newR = Math.max(arrMrg[pos][1], r);
        let j = pos + 1;

        // Merge subsequent intervals while they overlap with [newL, newR]
        while (j < arrMrg.length && arrMrg[j][0] <= newR) {
            newR = Math.max(newR, arrMrg[j][1]);
            j++;
        }

        // Replace arrMrg[pos .. j-1] with the merged interval
        arrMrg.splice(pos, j - pos, [newL, newR]);
    }

    // we build merged intervals from arr[0..idx-1]
    for (let i = 0; i < idx; i++) {
        let [l, r] = arr[i].split("-").map(Number);
        pushMergeBinSrch(l, r);
    }


    let sum = 0;
    let sum2= 0;
           
    //test1
    function test1(arr) {
        
        for (let i=idx+1; i<arr.length; i++) {
           sum += searchNrBN( +arr[i])? 1: 0;
        }    
    }
    test1(arr);
    
    //test2
    function test2(arr) {
         for (let i=0; i<arrMrg.length; i++) {
           sum2 += arrMrg[i][1] - arrMrg[i][0] +1;
        }   
    }
    test2(arr);

    return [sum, sum2];
}

console.log(run());