#!/private/bin/perl 

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dir = "<!--|ROOT_PATH|-->/lib/DB_schema/FRODO";

chdir "$dir";
print "$dir"."\n" ;

open(FILE1, "< ./EU_trans_1stSubmission.csv") or die "can't open csv file";

open(TABBED, ">EUtransTabbed") or die "can't open EUtransTabbed";


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

open(FILE2, "< ./EU_tt_second_batch.csv") or die "can't open csv file";

open(TABBED2, ">EUtrans2Tabbed") or die "can't open EUtrans2Tabbed";


$deliminator = "|" ;
$newline = "\n" ;

while ($line = <FILE2>) {
    $line =~ s/\r//;
    $line =~ s/\,/\|/g ;
    chomp $line ;
    print TABBED2 "$line$deliminator$newline";

}

close FILE2;
close TABBED2;

open(FILE3, "< ./notes_for_feature.csv") or die "can't open csv file";

open(TABBED3, ">EUtrans3Tabbed") or die "can't open EUtrans3Tabbed";


$deliminator = "|" ;
$newline = "\n" ;

while ($line = <FILE3>) {
    $line =~ s/\r//;
    $line =~ s/\,/\|/g ;
    $line =~ s/\}/\,/g ;
    chomp $line ;
    print TABBED3 "$line$deliminator$newline";

}

close FILE3;
close TABBED3;
