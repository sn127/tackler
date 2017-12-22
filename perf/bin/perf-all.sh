#!/bin/bash

exe_dir=$(dirname $0)

reports="balance balance-group register"
#reports="balance"

sets="1E3 1E4 1E5 1E6"
#sets="1E6"

#versions="0.4.1 0.5.0 0.6.0 0.7.0 0.8.0 0.9.0"
versions="0.9.0"

for v in $versions; do
for s in $sets; do
for r in $reports; do
for f in txt json "txt json"; do

echo "run: $v $s $r $f"
$exe_dir/perf-run.sh dist/tackler-cli-$v.jar $s $r "$f"

done
done
done
done
