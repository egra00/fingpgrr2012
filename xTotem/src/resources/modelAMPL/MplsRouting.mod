# MPLS routing

# ***********************
# SETS
# ***********************

set VERTICES;       # set of vertices of the network
set LINKS;          # set of links of the network

# ***********************
# PARAMETERS
# ***********************

param InLinks{i in VERTICES, j in LINKS} default 0;     # links entering the node
param OutLinks{i in VERTICES, j in LINKS} default 0;    # links leaving the node
param Capa {l in LINKS} default 0;                      # capacity of links
param Demand {i in VERTICES, j in VERTICES} default 0;  # traffic demand between nodes i and j
param rho {i in VERTICES, j in VERTICES} := (if (i = j) then 1 else 0);

# ***********************
# VARIABLES
# ***********************

var x {u in VERTICES, v in VERTICES, l in LINKS} binary;
var utilization {l in LINKS} >= 0;
var maxUtil >=0;

# ***********************
# OBJECTIVE FUNCTION
# ***********************

minimize cost: maxUtil;

# ***********************
# CONSTRAINTS
# ***********************

subject to flowConservationC{u in VERTICES, v in VERTICES, n in VERTICES}:
    rho[u,n] + (sum{l in LINKS} x[u,v,l] * InLinks[n,l]) = rho[v,n] + (sum{l in LINKS} x[u,v,l] * OutLinks[n,l]);

subject to utilizationC{l in LINKS}:
    utilization[l] = ((sum{u in VERTICES, v in VERTICES} x[u,v,l] * Demand[u,v]) / Capa[l]);

subject to maxUtilizationC{l in LINKS}:
    utilization[l] <= maxUtil;

end;



