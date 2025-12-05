function run() {

  const fs = require('fs');

  // we read the file synchronously
  const data = fs.readFileSync('input.txt', 'utf8');

  // we split into rows by newline and filter out empty lines
  const rows = data.split(/\r?\n/).map(s => s.trim()).filter(Boolean);

  //test1
  function test1(){
    let pos = 50; // initial position
    let count = 0;
    for (let val of rows) {
      
      const dir = val[0];
      const nr = Math.abs(+val.slice(1));

      if (dir === 'R') {
        pos = (pos + nr) % 100;
      } else {
        pos = (100 + pos - nr) % 100;
      }

      if(pos === 0)
        count++;
    } 
    return count;
  }

  //test2
  function test2() {
    let count = 0;
    let pos = 50; // initial position

    for (let val of rows) {
      const dir = val[0];
      const nr = Math.abs(+val.slice(1));

      let stepsToFirstZero;
      if (dir === 'R') {
        stepsToFirstZero = (pos === 0 ? 100 : 100 - pos);
        if (nr >= stepsToFirstZero) {
          count += Math.floor((nr - stepsToFirstZero) / 100) + 1;
        }
        pos = (pos + nr) % 100;
      } else {
        stepsToFirstZero = (pos === 0 ? 100 : pos);
        if (nr >= stepsToFirstZero) {
          count += Math.floor((nr - stepsToFirstZero) / 100) + 1;
        }
        pos = ((pos - nr) % 100 + 100) % 100;
      }
    }

    return count;
  }

  let sum = test1();
  let sum2 = test2();
  return [sum, sum2];
}

console.log(run());