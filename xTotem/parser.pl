#!/usr/bin/perl

my ${result};
my ${outout_file};
my ${table_counter};
my ${first_table_columns}; # Need two variables to order rib in and rib out columns
my ${last_table_columns};
my ${first_table_row};
my ${last_table_row};
my ${process_rib};
my ${process_msg};

if (@{ARGV} != 2) {
	die "Usage: ${0} result output_file_name\n";
}

${result} = ${ARGV}[0];
${outout_file} = ${ARGV}[1];

open IN_FILE, "<", "${result}.bgp" or die ${!};
open OUT_FILE, ">", "${outout_file}.csv" or die ${!};

${process_msg} = `grep -c '^[^#]' ${result}.msg`;
${process_msg} = "${process_msg},";

print OUT_FILE "MSGs,\n";
print OUT_FILE "${process_msg}\n";

foreach ${line} (<IN_FILE>) {

	if (${line} =~ /.*(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\'s tables data.*/) {
		if (! ${first_table_columns} eq "") {
			${last_table_row} = "${last_table_row}${table_counter},";

			print OUT_FILE "${first_table_columns}${last_table_columns}\n";
			print OUT_FILE "${first_table_row}${last_table_row}${process_msg}\n";
		}

		${first_table_columns} = "Router ${1}\n";
		${last_table_columns} = "";
		${first_table_row} = "";
		${last_table_row} = "";
		${table_counter} = 0;
	}
	elsif (${line} =~ /.*RT table data.*/) {
		${first_table_columns} = "${first_table_columns}RT,";
		${table_counter} = 0;
	}
	elsif (${line} =~ /.*RIB table data.*/) {
		${first_table_columns} = "${first_table_columns}RIB,";
		${first_table_row} = "${first_table_row}${table_counter},";
		${table_counter} = 0;
		${process_rib} = "true";
	}
	elsif (${line} =~ /.*RIB in table data with (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}).*/) {
		${first_table_columns} = "${first_table_columns}RIB in $1,";
		if (${process_rib}) {
			${first_table_row} = "${first_table_row}${table_counter},";
		}
		else {
			${last_table_row} = "${last_table_row}${table_counter},";
		}
		${process_rib} = ""; # false
		${table_counter} = 0;
	}
	elsif (${line} =~ /.*RIB out table data with (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}).*/) {
		${last_table_columns} = "${last_table_columns}RIB out $1,";
		${first_table_row} = "${first_table_row}${table_counter},";
		${table_counter} = 0;
	}
	elsif (${line} =~ /^(.> )?(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})/) {
		${table_counter}++;
	}

}

close IN_FILE;
close OUT_FILE;
