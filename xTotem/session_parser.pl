#!/usr/bin/perl

if (@{ARGV} != 2) {
	die "Usage: ${0} result output_file_name\n";
}

${result} = ${ARGV}[0];
${outout_file} = ${ARGV}[1];

open OUT_FILE, ">", "${outout_file}.ses" or die ${!};

${process_msg} = `grep -c '\<neighbor as' ${result}.xml`;
${process_msg} = ${process_msg} / 2; # count session tow times
${process_msg} = "${process_msg},";

print OUT_FILE "${process_msg}";

close OUT_FILE;
