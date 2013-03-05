#!/usr/bin/perl

my ${result};
my ${nb_run};
my ${n_gen};
my ${size_p};
my ${size_of};
my ${p_mut};
my ${p_cross};
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

	if (${line} =~ /.*Parameters: (.*)\t(.*)\t(.*)\t(.*)\t(.*)\t(.*)\r\n/) {
		${nb_run} = $1;
		${n_gen} = $2;
		${size_p} = $3;
		${size_of} = $4;
		${p_mut} = $5;
		${p_cross} = $6;
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
		print "${separator_size},${ssize_over_gsize},,,,,,${balanced},${nb_run},${n_gen},${size_p},${size_of},${p_mut},${p_cross}${components}\n";
		${nb_run} = "";
		${n_gen} = "";
		${size_p} = "";
		${size_of} = "";
		${p_mut} = "";
		${p_cross} = "";
		${separator_size} = "";
		${ssize_over_gsize} = "";
		${components} = "";
		${balanced} = "";
	}

}

close IN_FILE;
