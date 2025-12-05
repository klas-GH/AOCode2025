function zero() {

  const fs = require('fs');

  // Read the file synchronously
  const data = fs.readFileSync('input.txt', 'utf8');

  // Split into rows by newline and filter out empty lines
  //const rows = data.split(/\r?\n/).map(s => s.trim()).filter(Boolean);

  //const rows = ['L68', 'L30', 'R48', 'L5', 'R60', 'L55' ,'L1', 'L99','R14', 'L82'];
//const rows =
"11-22,95-115,998-1012,1188511880-1188511890,222220-222224,1698522-1698528,446443-446449,38593856-38593862,565653-565659,824824821-824824827,2121212118-2121212124"
  let sum = 0;
 
  const counts =  data   //rows //data 
         .split(/,/)                           // split by comma
          .map(s => s.trim().split(/-/).map(Number)); // split by dash and convert to numbers
        
   
    
   for (let [L, R] of counts) {
      //sum += countSymmetricInRange(L, R);
      sum += countSymmetricInRange2(L, R);


  }

  function countSymmetricInRange(l, r) {
   	let total = 0;
  	const lenL = String(l).length;
  	const lenR = String(r).length;

  	for (let len = lenL; len <= lenR; len++) {
    		if (len % 2 !== 0) continue; // only even lengths

    		const halfLen = len / 2;
    		const start = Math.pow(10, halfLen - 1); // smallest half (e.g., 1 for 2-digit, 10 for 4-digit)
    		const end = Math.pow(10, halfLen) - 1;   // largest half

    		for (let half = start; half <= end; half++) {
      			const str = String(half).padStart(halfLen, '0');
      			const candidate = Number(str + str);
      			if (candidate >= l && candidate <= r) {
        			total+=candidate;
      			}
    		}
  	}
  	return total;
   }

   function countSymmetricInRange2(l, r) {
  	let total = 0;

  	for (let num = l; num <= r; num++) {
    		const str = String(num);
    		const n = str.length;

    		// check divisors of n
    		for (let d = 1; d <= n / 2; d++) {
      			if (n % d === 0) {
        			const sub = str.slice(0, d);
        			if (sub.repeat(n / d) === str) {
          				total += num; // add invalid ID value
          				break;        // no need to check further
        			}
      			}
    		}
  	}

  	return total;
   }


  return sum;
}

// quick test with the example sequence (uncomment to test without file)
//const testRows = ['L68','L30','R48','L5','R60','L55','L1','L99','R14','L82'];
//require('fs').writeFileSync('input.txt', testRows.join('\n'), 'utf8');
console.log(zero());