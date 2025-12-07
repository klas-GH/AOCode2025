def run():
    # Read the file
    with open("input.txt", "r", encoding="utf-8") as f:
        arr = [line.strip() for line in f if line.strip()]

    sum1 = 0
    sum2 = 0

    for s in arr:
        sum1 += test1(s)
        sum2 += test2(s)

    return sum1, sum2


def test1(s: str) -> int:
    k = 2
    to_remove = len(s) - k
    stack = []

    for digit in s:
        while stack and to_remove > 0 and stack[-1] < digit:
            stack.pop()
            to_remove -= 1
        stack.append(digit)

    # ensure exactly k digits
    total = int("".join(stack[:k]))
    return total


def test2(s: str) -> int:
    k = 12
    to_remove = len(s) - k
    stack = []

    for digit in s:
        while stack and to_remove > 0 and stack[-1] < digit:
            stack.pop()
            to_remove -= 1
        stack.append(digit)

    # ensure exactly k digits
    total = int("".join(stack[:k]))
    return total


if __name__ == "__main__":
    print(run())
