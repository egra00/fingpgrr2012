#! /bin/bash

if [ $# -lt 2 ]; then
	echo "Usage $0 totem_runner topologies_path"
	exit -1
fi

test_path=$1
totem_script=$2
debug=$3
all_algoritms=(bgpsep bgpsepB bgpsepD bgpsepS fullmesh optimal zhang bates batesY batesZ) 

mode="2>&1 /dev/null"
if [ ${debug} -a ${debug} = "-vv" ]; then
	mode="> /dev/null"
fi
if [ ${debug} -a ${debug} = "-vvv" ]; then
        mode=""
fi



for test_file in `find ${test_path} -regextype awk -mtime -1 ! -regex  '.*(svn|BGPSep|BGPSepBackbone|BGPSepD|BGPSepS|Cbr|FullMesh|Optimal|Zhang|Bates|BatesY|BatesZ).*' -regex '.*xml$'`
do 
	for algoritm in "${all_algoritms[@]}"
	do
		echo "[${totem_script}]: Starting ${algoritm} with ${test_file}"
		eval "${totem_script} -rrloc_${algoritm} ${test_file} ${mode}"
		echo "[${totem_script}]: End ${algoritm} with ${test_file}"
	done
done

