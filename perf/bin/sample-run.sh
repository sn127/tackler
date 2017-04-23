#!/bin/bash

exe_path=$1
trg=$2

storage=git

 
# --input.txn.glob "**.txn" \
# --input.txn.dir data/perf-$trg/ \

(
for i in 1 2 3 4 5; do 
	time java -jar "$exe_path" \
	--cfg perf-$storage.conf \
	--input.git.ref $trg \
	--output out/perf-$storage-$trg

	echo
done
) > results/hw01/perf-$storage-$trg.txt  2>&1 

