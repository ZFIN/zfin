#!/local/bin/perl -s
#	Script to call ontape to do some simple backups
#
#	Clif Cox	8/4/99
#	Arthur Kirkpatrick 2000/03/10
#
#	$Author$	$Date$	$Revision$
#	$Source$

umask(002);	# Set Umask

$| = 1;		# Flush after every write

$INFORMIXDIR      = "/private/apps/Informix/informix_wildtype";
$INFORMIXSERVER   = "wildtype";
$ONCONFIG         = "onconfig.wildtype";
$INFORMIXSQLHOSTS = "$INFORMIXDIR/etc/sqlhosts.wildtype";

$ENV{"INFORMIXDIR"}      = $INFORMIXDIR;
$ENV{"INFORMIXSERVER"}   = $INFORMIXSERVER;
$ENV{"INFORMIXSQLHOSTS"} = $INFORMIXSQLHOSTS;
$ENV{"ONCONFIG"}         = $ONCONFIG;
$ENV{"LD_LIBRARY_PATH"}  = "$INFORMIXDIR/lib:$INFORMIXDIR/lib/esql";


$LOG		=	"/research/zfin/chromix/backup/backup_log";
$BACKUP		=	"/research/zfin/chromix/backup";
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

logit("Starting Informix backup of $INFORMIXSERVER\n");
system("echo | $ONTAPE -s -L 0");
logit("Finished Informix backup of $INFORMIXSERVER\n");

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


#	Write message to log file if log_level is >= value, Called with
#	logging level for this message, and the message
sub logit {
	local ($mes) = @_;
	local ($sec,$min,$hour,$mday,$mon,$year) = localtime();
	local ($_, $file, $line, $_) = caller 1;
	local ($date);

	$date = sprintf("%02d/%02d %02d:%02d:%02d",
			$mon+1, $mday, $hour, $min, $sec);

        open(LOG, ">>$LOG") || die "Can't open log file: $!";
	$mes .= ", at $file line $line\n" if $mes !~ /\n$/;
	print LOG "$date $mes";
	print     "$date $mes";
	close(LOG);
}


### All Done! ###
