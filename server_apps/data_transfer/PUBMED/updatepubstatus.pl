#!/private/bin/perl 


use DBI;
$mailprog = '/usr/lib/sendmail -t -oi -oem';

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
  || die("Failed while connecting to <!--|DB_NAME|--> "); 

#make the PMED dir
system("mkdir -p PMED");

#remove old files
system("rm -f pubnotactive.unl");
system("rm -f ppublish.unl");
system("rm -f ./PMED/*");

&inactive_pubs();

my $cur_update_pub = $dbh->prepare_cached('update publication set status = "active" where accession_no = ?;');

my $cur_insert_update = $dbh->prepare_cached('insert into updates (rec_id,field_name,new_value,when) select zdb_id,"status","active",current from publication where accession_no = ?;');

    open (PPUB, ">>ppublish.unl");
    open (PUBS, "pubnotactive.unl");
    while ($line = <PUBS>)
    {
      chop($line);
      if ($line =~ /^\d+$/)
      {
        system("/local/bin/wget -q 'http://www.ncbi.nlm.nih.gov/pubmed?term=$line&report=xml&format=text' -O ./PMED/$line");
      
        open(PMED, "./PMED/$line");
        while($pstat = <PMED>)
        {
          chop($pstat);
          if ($pstat =~ /PublicationStatus.*ppublish/) 
          {
            $cur_update_pub->execute($line);
            $cur_insert_update->execute($line);
            print PPUB "$line\n";
          }
        }
        close(PMED);
      }
    }
    close(PUBS);
    close(PPUB);


$dbh->disconnect();

exit;



sub inactive_pubs()
  {
    open (REPORT, ">>pubnotactive.unl") or die "can not open report";

    my $cur = $dbh->prepare('select accession_no
                             from publication
                             where status != "active"
                              and accession_no is not null;'
			   );
    $cur->execute;
    my($pub_acc_no);
    $cur->bind_columns(\$pub_acc_no);
    while ($cur->fetch)
    {
      if ($pub_acc_no =~ /^\d+$/)
      {
        print REPORT "$pub_acc_no\n";
      }
    }
    print REPORT "\n";
    close(REPORT);
  }


