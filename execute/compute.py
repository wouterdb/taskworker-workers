#!/usr/bin/python

import random, sys, time, numpy

def main():
    if len(sys.argv) != 3:
        return 1

    a = int(sys.argv[1])
    b = int(sys.argv[2])

    value = random.randrange(a, b)
    time.sleep(numpy.random.normal(10, 4))
    print(value)

if __name__ == "__main__":
    sys.exit(main())

