#!/usr/bin/perl

if (@{ARGV} != 1) {
	die "Usage: ${0} topologie.xml\n";
}

${result} = ${ARGV}[0];

${nodes} = `grep -c '\<node id' ${result}`;
${links} = `grep -c '\<link id' ${result}`;

open IN_FILE, "<", "${result}" or die ${!};

%hash = ();

loop_exit: {
	foreach ${line} (<IN_FILE>) {

		if (${line} =~ /.*<link id="(.*)[-_](.*)">.*/) {
			if ( ! exists $hash{$1} ) {
				$hash{$1} = 0;
			}
			if ( ! exists $hash{$2} ) {
				$hash{$2} = 0;
			}

			$hash{$1} = $hash{$1} + 1;
			$hash{$2} = $hash{$2} + 1;
		}
		elsif (${line} =~ /.*igp.*/) {
			last loop_exit;
		}
	}
}

close IN_FILE;

$grado = "";
foreach (sort { ($a cmp $b) } keys %hash) {
	$grado = "$grado,$hash{$_}";
}

$nodes=~s/\R\z//;
$links=~s/\R\z//;

print "${nodes},${links},,,,$grado,\n";
