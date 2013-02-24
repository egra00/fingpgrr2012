#! /bin/bash

####################################################################################
# En la carpeta topologies_and_tras_path estarán las topologías sobre las que      #
# se va a ejecutár la calibración y para cada topología example.xml debe           #
# existir un archivo example.tra con la definición del escenario para esta         #
# topología                                                                        #
####################################################################################

exclude=".*(bgpsep|bgpsepB|bgpsepD|bgpsepS|fullmesh|optimal|zhang|bates|batesY|batesZ|svn|BGPSep|BGPSepBackbone|BGPSepD|BGPSepS|Cbr|FullMesh|Optimal|Zhang|Bates|BatesY|BatesZ).*"

start_time="$(date +%s)"

if [ $# -lt 3 ]; then
	echo "Usage $0 totem_runner_script parser_script topologies_and_tras_path [params_path] flags "
	echo "	Where flags: "
	echo "		[-vv|-vvv]"
	echo "		[-f]"
	echo "		[-bgpsep]"
	echo "		[-bgpsepB]"
	echo "		[-bgpsepD]"
	echo "		[-bgpsepS]"
	echo "		[-fullmesh]"
	echo "		[-optimal]"
	echo "		[-zhang]"
	echo "		[-bates]"
	echo "		[-batesY]"
	echo "		[-batesZ]"
	exit -1
fi

totem_script=$1
parser_script=$2
topologies_and_tras_path=$3

params_path=$4

debug=""
files=""
algoritms=()

for flag in "$@"
do
	case ${flag} in
		-vv)
			debug="> /dev/null"
			;;
		-vvv)
			debug=""
			;;
		-f)
			files="true"
			;;
		-bgpsep)
			algoritms+=('bgpsep')
			;;
		-bgpsepB)
			algoritms+=('bgpsepB')
			;;
		-bgpsepD)
			algoritms+=('bgpsepD')
			;;
		-bgpsepS)
			algoritms+=('bgpsepS')
			;;
		-fullmesh)
			algoritms+=('fullmesh')
			;;
		-optimal)
			algoritms+=('optimal')
			;;
		-zhang)
			algoritms+=('zhang')
			;;
		-bates)
			algoritms+=('bates')
			;;
		-batesY)
			algoritms+=('batesY')
			;;
		-batesZ)
			algoritms+=('batesZ')
			;;
	esac
done

if [ ${#algoritms[@]} -eq 0 ]; then
	echo "Not specify any algorithm. Nothing to do"
	exit -1
fi

if [ "${debug}" = "" ]; then
	debug="2>&1 > /dev/null"
fi

if [ "${params_path}" = "-vv" ] || [ "${params_path}" = "-vvv" ] || [ "${params_path}" = "-f" ] || [ "${params_path}" = "-bgpsep" ] || \
   [ "${params_path}" = "-bgpsepB" ] || [ "${params_path}" = "-bgpsepD" ] || [ "${params_path}" = "-bgpsepS" ] || [ "${params_path}" = "-fullmesh" ] || \
   [ "${params_path}" = "-optimal" ] || [ "${params_path}" = "-zhang" ] || [ "${params_path}" = "-bates" ] || [ "${params_path}" = "-batesY" ] || \
   [ "${params_path}" = "batesZ" ]; then
	params_path=""
fi

# Calculate the total work to print avance
total_work=0
for one_topology_file in `find ${topologies_and_tras_path} -regextype awk -not -regex  "${exclude}" -regex '.*xml$'`
do
	one_tra_file="${one_topology_file%.*}.tra"
	if [ ! -f ${one_tra_file} ]; then
		echo "ERROR: Not exist ${one_tra_file}"
		exit -1
	fi
	for algoritm in "${algoritms[@]}"
	do
		if [ "${params_path}" = "" ]; then
			((total_work+=3))
		else
			for one_params_file in `find ${params_path} -name '*.params'`
			do
				while read params
				do
					((total_work+=3))
				done < ${one_params_file}
			done
		fi
	done
done

current_work=0
function printPorcentage() {
	((current_work++))
	current_time="$(($(date +%s)-start_time))"
	current_time=`printf "%02d:%02d:%02d:%02d\n" "$((current_time/86400))" "$((current_time/60%60))" "$((current_time%60))"`
	echo "${current_time} - $(echo "${current_work} * 100 / ${total_work}" | bc -l | awk '{printf "%.2f", $0}')%"
}

function removeTmpFiles() {
	# Only need the csv
	if [ "${files}" = "" ]; then 
		rm ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.bgp
		rm ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.msg
		rm ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.xml
		rm ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.cli
	fi
}

for one_topology_file in `find ${topologies_and_tras_path} -regextype awk -not -regex  "${exclude}" -regex '.*xml$'`
do
	one_tra_file="${one_topology_file%.*}.tra"
	for algoritm in "${algoritms[@]}"
	do
		base_output_name=`basename ${one_topology_file%.*}_${algoritm}`
		topology_name=`basename ${one_topology_file%.*}`
		base_output_dir=`dirname ${one_topology_file}`
		unique_file=0

		if [ "${params_path}" = "" ]; then
			eval "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${unique_file} ${params} ${debug}"
			#echo "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${unique_file} ${params} ${debug}"
			printPorcentage

			eval "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.bgp"
			#echo "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}.bgp"
			printPorcentage

			eval "${parser_script} ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file} ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}"
			#echo "${parser_script} ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file} ${base_output_dir}/${topology_name}-${base_output_name}_${unique_file}"
			printPorcentage

			removeTmpFiles

			((unique_file++))

		else
			for one_params_file in `find ${params_path} -name '*.params'`
			do
				base_params_file=`basename ${one_params_file%.*}`
				while read params
				do

					eval "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${base_params_file}_${unique_file} ${params} ${debug}"
					#echo "${totem_script} -rrloc_${algoritm} ${one_topology_file} -tra ${one_tra_file} -ofn ${base_output_name}_${base_params_file}_${unique_file} ${params} ${debug}"
					printPorcentage

					eval "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.bgp"
					#echo "cbgp -c ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.cli > ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}.bgp"
					printPorcentage

					eval "${parser_script} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}"
					#echo "${parser_script} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file} ${base_output_dir}/${topology_name}-${base_output_name}_${base_params_file}_${unique_file}"
					printPorcentage

					removeTmpFiles

					((unique_file++))

				done < ${one_params_file}
			done
		fi
	done
done

