function run() {

    const fs = require('fs');

    // we read the file synchronously
    const data = fs.readFileSync('input.txt', 'utf8');

    // we split into rows by newline and filter out empty lines
    const arr = data.split(/\r?\n/).map(s => s.trim()).filter(Boolean);

    let sum = 0;
    let sum2 = 0;

    for (let s of arr) {
      sum += test1(s);
      sum2 += test2(s);
    }

    //test1
    function test1(s) {
      const k = 2;
      let toRemove = s.length - k;
      const stack = [];

      for (let digit of s) {
        while (stack.length && toRemove > 0 && stack[stack.length - 1] < digit) {
          stack.pop();
          toRemove--;
        }
        stack.push(digit);
      }
      
      // we ensure exactly 2 digits
      const total = +stack.slice(0, k).join("");

      return total;
    }

    //test2
    function test2(s) {
      const k = 12;
      let toRemove = s.length - k;
      const stack = [];

      for (let digit of s) {
        while (stack.length && toRemove > 0 && stack[stack.length - 1] < digit) {
          stack.pop();
          toRemove--;
        }
        stack.push(digit);
      }

      // we ensure exactly 12 digits
      const total = +stack.slice(0, k).join("");

      return total;
    }


    return [sum, sum2];
}

console.log(run());