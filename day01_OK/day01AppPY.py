def run():
    # Read the file synchronously (like fs.readFileSync in Node.js)
    with open("input.txt", "r", encoding="utf-8") as f:
        data = f.read()

    # Split into rows by newline, strip whitespace, and filter out empty lines
    rows = [line.strip() for line in data.splitlines() if line.strip()]

    # -----------------------------
    # test1: count how many times position lands exactly on 0
    # -----------------------------
    def test1():
        pos = 50  # initial position
        count = 0
        for val in rows:
            dir = val[0]              # first character: 'R' or 'L'
            nr = abs(int(val[1:]))    # numeric part of the string

            if dir == "R":
                pos = (pos + nr) % 100
            else:
                pos = (100 + pos - nr) % 100

            # If position is exactly 0, increment counter
            if pos == 0:
                count += 1
        return count

    # -----------------------------
    # test2: count how many times we *pass through* position 0
    # -----------------------------
    def test2():
        count = 0
        pos = 50  # initial position

        for val in rows:
            dir = val[0]              # 'R' or 'L'
            nr = abs(int(val[1:]))    # steps to move

            if dir == "R":
                # Steps needed to reach the first zero going right
                steps_to_first_zero = 100 if pos == 0 else 100 - pos
                # If we have enough steps to reach/past zero, count crossings
                if nr >= steps_to_first_zero:
                    count += (nr - steps_to_first_zero) // 100 + 1
                # Update position
                pos = (pos + nr) % 100
            else:
                # Steps needed to reach the first zero going left
                steps_to_first_zero = 100 if pos == 0 else pos
                if nr >= steps_to_first_zero:
                    count += (nr - steps_to_first_zero) // 100 + 1
                # Update position (safe modulo for negatives)
                pos = ((pos - nr) % 100 + 100) % 100

        return count

    # Run both tests and return results
    sum1 = test1()
    sum2 = test2()
    return [sum1, sum2]


if __name__ == "__main__":
    # Print the results like console.log in JS
    print(run())
