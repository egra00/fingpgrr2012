#! /bin/bash

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
#all_algoritms=(bgpsep bgpsepB bgpsepD bgpsepS) 
all_algoritms=(bgpsep)

mode="2>&1 > /dev/null"
if [ ${debug} -a ${debug} = "-vv" ]; then
	mode="> /dev/null"
fi
if [ ${debug} -a ${debug} = "-vvv" ]; then
        mode=""
fi


for one_topology_file in `find ${topologies_and_tras_path} -regextype awk -mtime -1 ! -regex  '.*(svn|BGPSep|BGPSepBackbone|BGPSepD|BGPSepS|Cbr|FullMesh|Optimal|Zhang|Bates|BatesY|BatesZ).*' -regex '.*xml$'`
do
	one_tra_file="${one_topology_file%.*}.tra"
	if [ ! -f ${one_tra_file} ]; then
		echo "ERROR: Not exist ${one_tra_file}"
		exit -1
	fi
	for algoritm in "${all_algoritms[@]}"
	do
		base_output_name=`basename ${one_topology_file%.*}_${algoritm}`
		topology_name=`basename ${one_topology_file%.*}`
		base_output_dir=`dirname ${one_topology_file}`
		unique_file=0
		while read params
		do
			eval "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${unique_file} ${params} ${mode}"
			#echo "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${unique_file} ${params} ${mode}"
			eval "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.bgp"
			#echo "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.bgp"

			((unique_file++))
		done < ${test_file}
	done
done

