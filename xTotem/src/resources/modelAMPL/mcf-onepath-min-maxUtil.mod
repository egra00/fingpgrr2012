# Multi Commodity routing problem

# ***********************
# SETS
# ***********************

set VERTICES;       # set of vertices of the network
set LINKS;          # set of links of the network

# ***********************
# PARAMETERS
# ***********************

param InLinks{i in VERTICES, j in LINKS} default 0; # links entering the node
param OutLinks{i in VERTICES, j in LINKS} default 0; # links leaving the node
param Capa {l in LINKS} default 0; # capacity of links
param Demand {i in VERTICES, j in VERTICES} default 0;  # traffic demand between nodes i and j

# ***********************
# VARIABLES
# ***********************

var flow {l in LINKS, i in VERTICES, j in VERTICES} binary;
var utilization {l in LINKS} >= 0;
var maxUtil >=0;

# ***********************
# OBJECTIVE FUNCTION
# ***********************

minimize cost: maxUtil;

# ***********************
# CONSTRAINTS
# ***********************

subject to flowC{k in VERTICES, i in VERTICES, j in VERTICES}:
    (sum{l in LINKS} flow[l,i,j] * OutLinks[k,l]) - (sum{l in LINKS}
    flow[l,i,j] * InLinks[k,l]) = (if (k = i) then Demand[i,j] else (if (k=j)
    then -Demand[i,j] else 0));

#subject to capaC{l in LINKS}:
#    sum{k in COMMODITIES} flow [l,k] * ComValue[k] <= Capa[l];

subject to utilizationC{l in LINKS}:
    utilization[l] = ((sum{i in VERTICES, j in VERTICES} flow[l,i,j] *
    Demand[i,j]) / Capa[l]);

subject to maxUtilizationC{l in LINKS}:
    utilization[l] <= maxUtil;

end;



