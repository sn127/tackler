#!/bin/bash
# vim: tabstop=4 shiftwidth=4 smarttab expandtab softtabstop=4 autoindent

test_dir=$(dirname $0)

find "$test_dir" -name "*.ref.*" | while read ref; do
    dir=$(dirname $ref)
    refname=$(basename $ref)
    outname=$(echo $refname | sed 's/\(.*\)\.ref\.\(.*\)/out.\1.\2/')

    mv "$dir/$outname" "$dir/$refname"
done
