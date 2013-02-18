#! /bin/bash

############################################################################
#                                                                          #
# Solo funciona en Gilbert!. El find no se comporta igual en Debian/Ubuntu #
#                                                                          #
############################################################################

exclude=".*(bgpsep|bgpsepB|bgpsepD|bgpsepS|fullmesh|optimal|zhang|bates|batesY|batesZ|svn|BGPSep|BGPSepBackbone|BGPSepD|BGPSepS|Cbr|FullMesh|Optimal|Zhang|Bates|BatesY|BatesZ).*"

if [ $# -lt 2 ]; then
	echo "Usage $0 totem_runner topologies_path"
	exit -1
fi

totem_script=$1
test_path=$2
debug=$3
all_algoritms=(bgpsep bgpsepB bgpsepD bgpsepS fullmesh optimal zhang bates batesY batesZ) 

mode="2>&1 > /dev/null"
if [ ${debug} -a ${debug} = "-vv" ]; then
	mode="> /dev/null"
fi
if [ ${debug} -a ${debug} = "-vvv" ]; then
        mode=""
fi

for test_file in `find ${test_path} -regextype awk -not -regex  '${exclude}' -regex '.*xml$'`
do 
	for algoritm in "${all_algoritms[@]}"
	do
		echo "[${totem_script}]: Starting ${algoritm} with ${test_file}"
		#eval "${totem_script} -rrloc_${algoritm} ${test_file} ${mode}"
		echo "[${totem_script}]: End ${algoritm} with ${test_file}"
	done
done

