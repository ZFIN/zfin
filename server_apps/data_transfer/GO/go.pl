#!/local/bin/perl
#  This script creates a file that ZFIN sends to Stanford. The file is tab 
#  delimitted with 14 columns, each GO term/gene association on a seperate  
#  line. 
#  We must send the file via email to GO after running the script. A reminder
#  email, containing the path to the file, is sent to a member of ZFIN. 

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
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 

$dir = "<!--|ROOT_PATH|-->/home/data_transfer/GO/";
chdir "$dir";
$outFile = "gene_association.zfin";


&GOReport();
&sendReport();

$dbh->disconnect();

exit;


sub emailError()
  {
    &writeReport($_[0]);
    &sendReport();
    exit;
  }


sub writeReport()
  {
    open (REPORT, ">>$outFile") or die "cannot open report";
    print REPORT "$_[0] \n\n";
    close (REPORT);
  }



sub GOReport()
  {
    system("/bin/rm -f $outFile");
    open (REPORT, ">>$outFile") or die "can not open report";

    my $cur = $dbh->prepare('select mrkr_zdb_id, 
                                    mrkr_abbrev,
                                    goterm_go_id,
                                    mrkrgoev_source_zdb_id,
                                    goev_code,
                                    get_date_from_id(mrkrgo_zdb_id,"YYYYMMDD"),
                                    goterm_ontology[1]
                             from   marker,
                                    marker_go_term,
                                    go_term,
                                    marker_go_term_evidence,
                                    go_evidence_code
                             where  mrkr_zdb_id = mrkrgo_mrkr_zdb_id
                               and  mrkrgo_go_term_zdb_id = goterm_zdb_id
                               and  mrkrgo_zdb_id = mrkrgoev_mrkrgo_zdb_id
                               and  mrkrgoev_evidence_code = goev_code;'
			   );
    $cur->execute;
    my($mrkr_id, $mrkr_abbrev, $go_id, $source_id, $ev_code, $ev_date, $go_o);
    $cur->bind_columns(\$mrkr_id,\$mrkr_abbrev,\$go_id,\$source_id,\$ev_code,\$ev_date,\$go_o);
    while ($cur->fetch)
    {
      $go_o = goAspect($go_o);
      print REPORT "ZFIN\t$mrkr_id\t$mrkr_abbrev\t\tGO:$go_id\t$source_id\t$ev_code\t\t$go_o\t\t\tgene\ttaxon:7955\t$ev_date\tZFIN\n";
    }
    print REPORT "\n";
    close(REPORT);
    chmod(0644,"$outFile");
  }


sub sendReport()
  {
    open(MAIL, "| $mailprog") || die "cannot open mailprog $mailprog, stopped";

    print MAIL "To: <!--|SWISSPROT_EMAIL_ERR|-->\n";
    print MAIL "Subject: GO report\n";

    print MAIL "A new GO file has been created. Before submitting the new file, verify ZFIN's file format is up-to-date by comparing it against the posted version. The posted version can be found here: \nhttp://www.geneontology.org/doc/GO.annotation.html#file \n\nIf the formats are the same -> submit the updated GO file:\n$dir$outFile. \nElse -> modify /server_apps/data_transfer/GO/go.pl. \n\nZFIN's format: \n1. DB \n2. DB_Object_ID \n3. DB_Object_Symbol \n4. [NOT] \n5. GOid \n6. DB:Reference(|DB:Reference) \n7. Evidence \n8. With \n9. Aspect \n10. DB_Object_Name(|Name) \n11. DB_Object_Synonym(|Synonym) \n12. DB_Object_Type \n13. taxon(|taxon) \n14. Date \n15. Assigned ";
    close (MAIL);
  }


sub goAspect()
  {
    $aspect = $_[0];

    $aspect = 'P' if ($aspect eq 'B');
    $aspect = 'F' if ($aspect eq 'M');

    return $aspect;
  }
