#!/bin/bash

folder=$1

for file in `find ./$folder -regex .*.csv` 
do
	echo "," >> ./${folder}-data.csv
	cat $file >> ./${folder}-data.csv

done
