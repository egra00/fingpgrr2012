#!/usr/bin/perl -w
#
# This program builds a RIB file for a router whose RIB is missing
# The resulting RIB file will contain the eBGP routes deduced from the iBGP
# routes from the other routers.
# 
# Specify the IP address of the router for which you want to generate th RIB
# The program will take every routes for which the specified IP is the next-hop,
# put the specified IP in the ROUTER_IP field, and change the next-hop to a
# fake address (one for each AS).
#
# output the new RIB on standard output
#

use strict;

use constant ROUTER_IP => 3;
use constant NEXT_HOP => 8;
use constant AS_PATH => 6;
use constant PREFIX => 5;

if (@ARGV < 2) {
    print "Usage: $0 <IP> <RIB-files>\n";
    exit 0;
}

if ($ARGV[0] =~ m/^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$/) {
    print STDERR "Looking for IP: $ARGV[0]\n";
} else {
    print "Second argument is not an IP address: $ARGV[0]\n";
    exit 0;
}

my($ip) = $ARGV[0];
shift;

########################
my(%asIPs);

my($count)=3;
my(@ipArray) = (10, 0, 0, 0);

sub getip($) {
    my $as = shift;
    my $asIP = $asIPs{$as};
    if (!defined($asIP)) {
        # create a new IP address and add it to the hashmap
        while ($count > 0 && $ipArray[$count] >= 255) {
            $count--;
        }
        $ipArray[$count]++;
        $asIP = "$ipArray[0].$ipArray[1].$ipArray[2].$ipArray[3]";
        #print "$asIP\n";
        $asIPs{$as} = $asIP;
    }
    return $asIP;
}
########################

my(%prefixes);
# change output separator
$,="|";
my($file);
foreach $file (@ARGV) {
    if (!open (IN, "$file")) {
        print STDERR "Can't open RIB file: $file.";
        next;
    } else {
        print STDERR "Using file: $file\n";
    }
    while(<IN>) {
        chomp;
        my(@line) = split /\|/;
        #print "IP: ".$line[ROUTER_IP]." nexthop: ".$line[NEXT_HOP]." as_path:".$line[AS_PATH]."\n";
        if ($line[NEXT_HOP] eq $ip && !exists($prefixes{$line[PREFIX]})) {
            $line[ROUTER_IP] = $ip;
            my($as) = split(/ /, $line[AS_PATH]);
            #print "AS: $as\n";
            $line[NEXT_HOP] = getip($as);
            $prefixes{$line[PREFIX]} = [ @line ];
        }
    }
}

my($key);
for $key (keys %prefixes) {
    print @{ $prefixes{$key} };
    print "\n";
}


