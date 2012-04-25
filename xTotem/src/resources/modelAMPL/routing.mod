# Multi Commodity routing problem

# ***********************
# SETS
# ***********************

set VERTICES;
# set of vertices of the network

set COMMODITIES;
# set of commodities of the network

# ***********************
# PARAMETERS
# ***********************

param Incidence {i in VERTICES, j in VERTICES} default 0;
# incidence matrix i, j [ = 1 if (i, j) belong to A and 0 otherwise ]

param Capa {i in VERTICES, j in VERTICES} default 0;
# capacity on edge i, j

param ComValue {i in VERTICES, k in COMMODITIES} default 0;


# ***********************
# VARIABLES
# ***********************

var flow {i in VERTICES, j in VERTICES, k in COMMODITIES} >=0;


# ***********************
# OBJECTIVE FUNCTION
# ***********************

minimize cost: sum{i in VERTICES, j in VERTICES, k in COMMODITIES} flow[i,j,k];


# ***********************
# CONSTRAINTS
# ***********************

subject to flowC{i in VERTICES, k in COMMODITIES}:
(sum{j in VERTICES} flow[i,j,k] * Incidence[i,j]) - 
(sum{j in VERTICES} flow[j,i,k] * Incidence[j,i]) = ComValue[i,k];


subject to capaC{i in VERTICES, j in VERTICES}:
sum{k in COMMODITIES} flow[i,j,k] <= Capa[i,j];



