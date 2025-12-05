const fs = require('fs').promises;

async function zero() {
  try {
    // Try reading the file
    const data = await fs.readFile('input.txt', 'utf8');

    // Split into rows by newline and filter out empty lines
    const rows = data.split(/\r?\n/).map(s => s.trim()).filter(Boolean);

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
  } catch (err) {
    // Equivalent to handling !response.ok in fetch
    console.error('Error reading file:', err.message);
    return null; // or throw err if you want to propagate
  }
}

// Example usage
(async () => {
  const result = await zero();
  if (result !== null) {
    console.log('Total zero crossings:', result);
  } else {
    console.log('Failed to compute zero crossings.');
  }
})();
