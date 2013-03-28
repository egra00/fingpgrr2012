#!/bin/sh
host=`hostname`
(	trap "" HUP
	exec 0< /dev/null
	exec 1> $0.log
	exec 2> $0.log
	./runner.sh ./totem.sh ./parser.pl prove/topologies -f -bgpsepD -bgpsepS -fullmesh -optimal $@
) &
echo "##### Ejecutando en ${host} con pid $! #####" > $0.pid

