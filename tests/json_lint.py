#!/usr/bin/env python3
# vim: tabstop=4 shiftwidth=4 smarttab expandtab softtabstop=4 autoindent

import json
import sys

filename=sys.argv[1]

try:
    with open(filename, 'r') as f:
        json.load(f)
except ValueError as e:
    print("filename: " + filename)
    print(e)
    sys.exit(1)

