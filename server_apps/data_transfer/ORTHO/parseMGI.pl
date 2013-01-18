#!/private/bin/perl

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";


system("rm -f updateMouseOrthologyLog1");
system("rm -f updateMouseOrthologyLog2");
system("rm -f MGI.data");
system("rm -f mus_chr_loc_sym.tab");
system("rm -f mus_chr_loc_sym_W.tab");

system("wget -q --timestamping ftp://ftp.informatics.jax.org/pub/reports/MRK_List1.rpt -O MGI.data");

$ctMGIlines = 0;

open (MGI, "MGI.data") ||  die "Cannot open MGI.data : $!\n";

open (PARSEDMGI,  ">mus_chr_loc_sym.tab") || die "Can't open: mus_chr_loc_sym.tab $!\n";

open (PARSEDMGIW,  ">mus_chr_loc_sym_W.tab") || die "Can't open: mus_chr_loc_sym_W.tab $!\n";


while (<MGI>) {
 chomp;
 
 $ctMGIlines++;
 next if $ctMGIlines < 2; 
 
 @fieldsMGI = split("\t");

 $MGIid = $fieldsMGI[0];
 $Chr = $fieldsMGI[1];
 $loc = $fieldsMGI[2];
 $symbol = $fieldsMGI[6]; 
 $status = $fieldsMGI[7];
 $markerType = $fieldsMGI[9];
 
### print "\n$MGIid\t$Chr\t$loc\t$symbol\t$status\t$markerType\n\n" if $ctMGIlines < 10;

 next if $markerType ne "Gene";
 
 if ($status eq "W") {
     print PARSEDMGIW "$MGIid\t$Chr\t$loc\t$symbol\n";
 } else {
     print PARSEDMGI "$MGIid\t$Chr\t$loc\t$symbol\n";
 }
 
 undef @fieldsMGI;
 
}
 
close (MGI);
close (PARSEDMGI);
close (PARSEDMGIW);


system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> update_mus_ortho_loc.sql >updateMouseOrthologyLog1 2> updateMouseOrthologyLog2");


print "\nTested....\n";

exit;


