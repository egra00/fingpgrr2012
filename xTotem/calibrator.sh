#! /bin/bash

################################################################################
################################################################################
###                                                                          ###
### Solo funciona en Gilbert!. El find no se comporta igual en Debian/Ubuntu ###
###                                                                          ###
################################################################################
################################################################################
# En la carpeta topologies_and_tras_path estarán las topologías sobre las que  #
# se va a ejecutár la calibración y para cada topología example.xml debe       #
# existir un archivo example.tra con la definición del escenario para esta     #
# topología                                                                    #
################################################################################

if [ $# -lt 3 ]; then
	echo "Usage $0 totem_runner topologies_and_tras_path tests_path"
	exit -1
fi

totem_script=$1
topologies_and_tras_path=$2
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

for one_topology_file in `find ${topologies_and_tras_path} -regextype awk -mtime -1 ! -regex  '.*(svn|BGPSep|BGPSepBackbone|BGPSepD|BGPSepS|Cbr|FullMesh|Optimal|Zhang|Bates|BatesY|BatesZ).*' -regex '.*xml$'`
do 
	one_tra_file="${one_topology_file::3}.tra"
	if [ -e ${one_tra_file} ]; then
		echo "ERROR: Not existe the file "
		exit -1
	fi
	for algoritm in "${all_algoritms[@]}"
	do
		one_output_name=`basename ${one_topology_file::3}${algoritm}`
		while read params           
		do
			#eval "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${params} ${mode}"
			echo "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${params} ${mode}"
		done < ${test_file}
	done
done

