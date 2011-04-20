#!/private/bin/perl -wT
{
 use CGI;
 use DBI;

 my $Q = new CGI();

 $CGI::POST_MAX=1024;    # max 1K posts
 $CGI::DISABLE_UPLOADS = 1;    # no uploads

 print $Q->header();
 print $Q->start_html(-TITLE=>"ZFIN Regen",-BGCOLOR=>'white')."\n";
 print "<script language='JavaScript' src='/javascript/header.js'></script>";
  ### the hard coded env paths need a better idea
  $ENV{INFORMIXDIR}      = '<!--|INFORMIX_DIR|-->';
  $ENV{INFORMIXSERVER}   = '<!--|INFORMIX_SERVER|-->';
  $ENV{INFORMIXSQLHOSTS} = '<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->';
  $ENV{"ONCONFIG"}       = "<!--|ONCONFIG_FILE|-->";
  ### open a handle on the db
  my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1})
  || die "Failed while connecting to <!--|DB_NAME|--> "; #$DBI::errstr";

  print "<CENTER><TABLE width=400 bgcolor=#000000 border=2><TR><TD><font color=#FFFF66>";

  my $run = $Q->param('run');
 
  my ($modf, $modtime) = $dbh->selectrow_array("
                           select zflag_is_on, zflag_last_modified 
                             from zdb_flag
                            where zflag_name=".$dbh->quote($run));
 if ($modf eq 't') {
   print "<font color='#FF0000'>$run is currently RUNNING ... </font><br>";
   print "Started at $modtime <br>";
   print "Please wait, and retry later. <br>";
   print "<font></TD></TR></TABLE></CENTER>";
   print "<script language='JavaScript' src='/javascript/footer.js'></script>";
   print  $Q->end_html."\n"; 
   $dbh->disconnect;
   exit;
 }

  ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
  print "Starting $run at $hour:$min:$sec ... <br>"; 
	
  my $statement;

  #this is for security, we wouldn't want somebody to pass in arbitrary sql

  if ($Q->param('run')  eq "regen_names") 
   {
    $statement = "execute function regen_names();";
   } 
  elsif ($Q->param('run') eq "regen_fishsearch") 
   {
    $statement = "execute function regen_fishsearch();";
   } 
  elsif ($Q->param('run') eq "regen_oevdisp")    
   {
    $statement = "execute function regen_oevdisp();";
   }elsif ($Q->param('run') eq "regen_maps")    
   {
    $statement = "execute function regen_maps();";
   }

  my $result;
  $| = 1;


 FORK: {
   if ($pid = fork) {
#parent
     my $cur = $dbh->prepare("set lock mode to wait 5;");
     $cur->execute;
     $cur = $dbh->prepare($statement);
     $cur->execute;
     $cur->bind_col(1, \$result);
     $cur->fetchrow;
     if ($result eq "0") {
       print "<br><font color='#00FF00'>woohoo! $run completed succussfully</font><br>";
     }elsif($result eq "1"){
       
       print "<br><font color='#FF0000'>$run is currently RUNNING...</font><br>";
       print "Started at $modtime <br>";
       print "Please wait, and retry later. <br>";
     }else {
       print "<br><font color='#FF0000'>doh! $run failed.</font><br>";
       my $resetf = $dbh->do ("
                      update zdb_flag set zflag_is_on = 'f'
	               where zflag_name = ".$dbh->quote($run));
       if ($resetf == 1) {
	 my $resetT = $dbh->do ("
                       update zdb_flag set zflag_last_modified = CURRENT
	                where zflag_name = ".$dbh->quote($run));
       }
     }
     kill 9, $pid;
   } elsif (defined $pid) {
#child
     $j=0;
     while($j < 30 ) { # wait for at most 30 minutes
       sleep(60);
       print ".<br>\n";
       $j++;
     }

   } elsif ($! =~ /No more process/) {
     sleep 5;
     redo FORK
   } else {
     die "Can't fork: $!\n";
   }
 }

  ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
  print "<br>$run finished at $hour:$min:$sec<br>"; 

  print "<font></TD></TR></TABLE></CENTER>";

  print "<script language='JavaScript' src='/javascript/footer.js'></script>";
  print  $Q->end_html."\n";
  $dbh->disconnect;
}

