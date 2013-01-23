#!/bin/sh

TOTEM_HOMEDIR=`dirname $0`
TOTEM_BIN=$TOTEM_HOMEDIR/bin
LIBRARYPATH=$TOTEM_HOMEDIR/lib/:/usr/local/cplex122/cplex/bin/x86_sles10_4.1/
EXTDIRS=$TOTEM_HOMEDIR/lib/java:$TOTEM_HOMEDIR/lib/java/agape:$TOTEM_HOMEDIR/lib/java/jung2-201:/usr/local/cplex122/cplex/lib/

TOTEM_MAIN="be.ac.ulg.montefiore.run.totem.core.Totem"

JVMARGS="-d32 -Xmx512m"
#JVMARGS="-verbose:jni -Xcheck:jni -Xmx512m"

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


#echo "$JAVA $JVMARGS -cp "$TOTEM_BIN" -Djava.ext.dirs=$EXTDIRS -Djava.library.path=$LIBRARYPATH $TOTEM_MAIN $@"
$JAVA $JVMARGS -cp "$TOTEM_BIN" -Djava.ext.dirs=$EXTDIRS -Djava.library.path=$LIBRARYPATH $TOTEM_MAIN $@
