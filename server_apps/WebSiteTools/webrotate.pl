#!/private/bin/perl
#
# Hacked up web log rotate script by Clif Cox 11/30/98
# 2001/01/02, clements.  Modified to use httpd in new location.
#

#	Files
$prefix		=	"/private/apps/apache";
$logs		=	"$prefix/logs/zfin_";
$old_dir	=	"old";
$pidfile	=	"$prefix/logs/httpd.pid";
$DOMLU		=	"<!--|ROOT_PATH|-->/server_apps/WebSiteTools/domainlookup.pl";
$BZIP		=	"/local/bin/bzip2";


@months		=	(Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec);

# Get date
($u, $u, $u, $day, $mon, $year) = localtime(time);
--$year unless $mon; $year %= 100;
$date = sprintf("%s%02d", $months[($mon-1)%12], $year);

($pid = `cat $pidfile` + 0) || die "Couldn't read $pidfile: $!\n";

#	Move and rename all files in this dir to the old dir

	opendir(DIR, "$logs");
	foreach $log (readdir(DIR)) {
		$lg = "$logs/$log";
		next unless -f $lg && $log ne "httpd.pid";

		rename($lg, "$logs/$old_dir/$log.$date") || die "Error renameing log file.\n";
		$files[@files] = "$logs/$old_dir/$log.$date";
	} close DIR;   

kill (USR1, $pid);		# Ask the server to start using the new logs
				# May cause problems with older servers

sleep 1500;			# Wait a good amount of time for all children to exit


#	Now we look up all unresolved domainnames, and
#	compress the old logs so they don't take up so much space.

system("$DOMLU " . join(" ", @files));
system("$BZIP  " . join(" ", @files));

#       All Done!
