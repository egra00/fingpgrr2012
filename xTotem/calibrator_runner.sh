#!/bin/sh
host=`hostname`
(	trap "" HUP
	exec 0< /dev/null
	exec 1> calibrator.log
	exec 2> calibrator.log
	#./runner.sh ./totem.sh ./parser.pl calibration/topologies calibration/params $@
	./runner.sh ./totem.sh ./session_parser.pl calibration/topologies calibration/params -f -ns $@
) &
echo "##### Ejecutando en ${host} con pid $! #####" > calibrator.pid

