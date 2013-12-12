#!/usr/bin/python

import random, sys, time, numpy, os

def main():
    if "INPUT_FILE" not in os.environ or "OUTPUT_FILE" not in os.environ:
        sys.stderr.write("Error while finding input and output files\n")
        return 1

    input_file = os.environ["INPUT_FILE"]
    output_file = os.environ["OUTPUT_FILE"]

    args = {}
    with open(input_file, "r") as fd:
        for line in fd.readlines():
            key,value = line.strip().split("=")
            args[key] = value

    if "a" in args and "b" in args:
        a = int(args["a"])
        b = int(args["b"])

        value = random.randrange(a, b)
        time.sleep(numpy.random.normal(2, 1))

        with open(output_file, "w+") as fd:
            fd.write(str(value))

    return 1

if __name__ == "__main__":
    sys.exit(main())

