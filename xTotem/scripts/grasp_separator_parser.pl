#!/usr/bin/perl

my ${result};
my ${iter};
my ${apha};
my ${beta};
my ${separator_size};
my ${ssize_over_gsize};
my ${components};
my ${balanced};

if (@{ARGV} != 1) {
	die "Usage: ${0} result.xml\n";
}

${result} = ${ARGV}[0];

open IN_FILE, "<", "${result}" or die ${!};

foreach ${line} (<IN_FILE>) {

	if (${line} =~ /.*Parameters: (.*)\t(.*)\t(.*)\t\r\n/) {
		${iter} = $1;
		${alpha} = $2;
		${beta} = $3;
	}
	elsif (${line} =~ /.*Separator: (.*)\t(.*)\t(.*)\r\n/) {
		${separator_size} = $1;
		${ssize_over_gsize} = $2;
		${components_size} = $3;
	}
	elsif (${line} =~ /.*C\d* :(\d*)\r\n/) {
		${components} = "${components},$1";
	}
	elsif (${line} =~ /.*Balanced: (.*)\r\n/) {
		${balanced} = $1
	}
	elsif (${line} =~ /#* FIN TEST \d* #*\r\n/) {
		print "${separator_size},${ssize_over_gsize},,,,,,${balanced},${iter},${alpha},${beta}${components}\n";
		${iter} = "";
		${alpha} = "";
		${beta} = "";
		${separator_size} = "";
		${ssize_over_gsize} = "";
		${components} = "";
		${balanced} = "";
	}

}

close IN_FILE;
