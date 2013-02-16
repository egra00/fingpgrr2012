#!/bin/sh
(	trap "" HUP
	exec 0< /dev/null
	exec 1> calibrator.log
	exec 2> calibrator.log
	./calibrator.sh ./totem.sh ./parser.pl calibration/topologies calibration/params
) &
echo "##### Ejecutando con pid $! #####" > calibrator.pid

