#!/usr/bin/perl

if (@{ARGV} != 1) {
	die "Usage: ${0} topologie.xml\n";
}

${result} = ${ARGV}[0];
${outout_file} = ${ARGV}[1];

${nodes} = `grep -c '\<node id' ${result}`;
${links} = `grep -c '\<link id' ${result}`;

print "Nodes: ${nodes}Links: ${links}";
