#!/bin/bash

exe_path=$1
trg=$2

storage=fs
# --input.git.ref $trg \

(
for i in 1 2 3 4 5; do 
	time java -jar "$exe_path" \
	--cfg perf-$storage.conf \
	--input.fs.glob "**.txn" \
	--input.fs.dir data/perf-$trg/ \
	--output out/perf-$storage-$trg \
	--reporting.formats json txt \
	--reporting.console false \
	--reporting.reports register

	echo
done
) > results/hw01/0.7.0-next-perf-$storage-$trg-register_json_txt.txt  2>&1 

