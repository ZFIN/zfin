#!/private/bin/perl 

$ENV{"INFORMIXDIR"}="/private/apps/Informix/informix";
$ENV{"INFORMIXSERVER"}="wanda";
$ENV{"ONCONFIG"}="onconfig";
$ENV{"INFORMIXSQLHOSTS"}="/private/apps/Informix/informix/etc/sqlhosts";

$dir = "/research/zusers/staylor/hoover/RenoData";

chdir "$dir";
print "$dir"."\n" ;

open(FILE1, "< ./HM_txid_geneid_acc") or die "can't open HM file";

open(TABBED, ">HMpiped") or die "can't open HMpiped";


$deliminator = "|" ;
$newline = "\n" ;

while ($line = <FILE1>) {
    $line =~ s/\n//;
     $line = $line.$deliminator.$newline;
    print TABBED "$line";

}

close FILE1;
close TABBED;
