function zero() {

  const fs = require('fs');

  // Read the file synchronously
  const data = fs.readFileSync('input.txt', 'utf8');

  // Split into rows by newline and filter out empty lines
  const rows = data.split(/\r?\n/).map(s => s.trim()).filter(Boolean);

  //const rows = ['L68', 'L30', 'R48', 'L5', 'R60', 'L55' ,'L1', 'L99','R14', 'L82'];
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

// quick test with the example sequence (uncomment to test without file)
//const testRows = ['L68','L30','R48','L5','R60','L55','L1','L99','R14','L82'];
//require('fs').writeFileSync('input.txt', testRows.join('\n'), 'utf8');
console.log(zero());