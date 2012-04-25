#  MPLS Tunnel reoptimization program 
#  Author Sandford Bessler, ftw, 2002, 2003
#
#############################################

# solve the integer REOPT program
# perform independent experiments  
 
set LINKS;	# directional links of the whole network
set K;  	# commodities defined by source, destination nodes, traffic class)
param  P{K};    # number of predefined paths for each commodity

param res_cap;
 
param PATH {i in K,  LINKS, j in 1..P[i] } binary; # predefined path alternatives as set of included links
param demand {K}>= 0;             # current traffic demand vector, one per commodity
param initial_demand {K};          
 
param cap{LINKS} >= 0 ;
param M := 1000;    #large number

 
param delta;


param change_cost{i in K, j in 1..P[i]};  # costs to change the capacity of a pipe
  
param earn{i in K, j in 1..P[i]};   # earnings for carrying one flow unit
param x0 {i in K, j in 1..P[i]};  # capacities of existing pipes (calculated in the previous planning step)

var   x{i in K, j in 1..P[i]} ; # capacities of the pipes - to be found

var  y  {i in K, j in 1..P[i]}, binary; # decision variable - if the pipe x is to be changed or not
var  z  {i in K, j in 1..P[i]}, binary; # decision variable

maximize initial:       sum {i in K,  j  in  1..P[i]}  (x[i,j] * earn[i,j]); 
 


subject to total_cap {u in LINKS }:  sum {i in K, j in 1..P[i]} PATH[i, u, j] * x[i,j] <= cap[u];

subject to total_demand {i in K}:    sum {j in 1..P[i]} x[i,j] <= demand[i] ;

subject to nonnegative  {i in K, j in 1..P[i]} : x[i,j] >=0;

end;
