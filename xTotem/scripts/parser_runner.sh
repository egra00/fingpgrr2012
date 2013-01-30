#!/usr/bin/env bash

mkdir -p parser_out

echo "Running parser..."
for mrt in `ls parser_data`; do
    echo -n "      parsing $mrt..."
    ./bgpdump -vm parser_data/$mrt > parser_out/$mrt.bgp
done
