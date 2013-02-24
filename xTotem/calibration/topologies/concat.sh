#!/bin/bash

folder=$1
algo=$2

for file in `find ./$folder -regex .*_${algo}_.*.csv` 
do
	echo "," >> ./${folder}-${algo}-data.csv
	cat $file >> ./${folder}-${algo}-data.csv

done
