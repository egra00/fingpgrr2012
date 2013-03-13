#!/bin/bash

folder=$1
algo=$2
result=$3

for file in `find ./$folder -regex .*_${algo}_.*.${result}`
do
	echo "," >> ./${folder}-${algo}-data.${result}
	cat $file >> ./${folder}-${algo}-data.${result}

done
