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

tmp_file=$(mktemp tackler-perf-plot.XXXXXX)
trap "rm -f $tmp_file" 0

data () {
    local report=$1
    local frmt=$2
    local key=$3
    local unit=$4
    local ppeq=$5
    local filter="$6"

    (
        for i in $(find results/$hw -name "*$testset-$report*" | egrep "$filter" | egrep "$report-$frmt"'\.txt'); do
            #echo $i
            v=$(echo $i | sed 's/.*\/\(.*\)-perf.*/\1/' | awk -F '.' '{ printf("%d.%02d.%d", $1, $2, $3)}')
            t=$( (grep $key $i | sed 's/'$key'\t\(.*\)'$unit'.*/\1/' | sort -n | tail -n4 | head -n3; echo "2 k + + 3 / $ppeq p") | dc)

            echo -e "$v\t\t$t"
        done
    ) | sort -t '.' -n -k 1,1 -k 2,2 -k 3,3 > $tmp_file
    # duplicate last data point, so resulting plot with straigth line will be easier to read than single dot at the end.
    # There could be some option / different plot style with gnuplot, but let's go with that now 
    cat $tmp_file
    last_result=$(tail -n1 $tmp_file)
    echo -n "$(echo $last_result | awk '{ print $1; }'| awk -F '.' '{ printf("%d.%02d.%d-dev", $1, $2+1, $3)}')  "
    echo  "$(echo $last_result | awk '{ print $2}')"

}


lines () {
cat <<EOF
    '-' using 1:2:xtic(1) t "register (txt, all)"        with linespoints pt 9 lc rgbcolor "0xFF0000" lw 2 dt 1, \
    '-' using 1:2:xtic(1) t "balance (txt, all)"         with linespoints pt 9 lc rgbcolor "0x00FF00" lw 2 dt 1, \
    '-' using 1:2:xtic(1) t "balance-group (txt, all)"   with linespoints pt 9 lc rgbcolor "0x0000FF" lw 2 dt 1, \
    '-' using 1:2:xtic(1) t "register (json, all)"       with linespoints pt 7 lc rgbcolor "0xFF0000" lw 2 dt 2, \
    '-' using 1:2:xtic(1) t "balance (json, all)"        with linespoints pt 7 lc rgbcolor "0x00FF00" lw 2 dt 2, \
    '-' using 1:2:xtic(1) t "balance-group (json, all)"  with linespoints pt 7 lc rgbcolor "0x0000FF" lw 2 dt 2, \
    '-' using 1:2:xtic(1) t "register (txt, flt)"        with linespoints pt 9 lc rgbcolor "0x880000" lw 2 dt 4, \
    '-' using 1:2:xtic(1) t "balance (txt, flt)"         with linespoints pt 9 lc rgbcolor "0x008800" lw 2 dt 4, \
    '-' using 1:2:xtic(1) t "balance-group (txt, flt)"   with linespoints pt 9 lc rgbcolor "0x000088" lw 2 dt 4, \
    '-' using 1:2:xtic(1) t "register (json, flt)"       with linespoints pt 7 lc rgbcolor "0x880000" lw 2 dt 3, \
    '-' using 1:2:xtic(1) t "balance (json, flt)"        with linespoints pt 7 lc rgbcolor "0x008800" lw 2 dt 3, \
    '-' using 1:2:xtic(1) t "balance-group (json, flt)"  with linespoints pt 7 lc rgbcolor "0x000088" lw 2 dt 3
EOF
}

###
### GNUPLOT
###
(
cat <<EOF
#set term pngcairo dashed size 800,2400
set term svg dashed size 2400,800 dynamic background "0xFFFFFF"
set output "perf-$hw-$testset.svg"
set size 1.0,1.0
set origin 0.0,0.0
set xtics rotate
set multiplot

set size 0.33,1.0
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

for flt in '(perf-fs)|(perf-all)' 'perf-flt'; do
for frmt in "txt" "json"; do
for rpt in register balance balance-group; do
	data "$rpt" "$frmt" real s "" "$flt"
	echo e
done
done
done

cat <<EOF
set size 0.33,1.0
set origin 0.33,0
set grid
set title "Test set: $testset"
set key top left
set ylabel "Memory (M)"
set xrange  [*:*]
set yrange [*:*]
plot \
$(lines)
EOF

for flt in '(perf-fs)|(perf-all)' 'perf-flt'; do
for frmt in "txt" "json"; do
for rpt in register balance balance-group; do
	data "$rpt" "$frmt" mem k "1024 /" "$flt"
	echo e
done
done
done

cat <<EOF
set size 0.3,1.0
set origin 0.66,0
set grid
set title "Test set: $testset"
set key top left
set ylabel "CPU %"
set xrange  [*:*]
set yrange [*:*]
plot \
$(lines)
EOF

for flt in '(perf-fs)|(perf-all)' 'perf-flt'; do
for frmt in "txt" "json"; do
for rpt in register balance balance-group; do
	data "$rpt" "$frmt" cpu "%" "" "$flt"
	echo e
done
done
done


set unset multiplot
) \
| gnuplot

