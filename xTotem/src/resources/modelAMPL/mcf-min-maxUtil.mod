# Multi Commodity routing problem

# ***********************
# SETS
# ***********************

set VERTICES;       # set of vertices of the network
set LINKS;          # set of links of the network
set COMMODITIES;    # set of commodities of the network

# ***********************
# PARAMETERS
# ***********************

param InLinks{i in VERTICES, j in LINKS} default 0; # links entering the node
param OutLinks{i in VERTICES, j in LINKS} default 0; # links leaving the node
param Capa {l in LINKS} default 0; # capacity of links
param ComValue {i in VERTICES, k in COMMODITIES} default 0;

# ***********************
# VARIABLES
# ***********************

var flow {l in LINKS, k in COMMODITIES} >=0;
var utilization {l in LINKS} >= 0;
var maxUtil >=0;

# ***********************
# OBJECTIVE FUNCTION
# ***********************

minimize cost: 1000 * maxUtil;

# ***********************
# CONSTRAINTS
# ***********************

subject to flowC{i in VERTICES, k in COMMODITIES}:
    (sum{l in LINKS} flow[l,k] * OutLinks[i,l]) - (sum{l in LINKS} flow[l,k] * InLinks[i,l]) = ComValue[i,k];

#subject to capaC{l in LINKS}:
#    sum{k in COMMODITIES} flow [l,k] <= Capa[l];

subject to utilizationC{l in LINKS}:
    utilization[l] = ((sum{k in COMMODITIES} flow[l,k]) / Capa[l]);

subject to maxUtilizationC{l in LINKS}:
    utilization[l] <= maxUtil;

end;



