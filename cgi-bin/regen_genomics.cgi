#!/private/bin/perl -wT
{
 use CGI;
 use DBI;

 my $Q = new CGI();

 print $Q->header();
 print $Q->start_html(-TITLE=>"Regen Genomics",-BGCOLOR=>'white')."\n";
 print "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/header.js'></script>";
  ### the hard coded env paths need a better idea
  $ENV{INFORMIXDIR}      = '<!--|INFORMIX_DIR|-->';
  $ENV{INFORMIXSERVER}   = '<!--|INFORMIX_SERVER|-->';
  $ENV{INFORMIXSQLHOSTS} = '<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->';
  ### open a handle on the db
  my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1})
  || die "Failed while connecting to <!--|DB_NAME|--> "; #$DBI::errstr";

  print "<CENTER><TABLE width=400 bgcolor=#000000 border=2><TR><TD><font color=#FFFF66>";

 
  my ($modf, $modtime) = $dbh->selectrow_array("
                                  select zflag_is_on, zflag_last_modified 
                                  from zdb_flag
                                  where zflag_name = 'regen_genomics'");

 if ($modf == 't') {
   print "<font color='#FF0000'>regen genomics is currently RUNNING ... </font><br>";
   print "Started at $modtime <br>";
   print "Please wait, and retry later. <br>";
   print "<font></TD></TR></TABLE></CENTER>";
   print "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/footer.js'></script>";
   print  $Q->end_html."\n"; 
   $dbh->disconnect;
   exit;
 }

  ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
  print "Starting regen_genomics at $hour:$min:$sec ... <br>"; 
  my $result;

 $| = 1;

 FORK: {
   if ($pid = fork) {
#parent
     my $cur = $dbh->prepare("execute function regen_genomics();");
     $cur->execute;
     $cur->bind_col(1, \$result);
     $cur->fetch;
     if ($result eq "0") {
       print "<br><font color='#00FF00'>woohoo! regen genomics completed succussfully</font><br>";
     } elsif($result eq "1"){
       
       print "<br><font color='#FF0000'>regen genomics is currently RUNNING...</font><br>";
       print "Started at $modtime <br>";
       print "Please wait, and retry later. <br>";
     }else {
       print "<br><font color='#FF0000'>doh! regen genomics failed.</font><br>";
       my $resetf = $dbh->do ("
                      update zdb_flag set zflag_is_on = 'f'
	               where zflag_name = 'regen_genomics' ");
       if ($resetf == 1) {
	 my $resetT = $dbh->do ("
                       update zdb_flag set zflag_last_modified = CURRENT
	                where zflag_name = 'regen_genomics' ");
       }
     }
     kill 9, $pid;
   } elsif (defined $pid) {
#child
     $j=0;
     while($j < 60 ) {
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
  print "<br>regen_genomics finished at $hour:$min:$sec<br>"; 

  print "<font></TD></TR></TABLE></CENTER>";

  print "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/footer.js'></script>";
  print  $Q->end_html."\n";
  $dbh->disconnect;
}
