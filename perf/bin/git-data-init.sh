#!/bin/bash
# vim: tabstop=4 shiftwidth=4 smarttab expandtab softtabstop=4 autoindent

if [ $# != 1 ]; then
	echo "Usage: $0 <1E3 | 1E4 | 1E5 | 1E6>"
	exit 1
fi

name=$1
store="../../tperf/store/perf-$name"

if [ ! -d $store ]; then
	echo "Error: $store is not found"
	exit 1
fi

git co master

git co -b $name

touch txns-$name.txt
git add txns-$name.txt
git commit -m "initial $name"
 
mkdir -p txns
mkdir -p txns/2016

for i in 01 02 03 04 05 06 07 08 09 10 11 12; do

    echo "Perf: doing $name, round: $i"
    cp -a "$store/2016/$i" txns/2016/
    git add txns
    git commit -m "txns: $name: 2016/$i"
    git gc

    # make sure that git time stamps are distinct
    echo "Perf: done $name, round: $i"
    sleep 3
done

git push --set-upstream origin $name

