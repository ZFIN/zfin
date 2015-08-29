#!/private/bin/perl
#
# 13333.pl
#
# This script finds and reports those STRs with sequence(s) not manipulated correctly.
# See FB case 13333

use DBI;

### open a handle on the db
$handle = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '',
                       '',
		       {AutoCommit => 1,RaiseError => 1}
		      )
    or die "Cannot connect to Informix database: $DBI::errstr\n";

$sql = 'select dnote_data_zdb_id, dnote_text from data_note
         where dnote_data_zdb_id[1,9] in ("ZDB-MRPHL", "ZDB-CRISP", "ZDB-TALEN")
           and dnote_text like "%Reported Sequence:%";';

$curSTRwithReportedSeq = $handle->prepare($sql);

$curSTRwithReportedSeq->execute;

$curSTRwithReportedSeq->bind_columns(\$strId,\$dnote);

%strsWithReportedSeq = ();
$ct = 0;
while ($curSTRwithReportedSeq->fetch) {
   if ($dnote =~ m/Reported Sequence: (\w+) was/) {
       $strsWithReportedSeq{$strId} = $1;
       $ct = $ct + 1;
   }
}

$curSTRwithReportedSeq->finish();

$sql = 'select seq_mrkr_zdb_id, seq_sequence from marker_sequence
         where seq_mrkr_zdb_id[1,9] in ("ZDB-MRPHL", "ZDB-CRISP", "ZDB-TALEN");';

$curSeq = $handle->prepare($sql);

$curSeq->execute;

$curSeq->bind_columns(\$str,\$seq);

%strsWithSeq = ();
$ct2 = 0;
while ($curSeq->fetch) {
       $strsWithSeq{$str} = $seq;
       $ct2 = $ct2 + 1;
}

$curSeq->finish();

$sql = 'select seq_mrkr_zdb_id, seq_sequence_2 from marker_sequence
         where seq_sequence_2 is not null;';

$curSeq2 = $handle->prepare($sql);

$curSeq2->execute;

$curSeq2->bind_columns(\$str2,\$seq2);

%strsWithSeq2 = ();
$ct3 = 0;
while ($curSeq2->fetch) {
       $strsWithSeq2{$str2} = $seq2;
       $ct3 = $ct3 + 1;
}

$curSeq->finish();


$handle->disconnect();

print "\nct = $ct\n\n";
foreach $str (sort keys %strsWithReportedSeq) {
  $reportedSeq = $strsWithReportedSeq{$str};
  if (exists($strsWithSeq{$str})) {  
     print "$str\t$reportedSeq\n" if $strsWithSeq{$str} eq $reportedSeq;
  }
  if (exists($strsWithSeq2{$str})) {
     print "$str\t$reportedSeq\n" if $strsWithSeq2{$str} eq $reportedSeq;
  }

}

exit;

