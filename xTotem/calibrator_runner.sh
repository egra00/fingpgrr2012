#!/bin/sh
host=hostname
(	trap "" HUP
	exec 0< /dev/null
	exec 1> calibrator.log
	exec 2> calibrator.log
	./calibrator.sh ./totem.sh ./parser.pl calibration/topologies calibration/params
) &
echo "##### Ejecutando en ${host} con pid $! #####" > calibrator.pid

