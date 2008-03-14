#!/private/bin/perl -s
#
# Does an Informix dump of the database.  ZFIN typically runs this once a
# night.  The dump produced by this script can be used in conjunction with
# the logs to restore the database to a previous dump and then roll it
# forward using the log.
#
#	Clif Cox	8/4/99
#	Arthur Kirkpatrick 2000/03/10

umask(002);	# Set Umask

$| = 1;		# Flush after every write

$INFORMIXDIR      = "<!--|INFORMIX_DIR|-->";
$INFORMIXSERVER   = "<!--|INFORMIX_SERVER|-->";
$ONCONFIG         = "<!--|ONCONFIG_FILE|-->";
$INFORMIXSQLHOSTS = "$INFORMIXDIR/etc/<!--|SQLHOSTS_FILE|-->";

$ENV{"INFORMIXDIR"}      = $INFORMIXDIR;
$ENV{"INFORMIXSERVER"}   = $INFORMIXSERVER;
$ENV{"INFORMIXSQLHOSTS"} = $INFORMIXSQLHOSTS;
$ENV{"ONCONFIG"}         = $ONCONFIG;
$ENV{"LD_LIBRARY_PATH"}  = "$INFORMIXDIR/lib:$INFORMIXDIR/lib/esql";

$BACKUP		=	"<!--|ROOT_PATH|-->/server_apps/DB_maintenance";
$DATALINK	=	"data";

$ONTAPE		=	"$INFORMIXDIR/bin/ontape";
$TOUCH		=	"/usr/bin/touch";
$CP		=	"/bin/cp";

@backup_list	=	($ONCONFIG);

foreach $file (@backup_list) {
		system("$CP $INFORMIXDIR/etc/$file $BACKUP") &&
		die "Can't copy file $!";
}

# Get date
($u, $u, $u, $day, $mon, $year) = localtime(time);
$date  = sprintf("%02d%02d%02d", $year%100, $mon+1, $day);

chdir $BACKUP;

#  For space reasons, delete all previous versions of the backup.
#  They should have been backed up by the systems backup in the
#  24 hours since our last run.
my @files = <data[0-9]*>;
unlink @files;

$DATAFILE = "data$date";

touch($DATAFILE);
chmod 0660, $DATAFILE;
system("chown -h informix:informix $DATAFILE");

unlink  $DATALINK;
symlink $DATAFILE, $DATALINK;

system("echo `/bin/date`: Starting Informix backup of $INFORMIXSERVER");
system("echo | $ONTAPE -s -L 0");
system("echo `/bin/date`: Finished Informix backup of $INFORMIXSERVER");

exit;					# All done!


#################################################################################
#                                                                               #
#       Functions and routines                                                  #
#                                                                               #
#################################################################################


#	Set a file's mtime to $time
sub touch {
	local ($file, $time) = @_;
	local ($date);

	$time = time unless $time;	# Do we realy have the time?

	local ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) =
		localtime($time);

	$date = sprintf("%02d%02d%02d%02d%02d.%02d",
			$year%100, $mon+1, $mday, $hour, $min, $sec);
#	print "touch date = $date\n";

#	print "/usr/local/bin/touch -t $date $file\n";
	system("$TOUCH -m -t $date \'$file\'\n") &&
		die "Couldnt touch file: $file $!";
}


### All Done! ###
