#!/bin/bash

exe_dir=$(dirname $0)

find "$exe_dir/" -name '*.exec' | xargs grep 'test:uuid:' -L

