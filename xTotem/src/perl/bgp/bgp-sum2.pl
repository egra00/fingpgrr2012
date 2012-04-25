#!/usr/bin/perl -w
# ===================================================================
# @(#)bgp-sum.pl
#
# @author Bruno Quoitin (bqu@info.ucl.ac.be)
# @date 20/09/2004
# @lastdate 04/03/2005
# ===================================================================

use strict;

use Getopt::Long;
use File::Basename;

use constant ROUTEBTOA_PATH => dirname($0)."/route_btoa";
use constant ROUTE_LIMIT => -1;
use constant USE_ASPATH_LENGTH => 1;

# -----[ global options ]--------------------------------------------
my $global_communities= 0;

# -----[ database of all prefixes ]-----
# for each prefix, for each peer that has a route, vector of
#   1. the local preference
#   2. the AS path (length)
#   3. the MED
#   4. the origin (IGP/EGP/INCOMPLETE)
#   5. the next-hop address
#   6. communities
my %prefixes= ();

# -----[ database of atoms ]-----
my %atoms= ();

# -----[ show_info ]-------------------------------------------------
sub show_info($)
{
    my ($msg)= @_;

    print STDERR "Info: \033[31;1m$msg\033[0m\n";
}

# -----[ canonic_prefix ]--------------------------------------------
#
# -------------------------------------------------------------------
sub canonic_prefix($)
{
    my $prefix= shift;

    if ($prefix =~ m/([0-9]+).([0-9]+).([0-9]+).([0-9]+)\/([0-9]+)/) {
	my $num= ((($1*256+$2)*256+$3)*256)+$4;
	$num= $num >> (32-$5);
	$num= $num << (32-$5);
	return "".(($num >> 24) & 255).".".(($num >> 16) & 255).".".
	    (($num >> 8) & 255).".".($num & 255)."/$5";
    }
    die "Error: invalid prefix [$prefix]";
}

# -----[ rib_read ]--------------------------------------------------
# 
# -------------------------------------------------------------------
sub rib_read($)
{
    my $file= shift;

    open(IN, "zcat $file | ".ROUTEBTOA_PATH." -m |") or
	die "Error: unable to read \"$file\"";
    while (<IN>) {
	chomp;
	my @fields= split /\|/;
	
	my $prefix= canonic_prefix($fields[5]);
	my @path= split(/\s+/, $fields[6]);
	my $path_len= scalar(@path);
	my $origin= $fields[7];
	my $nexthop= $fields[8];
	my $pref= $fields[9];
	my $med= $fields[10];
	
	my $vector= [$pref, $path_len, $med, $origin, $nexthop];

	if ($global_communities) {
	    my @communities= split /\s+/, $fields[11];
	    my @sorted_communities= sort @communities;
	    my $comms= join ";", @sorted_communities;
	    push @$vector, ($comms);
	}

	$prefixes{$prefix}{$nexthop}= $vector;

	((ROUTE_LIMIT >= 0) && (scalar(keys %prefixes) > ROUTE_LIMIT)) and last;

    }
    close(IN);
}

# -----[ rib_atoms ]-------------------------------------------------
#
# -------------------------------------------------------------------
sub rib_atoms()
{
    foreach my $prefix (keys %prefixes) {
	
	my $atom_id= "";
	foreach my $peer (keys %{$prefixes{$prefix}}) {
	    my $vector= $prefixes{$prefix}{$peer};
	    $atom_id= $atom_id."$peer;".(join ";", @$vector);
	}

	if (exists($atoms{$atom_id})) {
	    $atoms{$atom_id}{$prefix}= 1;
	} else {
	    $atoms{$atom_id}{$prefix}= 1;
	}
    }
}

# -----[ main ]------------------------------------------------------
my %opts;
my $global_rib;
my $global_ribs_dir;
my $result= GetOptions(\%opts,
		      'rib:s' => \$global_rib, 
			'ribs-dir:s' => \$global_ribs_dir ,
		       'communities!');

if (!$result) {
    print STDERR "USAGE: bgp-sum.pl --rib=FILE\n\n";
    exit(0);
}

if (!defined($global_rib) && !defined($global_ribs_dir)) {
    print STDERR "ERROR: missing option \n";
    print STDERR "\nUSAGE: bgp-sum.pl --rib=FILE || --ribs-dir=DIR\n\n";
    exit(-1);
}

if (exists($opts{communities})) {
    $global_communities= $opts{communities};
}

print "# Generated from \"".$opts{rib}."\"\n";
print "# on ".localtime(time)."\n";
print "# with options:";
($global_communities) and print " communities";
(USE_ASPATH_LENGTH) and print " aspath-length";
(ROUTE_LIMIT >= 0) and print " route-limit=".ROUTE_LIMIT;
print "\n";



show_info("reading rib...");

if (defined ($global_ribs_dir)) {
  opendir(hRibsDir, "$global_ribs_dir") || die;
  my @RibsFiles = grep {/\.gz/ && -f "$global_ribs_dir/$_"} readdir(hRibsDir);
  close (hRibsDir);

  for my $RibDir (@RibsFiles) {
    print "# Generated from \"".$RibDir."\"\n";
    rib_read("$global_ribs_dir/$RibDir"); 
  }
} else {
print "# Generated from \"".$global_rib."\"\n";
rib_read($global_rib);
print "# Number of different prefixes: ".scalar(keys %prefixes)."\n";
}

show_info("computing atoms...");
rib_atoms();

my $num_atoms= scalar(keys %atoms);
show_info("number of different atoms: $num_atoms");
print "# Number of different atoms: $num_atoms\n";
#show_info("clustering ratio: ".(int((10000*$num_atoms)/$num_prefixes)/100)." %");

print "# List of clusters:\n";
my @atoms_sizes;
foreach my $atom (keys %atoms) {
    my @atom_prefixes= sort(keys %{$atoms{$atom}});
    my $atom_size= scalar(@atom_prefixes);
    print "ATOM\t$atom_prefixes[0]\t$atom_size\n";
    push @atoms_sizes, ($atom_size);
}
#my $mean= Stat::stat_mean(\@atoms_sizes);
#my $median= Stat::stat_median(\@atoms_sizes);
#my $perc5= Stat::stat_pth_percentile(\@atoms_sizes, 5);
#my $perc95= Stat::stat_pth_percentile(\@atoms_sizes, 95);
#show_info("distribution: mean=".(int(100*$mean)/100).", median=$median, perc5=$perc5, perc95=$perc95");

print "# List of prefixes:\n";
foreach my $atom (keys %atoms) {
    my @atom_prefixes= sort keys %{$atoms{$atom}};
    foreach my $prefix (@atom_prefixes) {
	print "PREFIX\t$prefix\t$atom_prefixes[0]\n";
    }
}
