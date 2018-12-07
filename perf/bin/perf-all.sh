#!/bin/bash

exe_dir=$(dirname $0)

reports="balance balance-group register"
#reports="balance"

sets="1E3 1E4 1E5 1E6"
#sets="1E6"

#versions="0.4.1 0.5.0 0.6.0 0.7.0 0.8.0 0.9.0"
versions="0.10.0"

for v in $versions; do
for s in $sets; do
for r in $reports; do
for frmt in txt json "txt json"; do
for flt in "" '{ "txnFilter": { "TxnFilterAND" : { "txnFilters" : [ { "TxnFilterTxnCode": { "regex": "#.*" }},  { "TxnFilterTxnDescription": { "regex": "txn-.*" } } ] } } }'; do

if [ -n "$flt" ]; then
       log_flt="filter"
else
       log_flt="all"
fi

echo "run: $v $s $r $frmt $log_flt"
$exe_dir/perf-run.sh dist/tackler-cli-$v.jar $s $r "$frmt" "$flt"

done
done
done
done
done
