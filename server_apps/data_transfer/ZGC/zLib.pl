#!/private/bin/perl 
#  Script to create zebrafish library load file.
#  delimiter = |

system("/bin/rm -f zLib_not_found");
system("/bin/rm -f *.previous");


#make room for the new version 
system("touch zfishlib_info.shtml");
system("/bin/mv -f zfishlib_info.shtml zfishlib_info.shtml.previous");
system("/bin/mv -f StaticLibList.unl StaticLibList.unl.previous");

#download the new version
system("/local/bin/wget -q 'http://zgc.nci.nih.gov/Tissues/StaticLibList?PAGE=0&ORG=Dr&STATUS=Confirmed' -O StaticLibList");

open (LIB, 'StaticLibList') or die "can not open StaticLibList";
open (UNL, ">StaticLibList.unl") or die "can not open StaticLibList.unl";

while ($line = <LIB>)
{
  chop $line;
  ($name,$tis,$vec,$num) = split(/\t/,$line,4);
  print UNL "$name|$tis|$vec|$num|\n" if ($num =~ /[0-9]/);
}

close (LIB);
close (UNL);


#download the new version
system("/local/bin/wget -q 'http://image.llnl.gov/image/html/zfishlib_info.shtml'");


#Compare new vs. old... load changes
$vLibDiff = `diff zfishlib_info.shtml zfishlib_info.shtml.previous`;


if ($vLibDiff ne "")
{
	open (LIB, "zfishlib_info.shtml") or die "can not open zfishlib_info.shtml";
	open (UNL, ">fishlib.unl") or die "can not open fishlib.unl";

	while ($line = <LIB>)
	{
	  if ($line =~ /^NAME: (.*)<BR>/) { print UNL "$1||"; }
	  if ($line =~ /^VECTOR: (.*)<BR>/) { print UNL "$1|"; }
	  if ($line =~ /^V_TYPE: (.*)<BR>/) { print UNL "$1|\n"; }
	}

	close (LIB);
	close (UNL);
}

exit;




