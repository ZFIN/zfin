#!/local/bin/perl -s
#	Script to call ontape to start continuous log backups
#
#	Clif Cox	8/4/99
#
#	$Author$	$Date$	$Revision$
#	$Source$

# No command line args.

umask(002);	# Set Umask

$INFORMIXDIR      = "/private/apps/Informix/informix_wildtype";
$INFORMIXSERVER   = "wildtype";
$ONCONFIG         = "onconfig.wildtype";
$INFORMIXSQLHOSTS = "$INFORMIXDIR/etc/sqlhosts.wildtype";

$ENV{"INFORMIXDIR"}      = $INFORMIXDIR;
$ENV{"INFORMIXSERVER"}   = $INFORMIXSERVER;
$ENV{"INFORMIXSQLHOSTS"} = $INFORMIXSQLHOSTS;
$ENV{"ONCONFIG"}         = $ONCONFIG;
$ENV{"LD_LIBRARY_PATH"}  = "$INFORMIXDIR/lib:$INFORMIXDIR/lib/esql";

$BACKUP         =       "/export/chromix/backup";
$LOGLINK	=	"logs";

$ONTAPE		=	"$INFORMIXDIR/bin/ontape";
$TOUCH		=	"/usr/bin/touch";

# Get date
($u, $u, $u, $day, $mon, $year) = localtime(time);
$date  = sprintf("%02d%02d%02d", $year%100, $mon+1, $day);

chdir $BACKUP;

$LOGFILE = "log$date";

if (-e $LOGFILE) {
        local $n = 0;

	while (-e "$LOGFILE.$n") { ++$n }
        $LOGFILE .= ".$n";
}

touch($LOGFILE);
chmod 0660, $LOGFILE;
system("chown -h informix:informix $LOGFILE");

unlink  $LOGLINK;
symlink $LOGFILE, $LOGLINK;

system("echo | $ONTAPE -c &");

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
