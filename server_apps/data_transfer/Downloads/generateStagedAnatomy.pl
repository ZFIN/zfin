#!/private/bin/perl -w
#
# This script queries anatomy_display table mainly to generate
# staged anatomy terms with relationship structure. The output 
# is writen to file at public download site. Two terms, unspecified 
# and cell division/proliferation are excluded on request. Before
# there is a better solution like database definition, hardcode. 
#
# INPUT:
#     none
# OUTPUT:
#     <!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_structure.txt
#

use strict;
use DBI;

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

my ($stgZdbId, $lastStgZdbId, $stgName, $anatName, $dispIndent);
my $mailprog = '/usr/lib/sendmail -t -oi -oem';
my $output = "<!--|ROOT_PATH|-->/home/data_transfer/Downloads/staged_anatomy.other";

open (OUT,">$output") or &emailError ("Can not open $output to write");

my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', '', 
                       {AutoCommit => 1,RaiseError => 1}
                      )
    || &emailError("Failed while connecting to <!--|DB_NAME|--> "); 

$lastStgZdbId = "";

my $sql = "select stg_zdb_id, stg_name, anatitem_name, anatdisp_indent
             from anatomy_display
                  join stage on anatdisp_stg_zdb_id = stg_zdb_id
                  join anatomy_item on anatdisp_item_zdb_id = anatitem_zdb_id
            where anatitem_zdb_id not in ('ZDB-ANAT-041102-1', 'ZDB-ANAT-010921-520')
         order by stg_hours_start, anatdisp_seq_num;";

my $sth = $dbh->prepare($sql);
$sth->execute;
$sth->bind_columns(\$stgZdbId, \$stgName, \$anatName, \$dispIndent);

while ($sth->fetch) {

    if (!$lastStgZdbId || ($stgZdbId ne $lastStgZdbId)) {
	print OUT "\n" if $lastStgZdbId;  # additional break between stages
	print OUT "$stgName\n";
    }
    $lastStgZdbId = $stgZdbId;

    for (my $i = $dispIndent; $i > 0; $i--) {
	print OUT "\t";
    }
    print OUT "$anatName\n";
}

exit;

#=========================================
# emailError
#
# INPUT:
#    string ::  error message
# OUTPUT:
#    none
# EFFECT:
#    error message is sent to db owner.
#

sub emailError($)
  {
    open(MAIL, "| $mailprog") || die "Cannot open mailprog $mailprog";
    print MAIL "To: <!--|DB_OWNER|-->\@cs.uoregon.edu\n";
    print MAIL "Subject: generateStageAnatomy.pl error\n";
    print MAIL "$_[0]";
    close MAIL;
    exit;
  }

