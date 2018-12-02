#!/bin/bash
#
# Generate test-db entries from ScalaTest-files
#
# Expected input format of ScalaTest is:
#    /**
#     * test: 11d4409c-93e2-4670-b2d5-65073980ba2d
#     */
#    it should "AND(false, true)" in {
#
test_file="$1"
test_class="$2"

print_test() {
  local test_id="$1"
  local desc="$2"

cat << EOF
          - test:
              id: $test_id
              name: $test_class
              descriptions:
                - desc: "$desc"
EOF
}


grep --no-group-separator -A2 test: "$test_file" | \
grep -v '\*/' | \
while read tst 
do 
	read raw_desc
	desc="$(echo $raw_desc | sed 's/.*"\(.*\)".*/\1/')"
	test_id="$(echo $tst | sed 's/.*test: //')"
	print_test "$test_id" "$desc"
done
