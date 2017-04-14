#!/bin/bash

exe_dir=$(dirname $0)

test_db="$exe_dir/tests.yaml"

pykwalify -v -s  "$exe_dir/tests-schema.yaml" -d  "$test_db"

grep ' id:' "$test_db" | sed 's/.*id: //' | sort | uniq -d

# good enough for know
grep ' refid:' "$test_db" | sed 's/.*refid: //' | while read refid; 
do  
	egrep -q -L '.* id: +'$refid' *$' "$test_db" || echo $refid
done

echo "silence is gold"
