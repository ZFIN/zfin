#!/usr/bin/env python
 
import fileinput
 
for line in fileinput.input():
    count, id = line.split()
    score = 1.0 + float(count) * .01
    print id + "=" + str(score)

