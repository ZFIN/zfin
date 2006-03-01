#!/private/bin/perl 

$ENV{"INFORMIXDIR"}="/private/apps/Informix/informix";
$ENV{"INFORMIXSERVER"}="wanda";
$ENV{"ONCONFIG"}="onconfig";
$ENV{"INFORMIXSQLHOSTS"}="/private/apps/Informix/informix/etc/sqlhosts";


system("echo 'unload to phen_keywords select zdb_id, pheno_keywords from fish where pheno_keywords is not null' | dbaccess almdb") and die "can not create parsed_keywords";

open(FILE1, "< ./phen_keywords") or die "can't open pheno_keywords";

open(PARSED, ">pheno_keywords_parsed") or die "can't open pheno_keywords_parsed";
$deliminator = "|" ;
$new_line = "\n" ;
my (@line, @phenos) ;

while (<FILE1>) {
    @line = split(/\|/,$_);

    print @line[3];
    @phenos = split(/\;/, @line[1]) ;
    foreach $pheno_word (@phenos) {
	$pheno_word =~ s/^ *//;
	$pheno_word =~ s/\://;
        print PARSED "@line[0]$deliminator$pheno_word$deliminator$new_line";
    }
}

close FILE1;

close PARSED;
    
