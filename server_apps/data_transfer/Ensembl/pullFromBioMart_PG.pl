#!/private/bin/perl -w
use DBI;
use MIME::Lite;


sub downloadFiles() {
   

    my $wgetStatusFile = "/tmp/ensembleBioMartEnsdargToEnsdart.<!--|DB_NAME|-->.ids";
    system("rm -f $wgetStatusFile");
    if (system("/local/bin/wget http://www.ensembl.org/biomart/martview/ca9511f2d4d91522c380e5fa7b22c64d?VIRTUALSCHEMANAME=default&ATTRIBUTES=drerio_gene_ensembl.default.feature_page.ensembl_gene_id|drerio_gene_ensembl.default.feature_page.ensembl_transcript_id&FILTERS=&VISIBLEPANEL=mainpanel >> $wgetStatusFile 2>&1")) {
	&errorExit("Failed to download ids from Ensemble.","  See $wgetStatusFile for details.");
    }
    if (-z $wgetStatusFile) {
	&errorExit("Downloaded file $wgetStatusFile is empty.  Aborting.",
		   "  See $wgetStatusFile for details.");
    }

    return ();
}



my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

$dbh->commit();
$dbh->disconnect();
