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

exclude=".*(bgpsep|bgpsepB|bgpsepD|bgpsepS|fullmesh|optimal|zhang|bates|batesY|batesZ|svn|BGPSep|BGPSepBackbone|BGPSepD|BGPSepS|Cbr|FullMesh|Optimal|Zhang|Bates|BatesY|BatesZ).*"

start_time="$(date +%s)"

if [ $# -lt 4 ]; then
	echo "Usage $0 totem_runner parser topologies_and_tras_path params_path"
	exit -1
fi

totem_script=$1
parser_script=$2
topologies_and_tras_path=$3
params_path=$4
debug=$5
all_algoritms=(bgpsep bgpsepB bgpsepD bgpsepS) 

mode="2>&1 > /dev/null"
if [ "${debug}" = "-vv" ]; then
	mode="> /dev/null"
fi
if [ "${debug}" = "-vvv" ]; then
        mode=""
fi

total_work=0
for one_topology_file in `find ${topologies_and_tras_path} -regextype awk -not -regex  '${exclude}' -regex '.*xml$'`
do
	one_tra_file="${one_topology_file%.*}.tra"
	if [ ! -f ${one_tra_file} ]; then
		echo "ERROR: Not exist ${one_tra_file}"
		exit -1
	fi
	for algoritm in "${all_algoritms[@]}"
	do
		for one_params_file in `find ${params_path} -name '*.params'`
		do
			while read params
			do
				((total_work+=3))
			done < ${one_params_file}
		done
	done
done

current_work=0
function printPorcentage() {
	((current_work++))
	current_time="$(($(date +%s)-start_time))"
	current_time=`printf "%02d:%02d:%02d\n" "$((current_time/3600%24))" "$((current_time/60%60))" "$((current_time%60))"`
	echo "${current_time} - $(echo "${current_work} * 100 / ${total_work}" | bc -l | awk '{printf "%.2f", $0}')%"
}

for one_topology_file in `find ${topologies_and_tras_path} -regextype awk -not -regex  '${exclude}' -regex '.*xml$'`
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
		for one_params_file in `find ${params_path} -name '*.params'`
		do
			base_params_file=`basename ${one_params_file%.*}`
			while read params
			do
				eval "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${base_params_file}_${unique_file} ${params} ${mode}"
				#echo "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${base_params_file}_${unique_file} ${params} ${mode}"
				printPorcentage

				eval "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.bgp"
				#echo "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.bgp"
				printPorcentage

				eval "${parser_script} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}"
				#echo "${parser_script} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}"
				printPorcentage

				# Only need the csv
				if [ "${mode}" != "" ]; then 
					rm ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.bgp
					rm ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.msg
					rm ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.xml
					rm ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.cli
				fi

				((unique_file++))

			done < ${one_params_file}
		done
	done
done

