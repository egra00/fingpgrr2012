#!/bin/sh

CPLEX_PATH_LIB=/usr/local/cplex122/cplex/lib/
CPLEX_PATH_BIN_ARQ=/usr/local/cplex122/cplex/bin/x86_sles10_4.1/

if [ ! -d "$CPLEX_PATH_LIB" -o ! -d "$CPLEX_PATH_BIN_ARQ" ]; then
	CPLEX_PATH_LIB=""
	CPLEX_PATH_BIN_ARQ=""
fi

TOTEM_HOMEDIR=`dirname $0`
TOTEM_BIN=$TOTEM_HOMEDIR/bin
LIBRARYPATH=$TOTEM_HOMEDIR/lib/:$CPLEX_PATH_BIN_ARQ
EXTDIRS=$TOTEM_HOMEDIR/lib/java:$TOTEM_HOMEDIR/lib/java/agape:$TOTEM_HOMEDIR/lib/java/jung2-201:$CPLEX_PATH_LIB

TOTEM_MAIN="be.ac.ulg.montefiore.run.totem.core.Totem"

JVMARGS="-d32 -Xmx512m"

if [ -z "$JAVA_HOME" ]; then
    JAVA=`which java`
else
    JAVA=$JAVA_HOME/bin/java
fi

if [ ! -f "$JAVA" ]; then
    echo "Java executable not found ($JAVA). Aborting."
    exit 0
fi

if [ ! -x "$JAVA" ]; then 
    echo "Java executable not found ($JAVA). (Not executable). Aborting."
    exit 0
fi


$JAVA $JVMARGS -cp "$TOTEM_BIN" -Djava.ext.dirs=$EXTDIRS -Djava.library.path=$LIBRARYPATH $TOTEM_MAIN $@
