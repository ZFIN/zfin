#!/private/bin/perl 

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dir = "<!--|ROOT_PATH|-->/lib/DB_schema/FRODO/";

chdir $dir ;

#system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> unload_tgcons.sql") and die "can not create tgcons";

open(FILE1, "< ./tgcons") or die "can't open tgcons";

open(PARSED, ">tg_parsed") or die "can't open tg_parsed";


$deliminator = "|" ;
$new_line = "\n" ;
$tg_base = "Tg(-" ;

my (@line, @tgjalves, @patos) ;

while (<FILE1>) {
    @line = split(/\|/,$_);
    @tghalves = split(/\:/, @line[1]) ;
    @tghalves[0] =~ s/^Tg\(\-*\d*\.*\d*//;
    @tghalves[1] =~ s/\-+\d+\.*\d*$//;
    @tghalves[1] =~ s/\)$//;

    
    print PARSED "@line[0]$deliminator@line[1]$deliminator@tghalves[0]$deliminator@tghalves[1]$deliminator$new_line" ;
}
close FILE1;

exit ;
