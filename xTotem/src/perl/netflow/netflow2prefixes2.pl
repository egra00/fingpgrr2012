#!/usr/bin/perl
#
# @author: ????
# @author: Gael Monfort (monfort@run.montefiore.ulg.ac.be)
#
# @lastdate: 14-Sept.-2007
#
# Produce netflow aggragated files in the format
#    src_prefix dst_prefix flow_size
# in the dest-dir with the same directory structure as in the netflow-dir
#
# Arguments:
#   rib-dir: Directory holding RIB ASCII files (can be gzipped)
#   netflow-dir: Directory holding netflow data
#   dest-dir: Destination directory (should exists)
#   last-dir: Optional. Directory path to use under the netflow directory (others are omitted)
#

use strict;

use File::Basename;
use File::Path;
use File::Find;
use File::Spec;
use Cwd;
use lib dirname($0).'/perl/5.8.6/i686-linux-thread-multi';
use lib dirname($0).'/perl';
use Symbol;
use Date::Manip;
use Net::Patricia;# structure for BGP routing table

use constant FLOWTOOLSDIR => File::Spec->rel2abs(dirname($0)."/flow-tools");

# -----[ ingress routers to IP conversion ]-----
# Just to have all the directories where the netflow lies.
my %ingress_to_IP= (
		'ATLA' => '198.32.12.9',
		'DNVR' => '198.32.12.41',
		'HSTN' => '198.32.12.57',
		'IPLS' => '198.32.12.177',
		'KSCY' => '198.32.12.89',
		'LOSA' => '198.32.12.105',
		'NYCM' => '198.32.12.121',
		'SNVA' => '198.32.12.137',
		'STTL' => '198.32.12.153',
		'WASH' => '198.32.12.169',
);

if (@ARGV != 3 && @ARGV != 4) {
  print "Usage: ".basename($0)." rib-dir netflow-dir dest-dir [last-dir]\n";
  exit 0;
}

die "Destination directory does not exists" unless -d $ARGV[2];
my $workingdir = getcwd();
my $srcdir = File::Spec->rel2abs($ARGV[1], $workingdir);
my $ribdir = File::Spec->rel2abs($ARGV[0], $workingdir); 

# For BGP routing table loaded when having to find out the best matching prefixes for each destination IP.
my $bgp = new Net::Patricia;

print "Reading BGP RIB to convert destination IPs into prefixes\n";
my %opts = ( 'wanted' => \&read_RIB, 'no_chdir' => 1);
chdir($ribdir);
foreach my $dir (keys %ingress_to_IP){
    finddepth(\%opts,$dir);
};

print "end of rib read\n";
################################################################################################
# Reading the traffic files per ingress node.
################################################################################################
# The first thing to be done is reading the traffic files since it is based on this information that
# we choose which prefixes we are going to tweak during the optimization. So we compute the cumulative
# traffic per prefix and then take the largest prefixes that capture x % of the total traffic. 

# We read the traffic files for each ingress independently as several ingresses might have
# files with the same timestamp, so we iterate on the ingresses and re-initialize the TRAFFICFILELIST
# hash at each iteration.
my %opts = ( 'wanted' => \&read_traffic, 'no_chdir' => 1);
chdir($srcdir);
foreach my $dir (keys %ingress_to_IP){
    #print "$ARGV[1]/$dir\n";
    finddepth(\%opts,"$dir");
};


###################
# Reads BGP table
###################
# Subroutine that reads a BGP routing table and builds a tree with all known prefixes.

sub read_RIB {
    my $RIB = $File::Find::name;
    if (-f $RIB) {
      if ($RIB =~ m/\.gz$/) {
          print "Reading compressed rib from $RIB\n";
          if (!defined(open (IN, "zcat $RIB |"))) {
              print "Can't open file $RIB for reading.\n";
	      return;
	  }
      } else {
          print "Reading rib from $RIB\n";
          if (!defined(open (IN, "< $RIB"))) {
              print "Can't open file $RIB for reading.\n";
	      return;
          }
      }
      
      my $CURRENTLINE;
      while (defined($CURRENTLINE=<IN>)){
	  my @MYLIST = split(/[|]/,$CURRENTLINE);
	  # We only take into account prefixes that can be reached through the Internet peerings.
	  $bgp->add_string($MYLIST[5],"$MYLIST[5]");
	  #print "Added prefix ",$bgp->match_exact_string($MYLIST[5]),"\n";
     };
     close(IN);
  }
}


############################
# Initializes traffic info
############################
# This subroutine takes the GEANT netflow files, matches the source and destination prefix, and writes
# an output file for each input one with only per prefix stats. What we do is that we aggregate everything
# at the timescale of minutes (we won't be more precise anyway) and once we are finished reading the file
# we write for each scr prefix dst prefix pair the minutes at which it was active and the bytes it had : 
# UNIX time, source prefix, destination prefix, bytes.

sub read_traffic {
    my $file = $File::Find::name;

    # change the current directory to be in the source directory
    chdir($srcdir);

    if (-f $file) {
        # First finding out the ingress router it corresponds to.
        my %traffic= ();
        my %pair = ();
        my $mintime = 999999999999;
        my $maxtime = 0;
        my @NAME = split(/\//,$file);
        if (!defined($ARGV[3]) or dirname($file) =~ m/\/$ARGV[3]$/) {
            #print "Reading traffic file $file\n";

            my $ingress = $ingress_to_IP{$NAME[0]};
            #print "Ingress is $ingress\n";
	   
            my $pid;
            if (not defined($pid = open(IN, "-|"))) {
                die "can't fork: $!";
            }
            if ($pid) {
                # parent process - do nothing
            } 
            else {
                # child process
                system(FLOWTOOLSDIR."/flow-cat $file | ".FLOWTOOLSDIR."/flow-print -f 5 ");
                exit 0;
            };
            # Skipping the first two lines.
            my $CURRENTLINE=<IN>;
            $CURRENTLINE=<IN>;
            while (defined($CURRENTLINE=<IN>)){
                chop($CURRENTLINE);
                my @MYLIST = split(/[\s]+/,$CURRENTLINE);
                # Structure: Start, End, Src If, Src IP, SrcP, Dest If, Dst IP, DstP, P, Fl, Pkts, Octets
                # So first, finding out which destination prefix it matches.
                my $srcpref = $bgp->match_string($MYLIST[3]);
                my $dstpref = $bgp->match_string($MYLIST[6]);
                # Now transforming start time into Unix time.
#               my @START = split(/\.|\:/,$MYLIST[0]);
#               my $year = $NAME[3];
#               my $month = substr($START[0],0,2);
#               my $day = substr($START[0],2,2);
#               my $hour = $START[1];
#               my $minutes = $START[2];
#               my $seconds = $START[3];
                # Base UNIX time is as follows.
#               my $STARTTIME = &Date_SecsSince1970($month,$day,$year,$hour,$minutes,0);
#               $STARTTIME = int($STARTTIME/60);
                # Now transforming start time into Unix time.
#               my @END = split(/\.|\:/,$MYLIST[1]);
                #my $year = $NAME[3];
#               my $month = substr($END[0],0,2);
#               my $day = substr($END[0],2,2);
#               my $hour = $END[1];
#               my $minutes = $END[2];
#               my $seconds = $END[3];
                # Base UNIX time is as follows.
#               my $ENDTIME = &Date_SecsSince1970($month,$day,$year,$hour,$minutes,0);
#               $ENDTIME = int($ENDTIME/60);
                if ((defined($srcpref))&&(defined($dstpref))){
                    # Ok, found matching prefixes. Attribute traffic to ingress and prefix.
#                   foreach $time ($STARTTIME..$ENDTIME){
#                       $traffic{$srcpref}{$dstpref}{$time}+=$MYLIST[11]/(abs($ENDTIME-$STARTTIME)+1);
#                       if ($time > $maxtime){
#                           $maxtime = $time;
#                        };
#                        if ($time < $mintime){
#                            $mintime = $time;
#                        };
#                   };
                    # Mark src dst prefix pair as seen.
                    $pair{"$srcpref $dstpref"} = 1;
                    $traffic{$srcpref}{$dstpref}+=$MYLIST[11];
                }
                else {
                    #print "Could not match prefix to src or dst IPs $MYLIST[3] ($srcpref) $MYLIST[6] ($dstpref)\n";
                };
            }; # end of while
            close(IN);
            # Good, so now we write the output file and put the aggregated flows there.
            # The name of the output file is just a given prefix (like /home/suhlig/traffic/)
            # to which we append everything but the first part of the file name.
	    
	    # Restore the current directory
	    chdir($workingdir);
	    die "Destination directory does not exists" if (not -d $ARGV[2]);
	    
	    my $OUTNAME = $ARGV[2]."/".$file;
	    mkpath(dirname($OUTNAME)); 
    
            print "Writing output file to \"".Cwd::abs_path($OUTNAME)."\"\n";
	    
            open(OUT,">$OUTNAME")|| die "cannot open output aggregated traffic file: $!\n";
            foreach my $pair (keys %pair){
                my ($src,$dst) = split(/[\s]+/,$pair);
                print OUT "$src $dst ",$traffic{$src}{$dst},"\n";
#               foreach my $time ($mintime..$maxtime){ 
#                   if (defined($traffic{$src}{$dst}{$time})){
#                       print OUT ($time*60)," $src $dst ",$traffic{$src}{$dst}{$time},"\n";
#                   };
#               };
            };
            close(OUT);
            # Then we compress this ASCII file
            system("gzip -9 $OUTNAME");
        }; # file not to exclude
    }; # is a file
}
