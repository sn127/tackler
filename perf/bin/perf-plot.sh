#!/bin/bash
# vim: tabstop=4 shiftwidth=4 smarttab expandtab softtabstop=4 autoindent
#
# Plot perf data with Gnuplot
#
if [ $# -ne 2 ]; then
    echo "Usage: $0 <hw> <test set: [1E3, 1E4, 1E5, 1E6]>"
    exit 1
fi

hw=$1
testset=$2

data () {
    local report=$1
    local frmt=$2
    local key=$3
    local unit=$4
    local ppeq=$5

    (
        for i in $(find results/$hw -name "*$testset-$report*" | egrep "$report-$frmt"'\.txt'); do
            echo $i
            v=$(echo $i | sed 's/.*\/\(.*\)-perf.*/\1/')
            t=$( (grep $key $i | sed 's/'$key'\t\(.*\)'$unit'.*/\1/' | sort -n | tail -n4 | head -n3; echo "2 k + + 3 / $ppeq p") | dc)

            echo -e "$v\t\t$t"
        done
    ) | sort -k1
}


lines () {
cat <<EOF
    '-' using 1:2:xtic(1) t "register (txt)"        with lines lc rgbcolor "0xFF0000" lw 2 ,\
    '-' using 1:2:xtic(1) t "balance (txt)"         with lines lc rgbcolor "0x00FF00" lw 2, \
    '-' using 1:2:xtic(1) t "balance-group (txt)"   with lines lc rgbcolor "0x0000FF" lw 2,\
    '-' using 1:2:xtic(1) t "register (json)"       with linespoints lc rgbcolor "0xFF0000" lw 2 dt "-",\
    '-' using 1:2:xtic(1) t "balance (json)"        with linespoints lc rgbcolor "0x00FF00" lw 2 dt "-", \
    '-' using 1:2:xtic(1) t "balance-group (json)"  with linespoints lc rgbcolor "0x0000FF" lw 2 dt "-"
EOF
}


###
### GNUPLOT
###
(
cat <<EOF
set term pngcairo dashed size 800,2400
#set term svg dashed size 800,2400 dynamic background "0xFFFFFF"
set output "perf-$hw-$testset.png"
set size 1.0,1.0
set origin 0.0,0.0
set xtics rotate
set multiplot
set size 1.0,0.3
set origin 0,0
set grid
set title "Test set: $testset"
set key top left
set ylabel "Time (s)"
set xrange  [*:*]
set yrange [*:*]
plot \
$(lines)
EOF

for frmt in "txt" "json"; do
for rpt in register balance balance-group; do
	data "$rpt" "$frmt" real s ""
	echo e
done
done

cat <<EOF
set size 1.0,0.3
set origin 0,0.3
set grid
set title "Test set: $testset"
set key top left
set ylabel "Memory (M)"
set xrange  [*:*]
set yrange [*:*]
plot \
$(lines)
EOF

for frmt in "txt" "json"; do
for rpt in register balance balance-group; do
	data "$rpt" "$frmt" mem k "1024 /"
	echo e
done
done

cat <<EOF
set size 1.0,0.3
set origin 0,0.6
set grid
set title "Test set: $testset"
set key top left
set ylabel "CPU %"
set xrange  [*:*]
set yrange [*:*]
plot \
$(lines)
EOF

for frmt in "txt" "json"; do
for rpt in register balance balance-group; do
	data "$rpt" "$frmt" cpu "%" ""
	echo e
done
done


set unset multiplot
) \
| gnuplot

