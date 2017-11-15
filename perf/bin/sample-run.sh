#!/bin/bash

exe_path=$1
trg=$2
report=$3
formats="$4"

storage=fs


version=$(java -jar $exe_path --version | sed 's/Version: \([^ ]\+\) \[.*/\1/')

if [ "$version" = "0.4.1" ]; then
   fs=txn
else
   fs=fs
fi


(
echo "exe: $exe_path"
echo "set: $trg"
echo "version: $version"
echo "storage: $storage"
echo "report: $report"
echo "formats: $formats"
echo ""
echo ""
 
for i in 1 2 3 4 5; do 
	/usr/bin/time -f "\nreal\t%es\nuser\t%Us\nsys\t%Ss\nmem\t%Mk (max)\ncpu\t%P" \
	java -Xmx4G -Xms4G -jar "$exe_path" \
	--cfg perf-$storage.conf \
	--input.$fs.glob "**.txn" \
	--input.$fs.dir data/perf-$trg/ \
	--output out/perf-$storage-$trg \
	--reporting.console false \
	--reporting.formats $formats \
	--reporting.reports $report

	echo
done
) > results/hw00/$version-perf-$storage-$trg-$report-"$(echo $formats | tr ' ' '_')".txt  2>&1

#) > results/hw00/$version-perf-$storage-$trg-$report.txt  2>&1
# --input.git.ref $trg \

