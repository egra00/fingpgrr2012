# General Routing Problem Using Piecewise Linear Objective Function
# Last modification 20-March-2008
# Author Hakan Umit (hakan.umit@uclouvain.be)

# ***********************
# SETS
# ***********************

set NODES;       # set of nodes in the network
set LINKS;       # set of arcs in the network

# ***********************
# PARAMETERS
# ***********************

param TailNode{a in LINKS}; # Tail node of arcs
param HeadNode{a in LINKS}; # Head node of arcs
param Cap{a in LINKS}; # Capacity of arcs
param Dem{o in NODES, d in NODES}; # Demand between nodes o and d

# ***********************
# VARIABLES 
# ***********************

var flow {a in LINKS, t in NODES} >=0;
var load {a in LINKS} >=0;
var phi_of_arc {a in LINKS} >= 0;

# ***********************
# OBJECTIVE FUNCTION
# ***********************

minimize phi_total: sum{a in LINKS}phi_of_arc[a];

# ***********************
# CONSTRAINTS
# ***********************

subject to phi_ctr1{a in LINKS}:
phi_of_arc[a] >= load[a];

phi_ctr2{a in LINKS}:
phi_of_arc[a] >= 3 * load[a] - 2/3 * Cap[a];

phi_ctr3{a in LINKS}:
phi_of_arc[a] >= 10 * load[a] - 16/3 * Cap[a];

phi_ctr4{a in LINKS}:
phi_of_arc[a] >= 70 * load[a] - 178/3 * Cap[a];

phi_ctr5{a in LINKS}:
phi_of_arc[a] >= 500 * load[a] - 1468/3 * Cap[a];

phi_ctr6{a in LINKS}:
phi_of_arc[a] >= 5000 * load[a] - 16318/3 * Cap[a];

load_ctr{a in LINKS}:
load[a] = sum{t in NODES}flow[a,t];

flow_ctr{i in NODES,d in NODES}:
sum{a in LINKS:TailNode[a]=i}flow[a,d] - sum{a in LINKS:HeadNode[a]=i}flow[a,d]= if (i=d) then -sum{s in NODES}Dem[s,d]
									       else (if (i!=d) then Dem[i,d]);
end;
