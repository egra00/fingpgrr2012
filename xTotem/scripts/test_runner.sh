#! /bin/bash

test_path=$1
totem_script=$2
all_algoritms=(bgpsep bgpsepB bgpsepD bgpsepS cbr fullmesh optimal zhang)
for test_file in `find ${test_path} -name '*.xml'`
do 
	for algoritm in "${all_algoritms[@]}"
	do
		`${totem_script} -rrloc_${algoritm} ${test_file}`
	done
done

