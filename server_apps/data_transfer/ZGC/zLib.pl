#!/local/bin/perl 
#  Script to create zebrafish library load file.
#  delimiter = |

system("/bin/rm -f zLib_not_found");
system("/bin/rm -f *.previous");


#make room for the new version 
system("touch zfishlib_info.shtml");
system("/bin/mv -f zfishlib_info.shtml zfishlib_info.shtml.previous");


#download the new version
system("wget 'http://image.llnl.gov/image/html/zfishlib_info.shtml");


#Compare new vs. old... load changes
$vLibDiff = `diff zfishlib_info.shtml zfishlib_info.shtml.previous`;


if ($vLibDiff ne "")
{
	open (LIB, "zfishlib_info.shtml") or die "can not open zfishlib_info.shtml";
	open (UNL, ">fishlib.unl") or die "can not open fishlib.unl";

	while ($line = <LIB>)
	{
	  if ($line =~ /^NAME: (.*)<BR>/) { print UNL "$1||\n"; }
	}

	close (LIB);
	close (UNL);
}

exit;




