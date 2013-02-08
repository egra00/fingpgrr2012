#! /bin/bash

############################################################################
#                                                                          #
# Solo funciona en Gilbert!. El find no se comporta igual en Debian/Ubuntu #
#                                                                          #
############################################################################

if [ $# -lt 3 ]; then
	echo "Usage $0 totem_runner topologies_path tests_path"
	exit -1
fi

totem_script=$1
topologies_path=$2
test_file=$3
debug=$4
all_algoritms=(bgpsep bgpsepB bgpsepD bgpsepS) 

mode="2>&1 /dev/null"
if [ ${debug} -a ${debug} = "-vv" ]; then
	mode="> /dev/null"
fi
if [ ${debug} -a ${debug} = "-vvv" ]; then
        mode=""
fi

for one_topology_file in `find ${topologies_path} -regextype awk -mtime -1 ! -regex  '.*(svn|BGPSep|BGPSepBackbone|BGPSepD|BGPSepS|Cbr|FullMesh|Optimal|Zhang|Bates|BatesY|BatesZ).*' -regex '.*xml$'`
do 
	for algoritm in "${all_algoritms[@]}"
	do
		while read params           
		do
			eval "${totem_script} -rrloc_${algoritm} ${one_topology_file} ${params} ${mode}"
			#echo "${totem_script} -rrloc_${algoritm} ${one_topology_file} ${params} ${mode}"
		done < ${test_file}
	done
done

