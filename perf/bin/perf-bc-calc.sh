#!/bin/bash
#
# Calculate total sum of transactions with bc
#
perf=$1

((echo "0.0"; find $perf -type f -exec sed -n 's/.*e:.* \(.*\)$/ + \1/p' {} \; ) | tr -d '\n'; echo) | bc -l | tee $perf-bc.txt
