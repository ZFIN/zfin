#!/private/bin/perl 

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dir = "<!--|ROOT_PATH|-->/lib/DB_schema/FRODO";

chdir "$dir";
print "$dir"."\n" ;

open(FILE1, "< ./PhenoKeywordTrans_v5.csv") or die "can't open csv file";

open(TABBED, ">phenoTabbed") or die "can't open phenoTabbed";


$deliminator = "|" ;
$newline = "\n" ;

while ($line = <FILE1>) {
    $line =~ s/\r//;
    $line =~ s/\,/\|/g ;
    $line =~ s/\}/,/g;
    chomp $line ;
    print TABBED "$line$deliminator$newline";

}

close FILE1;
close TABBED;
