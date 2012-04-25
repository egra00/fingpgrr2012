#!/usr/bin/perl

#
# Converts the compressed rib files into compressed ASCII readable ribs.
#
# ARGS:
#   - rib directory
#   - destination directory
#   - [destination filename]
#   
#   The rib directory will be searched for .gz files. These files will be passed to the route_btoa script.
#   The result will be put in a file in the same directory structure as the origina ones.
#   If the destination filename parameter is not given, the new file name will have the same name as the original
#   one but its extension will be changed to .ascii.gz.
#   If the destination filename is given, there should be only one .gz file in each directory and the new files 
#   will be named according to the given filename (with .gz extension added).
#   

use strict;

use Cwd;
use File::Spec;
use File::Find;
use File::Path;
use File::Basename;

use constant ROUTEBTOA_PATH => File::Spec->rel2abs(dirname($0)."/route_btoa");


if (@ARGV != 2 && @ARGV != 3) {
  print "Usage: ".basename($0)." rib-base-dir destination-base-dir [destination-filename]\n";
  exit 0;
}

# create the destination directory
mkpath($ARGV[1]);

# search for files from base directory
my (%opts) = ( 'wanted' => \&read_file,
               'no_chdir' => 1 );

my($basedestdir) = File::Spec->rel2abs($ARGV[1]);

chdir($ARGV[0]);
finddepth(\%opts, "./");

sub read_file($) {
  my $file = $File::Find::name;
 
  if (-f $file && $file =~ m/\.gz$/) {
   my($newname) = $file;
   if (@ARGV == 2) {
      $newname =~ s/\.gz$/\.ascii/;
   } else {
      $newname = File::Spec->catfile(dirname($newname), $ARGV[2]);
   }
    my($destfile) = File::Spec->catfile(File::Spec->abs2rel($basedestdir),$newname);
	
    my($cwd) = getcwd();
    #print "Paths relative to: $cwd\n";
    #print "File: $file\n";
    #print "Destfile: $destfile\n";
  
    if (!defined(open(IN, "zcat $file | ".ROUTEBTOA_PATH." -m |"))) {
      print "Error: unable to read \"$file\"\n";
      return;
    }

    mkpath(dirname($destfile));

    if (!defined(open(DEST, "> $destfile"))) {
    	print "Error: unable to open dest file \"$destfile\" relative to $cwd\n";
	return;
    }

    print DEST <IN>;

    print "File ".Cwd::abs_path($destfile)." written\n";
    close(OUT);
    close(IN);

    print "Compressing file\n";
    system("gzip -9 $destfile");
  }
}


