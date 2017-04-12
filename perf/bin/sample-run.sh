#!/bin/bash

exe_path=$1
trg=$2

(
for i in 1 2 3 4 5; do 
	time java -jar "$exe_path" \
	--cfg perf.conf \
	--input.txn.glob "**.txn" \
	--input.txn.dir data/perf-$trg/ \
	--output out/perf-$trg

	echo
done
) > perf-$trg.txt  2>&1 

