#!/bin/bash

exe_dir=$(dirname $0)

grep error: "$exe_dir/tests.yaml" | sed 's/.*error: //' | grep '&' | sort | uniq -d
grep test: "$exe_dir/tests.yaml" | sed 's/.*test: //' | grep '&' | sort | uniq -d

