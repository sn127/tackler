#!/bin/bash

exe_dir=$(dirname $0)

test_db="$exe_dir/tests.yaml"

echo "Check test-db:"
pykwalify -v -s  "$exe_dir/tests-schema.yaml" -d  "$test_db"

grep ' id:' "$test_db" | sed 's/.*id: //' | sort | uniq -d

# good enough for know
grep ' refid:' "$test_db" | sed 's/.*refid: //' | while read refid; 
do  
	egrep -q -L '.* id: +'$refid' *$' "$test_db" || echo $refid
done

echo "Missing uuid:"
$exe_dir/find-missing.sh

echo "Duplicates:"
find "$exe_dir" -name '*.exec' | xargs sed -n 's/.*test:uuid: \(.*\)/\1/p' | sort | uniq -d



echo 
echo "Silence is gold"
