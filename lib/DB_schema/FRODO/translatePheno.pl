#!/local/bin/perl 

$ENV{"INFORMIXDIR"}="/private/apps/Informix/informix";
$ENV{"INFORMIXSERVER"}="wanda";
$ENV{"ONCONFIG"}="onconfig";
$ENV{"INFORMIXSQLHOSTS"}="/private/apps/Informix/informix/etc/sqlhosts";

open(FILE1, "< ./KeywordTrans_v2.csv") or die "can't open csv file";

open(TABBED, ">phenoTabbed") or die "can't open phenoTabbed";


$deliminator = "|" ;
$newline = "\n" ;

while ($line = <FILE1>) {
    $line =~ s/\r//;
    $line =~ s/\,/\|/g ;
    chomp $line ;
    print TABBED "$line$deliminator$newline";

}

close FILE1;
close TABBED;
