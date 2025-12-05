function zero() {

  const fs = require('fs');

  // Read the file synchronously
  const data = fs.readFileSync('input.txt', 'utf8');

  // Split into rows by newline and filter out empty lines
  const rows = data.split(/\r?\n/).map(s => s.trim()).filter(Boolean);

  //const rows = ['L68', 'L30', 'R48', 'L5', 'R60', 'L55' ,'L1', 'L99','R14', 'L82'];
  //const rows = "987654321111111,811111111111119,234234234234278,818181911112111"
  let sum = 0;
  let sum2= 0;
 
  const arr =  rows //data 
         //.split(/,/)
	 .map(s => s.trim());                          // split by comma
          //.map(s => s.trim().split(/-/).map(Number)); // split by dash and convert to numbers
        
  
   for (let s of arr) {
      sum += test1(s);
      sum2 += test2(s);
  }

  function test1(s) {
   
    let total = 0;
    let maxLeft = 0;
    let maxRight = 0;   
    let idx = -1;
    for (let i = 0; i<s.length-1; i++) {
		if(s[i] > maxLeft){
		    maxLeft = s[i];
		    idx = i;
		}	  
    }

    for (let i = idx+1; i<s.length; i++) {
		if(s[i] > maxRight)
		    maxRight = s[i];	
    }
     total = +(maxLeft + maxRight);
  	return total;
   }

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

   // Ensure exactly 12 digits
    total = +stack.slice(0, k).join("");
  
  	return total;
   }


  return [sum, sum2];
}

// quick test with the example sequence (uncomment to test without file)
//const testRows = ['L68','L30','R48','L5','R60','L55','L1','L99','R14','L82'];
//require('fs').writeFileSync('input.txt', testRows.join('\n'), 'utf8');
console.log(zero());