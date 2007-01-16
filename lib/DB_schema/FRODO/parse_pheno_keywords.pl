#!/private/bin/perl 

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dir = "<!--|ROOT_PATH|-->/lib/DB_schema/FRODO/";

chdir $dir ;

system("echo 'unload to phen_keywords select zdb_id, pheno_keywords from fish where pheno_keywords is not null' | $ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|-->") and die "can not create parsed_keywords";

open(FILE1, "< ./phen_keywords") or die "can't open pheno_keywords";

open(PARSED, ">pheno_keywords_parsed") or die "can't open pheno_keywords_parsed";
open(CONVERTED_PATO_EU, ">pato_fish") or die "can't open pato_fish" ;

$deliminator = "|" ;
$new_line = "\n" ;

my (@line, @phenos, @patos) ;

while (<FILE1>) {
    @line = split(/\|/,$_);
    
    print @line[3];
    @phenos = split(/\;/, @line[1]) ;
    foreach $pheno_word (@phenos) {
	$pheno_word =~ s/^ *//;
	if ($pheno_word =~ /\:+/) {
	    @patos = split(/\:/, $pheno_word);
	    $stage = @patos[0];
	    $entity = @patos[1];
	    $attribute = @patos[2];
	    $value = @patos[3];
	    print CONVERTED_PATO_EU "@line[0]$deliminator$stage$deliminator$entity$deliminator$attribute$deliminator$value$deliminator$new_line" ;
	}
	#$pheno_word =~ s/\://;
        print PARSED "@line[0]$deliminator$pheno_word$deliminator$new_line";
    }
}

close FILE1;

close PARSED;
    
exit ;
