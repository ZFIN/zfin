#!/private/bin/perl -wT
{
 use CGI;
 use DBI;

 my $Q = new CGI();

 $CGI::POST_MAX=1024;    # max 1K posts
 $CGI::DISABLE_UPLOADS = 1;    # no uploads

 print $Q->header();
 print $Q->start_html(-TITLE=>"Regen Genomics",-BGCOLOR=>'white')."\n";
 print "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/header.js'></script>";
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

  ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
  print "Starting $run at $hour:$min:$sec ... <br>"; 
	
  my $statement;

  #this is for security, we wouldn't want somebody to pass in arbitrary sql

  if ($Q->param('run')  eq "regen_genomics") 
   {
    $statement = "execute function regen_genomics();";
   } 
  elsif ($Q->param('run') eq "regen_fishsearch") 
   {
    $statement = "execute function regen_fishsearch();";
   } 
  elsif ($Q->param('run') eq "regen_oevdisp")    
   {
    $statement = "execute function regen_oevdisp();";
   }

  my $result;
  $| = 1;


 FORK: {
   if ($pid = fork) {
#parent
     my $cur = $dbh->prepare($statement);
     $cur->execute;
     $cur->bind_col(1, \$result);
     $cur->fetchrow;
     if ($result eq "1") {
       print "<br><font color='#00FF00'>woohoo! $run completed succussfully</font><br>";
     } else {
       print "<br><font color='#FF0000'>doh! $run failed.</font><br>";
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
  print "<br>$run finished at $hour:$min:$sec<br>"; 

  print "<font></TD></TR></TABLE></CENTER>";

  print "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/footer.js'></script>";
  print  $Q->end_html."\n";
  $dbh->disconnect;
}

