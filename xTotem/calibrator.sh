#! /bin/bash

####################################################################################
# En la carpeta topologies_and_tras_path estarán las topologías sobre las que      #
# se va a ejecutár la calibración y para cada topología example.xml debe           #
# existir un archivo example.tra con la definición del escenario para esta         #
# topología                                                                        #
####################################################################################
#                                                                                  #
# Para ejecutar la calibración de los alogritmos con las instancias presentadas:   #
# ./calibrator.sh ./totem.sh ./parser.pl calibration/topologies calibration/params #
#                                                                                  #
####################################################################################

if [ $# -lt 4 ]; then
	echo "Usage $0 totem_runner parser topologies_and_tras_path params_path"
	exit -1
fi

totem_script=$1
parser_script=$2
topologies_and_tras_path=$3
params_path=$4
debug=$5
#all_algoritms=(bgpsep bgpsepB bgpsepD bgpsepS) 
all_algoritms=(bgpsep)

mode="2>&1 > /dev/null"
if [ ${debug} -a ${debug} = "-vv" ]; then
	mode="> /dev/null"
fi
if [ ${debug} -a ${debug} = "-vvv" ]; then
        mode=""
fi

for one_topology_file in `find ${topologies_and_tras_path} -regextype awk -not -regex  '.*(svn|BGPSep|BGPSepBackbone|BGPSepD|BGPSepS|Cbr|FullMesh|Optimal|Zhang|Bates|BatesY|BatesZ).*' -regex '.*xml$'`
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
		for one_params_file in `find ${params_path} -regextype awk -not -regex '*.params$'`
			base_params_file=`basename ${one_params_file%.*}`
			while read params
			do
				eval "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${base_params_file}_${unique_file} ${params} ${mode}"
				#echo "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${base_params_file}_${unique_file} ${params} ${mode}"
				eval "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.bgp"
				#echo "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.bgp"

				eval "${parser_script} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.bgp ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}"
				#echo "${parser_script} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.bgp ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}"

				((unique_file++))
			done < ${one_params_file}
		done
	done
done

