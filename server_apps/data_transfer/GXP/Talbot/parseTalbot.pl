#!/private/bin/perl -wt
# 
# Read in: gene.txt
#          expression.txt
#          keywords.txt
#          images.txt
#          stage_codes.txt
#          view_codes.txt
#     
# Convert input files into standard GXP load files format. 
# Interact with zfin database to query the exist gene with the accession 
# and check the consistency if there is a given assignment. Output
# unknown accession to acc4blast.txt for blasting. 
# Also interpret stage code and view code in expression, keywords 
# and images files. Create authors.unl file based the number of probes. 
# 
# Output:  probes.unl
#          expression.unl
#          authors.unl
#          keywords.unl
#          images.unl
#          parseTalbot.err
#          acc4blast.txt
#

use DBI;

#============================================
#  Main
#
# inherit Informix environment variable from the shell

die "Db name is required.\n" if (@ARGV < 1);

my $dbname = "$ARGV[0]";
my $username = "";
my $password = "";

my $AUTHOR_1 = "Rauch, Gerd-Jörg";
my $AUTHOR_2 = "Talbot, Will";

my $stage_in   = "cegs_stage_codes.txt";
my %stage_hash = ();

my $view_in    = "cegs_view_codes.txt";
my %view_hash  = ();

my $probe_in   = "gene.txt";
my $probe_out  = "probes.unl";
my $err        = "parseTalbot.err";
my $probe_acc  = "acc4blast.txt";
my $author_out = "authors.unl";
my $expression_in  = "expression.txt";
my $expression_out = "expression.unl";
my $keyword_in  = "keywords.txt";
my $keyword_out = "keywords.unl";
my $image_in    = "images.txt";
my $image_out   = "images.unl";

open ERR, ">$err" or die "Cannot open $err to read";

#######################################
# convert stage_code into hash storage
#
#######################################

open STAGE_IN, "<$stage_in" or die "Cannot open $stage_in to read";
my $previous_stage_code = 0;
my $previous_stage_name = "";
my $stage_code          = 0;
my $stage_name          = "";
while (<STAGE_IN>) {
    s/\s+$//g;                         #important! to eliminate odd trailing space chars
    if (/^[0-9]/) {
	($stage_code, $stage_name) = split (/\t/);
	if ($stage_code != $previous_stage_code) {
	    if ($previous_stage_code > 0) {		
		$stage_hash{$previous_stage_code} .= "|".$previous_stage_name;
	    }
	    
	    $stage_hash{$stage_code} = $stage_name;
	}

	$previous_stage_code = $stage_code;
	$previous_stage_name  = $stage_name;
	next;
    }  
    &checkUnexpectedLine($stage_in, $_);   
}
$stage_hash{$previous_stage_code} .= "|".$previous_stage_name;

close (STAGE_IN);


##########################################
# convert view_code into hash storage
#
##########################################
open VIEW_IN, "<$view_in" or die "Cannot open $view_in to read";
while (<VIEW_IN>) {
    s/\s+$//g;   
    if(/^[0-9]/) {
	my ($view_code, $view_name, $view_orient) = split (/\t/);
	$view_hash{$view_code} = $view_name."|".$view_orient;
	next;
    }   
    &checkUnexpectedLine($view_in, $_);   
}
close (VIEW_IN);


#########################################
#  probes.unl
#
#       input                       output
#      ---------                  ---------
#      0  ceg_id                   _keyValue 
#      1  acc_number               clone_name 
#      2  name                     gene_zdb_id 
#      3  other_names              gb5p 
#      4  long_name                gb3p 
#      5  vector                   library 
#      6  library         ==>      digest 
#      7  description              vector 
#      8  human_protein            pcr amplification 
#      9  blastn_acc               insert_kb 
#      0  vs_align_length          cloning_site
#      1  vs_match_pcnt            polymerase 
#      2  digest                   comments 
#      3  polymerase               modified_date

###########################################
 
######################################
# authors.unl
#
# _keyValue     author_name     blank
#######################################
    
my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";

open PROBE_IN, "<$probe_in" or die "Cannot open $probe_in to read";
open PROBE_OUT, ">$probe_out" or die "Cannot open $probe_out to write";
open PROBE_ACC, ">$probe_acc" or die "Cannot open $probe_acc to read";
open AUTHOR_OUT, ">$author_out" or die "Cannot open $author_out to write";
   
while (<PROBE_IN>) {
    s/\s+$//g;
    if (/^[0-9]/) {
	my @row = split (/\t/);
	my $cegs_id     = $row[0];
	my $acc_col     = $row[1];
	my $name_col    = $row[2];
	my $othname_col = $row[3];
	my $clone_name_whole = $othname_col eq '' ? $name_col : $othname_col;
	my ($clone_name, $ver) = split(/\./, $clone_name_whole);
	my $gene_sym = $othname_col ne '' ? $name_col : '' ;
	$gene_sym =~ s/zgc\s/zgc:/; $gene_sym =~ s/wu\s/wu:/;
        my $gene_sym_lower = lc $gene_sym;
	my ($zfin_gene_id, $zfin_sym);

	my $vector      = $row[5]; $vector =~ s/^\s+//; $vector =~ s/\s+$//;
	my $library     = $row[6]; $library =~ s/^\s+//; $library =~ s/\s+$//;
	my $digest      = $row[12];$digest =~ s/^\s+//; $digest =~ s/\s+$//;
	my $polymerase  = $row[13];$polymerase =~ s/^\s+//; $polymerase =~ s/\s+$//;
	if (! $acc_col) {
	    print ERR "$cegs_id has no accession number.\n";
	    next;
	}
	if (! $clone_name) {
	    print ERR "$acc_col has no clone name.\n";
	    next;
	}

        # find out the gb acc related to which gene
	my $sth = $dbh->prepare ("select mrkr_zdb_id, mrkr_abbrev
                                    from db_link, marker
                                   where dblink_acc_num = '$acc_col'
                                     and dblink_linked_recid = mrkr_zdb_id
                                     and mrkr_type like 'GENE%'
                                     and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37' 
                                 UNION
                                  select mrkr_zdb_id, mrkr_abbrev
                                    from db_link, marker, marker_relationship
                                   where dblink_acc_num = '$acc_col'
                                     and dblink_linked_recid = mrel_mrkr_2_zdb_id
                                     and mrel_mrkr_1_zdb_id = mrkr_zdb_id
                                     and mrkr_type like 'GENE%'
                                     and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37'   
                                ");
	$sth->execute();
	my $array_ref = $sth->fetchall_arrayref();
	if (@$array_ref > 1) {            ## gb acc matches >1 zfin genes ##
	    print ERR join("    ", $acc_col, $clone_name, $gene_sym, ">1 ZFIN genes")."\n";

	}elsif (@$array_ref == 1) {       ## gb acc matches one zfin gene ##

	    my $array_ref_sym = "";
	    my ($row) = @$array_ref;
	    ($zfin_gene_id, $zfin_sym) = @$row; 

	    if ($zfin_sym ne $gene_sym && $gene_sym) {
		my $sth_sym =  $dbh->prepare ("
                                          select dalias_zdb_id
                                            from data_alias
                                           where dalias_alias_lower = '$gene_sym_lower'
                                             and dalias_data_zdb_id = '$zfin_gene_id'");
		$sth_sym->execute();
		$array_ref_sym = $sth_sym->fetchall_arrayref();
	    }

 	    # three cases that zfin gene id will get assigned
            # - talbot gene symbol matches zfin gene symbol
            # - talbot gene symbol matches zfin gene previous name
            # - no talbot gene symbol but acc# matched one zfin gene
	    if ($zfin_sym eq $gene_sym || @$array_ref_sym == 1 || ! $gene_sym) {

		print PROBE_OUT join("|",$row[0],$clone_name,$zfin_gene_id,"",$acc_col,$library,$digest,$vector,"","","",$polymerase,"","")."||\n";
		print AUTHOR_OUT "$row[0]|$AUTHOR_1||\n";
		print AUTHOR_OUT "$row[0]|$AUTHOR_2||\n";
	   
	    }else {
		print ERR join("    ", $acc_col, $clone_name, $gene_sym, $zfin_gene_id, $zfin_sym)."\n";
	    }

	}else{
            # when gb acc didn't match any zfin gene, but talbot provided a gene symbol,
            # we don't actually use that symbol, instead we do a blast analysis and use the 
            # blast results. But we do courtesy check on that name to catch cases when 
            # clone name was put on the gene name column. 	

	    if ($gene_sym) {       
		$zfin_gene_id = $dbh->selectrow_array("
                                             select mrkr_zdb_id 
                                               from marker
                                              where mrkr_abbrev = '$gene_sym_lower'
                                                and mrkr_type like 'GENE%'
                                            UNION
                                             select dalias_data_zdb_id
                                               from data_alias
                                              where dalias_alias_lower =  '$gene_sym_lower'
                                                and dalias_data_zdb_id like 'ZDB-GENE%' ");
	    }

	    if ($gene_sym && !$zfin_gene_id){
		print ERR join("    ", $acc_col, $clone_name, $gene_sym, "Gene sym not in ZFIN")."\n";
		
	    }else {
		
		print PROBE_OUT join("|",$row[0],$clone_name,"","",$acc_col,$library,$digest,$vector,"","","",$polymerase,"","")."||\n";
		print AUTHOR_OUT "$row[0]|$AUTHOR_1||\n";
		print AUTHOR_OUT "$row[0]|$AUTHOR_2||\n";
		print PROBE_ACC "$acc_col\n";
	    }
	}
	
	next;
    }
    &checkUnexpectedLine($probe_in,$_);   
}
close (PROBE_IN);
close (PROBE_OUT);
close (PROBE_ACC);
close (AUTHOR_OUT);  
     

########################################
# expression.txt
#
#      CEGS_ID   STAGE    COMMENTS
#          1       1               
#
# expression.unl
# 
#       _keyValue
#       exp_sstart
#       exp_sstop
#       exp_description
#       exp_found
#       exp_keywords     # to be consistent with Thisse format
#       exp_modified
#########################################

open EXPR_IN, "<$expression_in" or die "Cannot open $expression_in to read";
open EXPR_OUT, ">$expression_out" or die "Cannot open $expression_out to write";
while (<EXPR_IN>) {
    s/\s+$//g;
    if (/^[0-9]/) {
	s/\s+$//g;
	my ($exp_key, $exp_stage, $exp_desc) = split (/\t/);
	if ($exp_stage < 1 || $exp_stage > keys(%stage_hash)) {
	    print "ERROR: unexpected stage code in $expression_in: $_ \n";
	    exit;
	}
	$exp_desc =~ s/^[ \"]*//; $exp_desc =~ s/[ \"]*$//; 

	# exp_found is populated with 't' for now, the loading script will correct it
	# with corresponding keywords. 
	print EXPR_OUT join("\|",$exp_key,$stage_hash{$exp_stage},$exp_desc,"t","","")."|\n";
	next;
    }
    &checkUnexpectedLine($expression_in, $_);   
}
close (EXPR_IN);
close (EXPR_OUT);


###########################################
# keywords.txt
#
#      CEGS_ID   STAGE   EXPR_PART
#          1       1     no expression detected
#          1       5     somites
#
# keywords.unl
#
#       _keyValue
#       keywrd_sstart
#       keywrd_sstop
#       keywrd_keyword
#       keywrd_modified
###########################################

open KEYWD_IN, "<$keyword_in" or die "Cannot open $keyword_in to read";
open KEYWD_OUT, ">$keyword_out" or die "Cannot open $keyword_out to write";
while(<KEYWD_IN>) {
    s/\s+$//g;
    if (/^[0-9]/) {
	my ($keywrd_key, $keywrd_stage, $keywrd_keyword) = split (/\t/);
        if ($keywrd_stage < 1 || $keywrd_stage > keys(%stage_hash)) {
	    print "ERROR: unexpected stage code in $keyword_in: $_ \n";
	    exit;
	}
	print KEYWD_OUT join("|",$keywrd_key,$stage_hash{$keywrd_stage},$keywrd_keyword,"")."|\n";
	next;
    }
    &checkUnexpectedLine($keyword_in, $_);
}
close (KEYWD_IN);
close (KEYWD_OUT);


###########################################
# images.txt
#
#   CEGS_ID    IMAGE_NAME           STAGE  VIEW
#      1   cegsid-1--8 somites.jpg    5     3
#
# images.unl
#
#    _keyValue
#    img_name
#    img_sstart
#    img_sstop
#    img_view
#    img_orientation
#    img_preparation
#    img_comments
#    img_modified
###########################################

open IMG_IN, "<$image_in" or die "Cannot open $image_in to read";
open IMG_OUT, ">$image_out" or die "Cannot open $image_out to write";
while (<IMG_IN>) {
    s/\s+$//g;
    if (/^[0-9]/) {
	my ($img_key, $img_name, $img_stage, $img_view) = split (/\t/);
	if ($img_stage < 1 || $img_stage > keys(%stage_hash)) {
	    print "ERROR: unexpected stage code in $image_in: $_ \n";
	    exit;
	}
	if ($img_view < 0 || $img_view > keys(%view_hash)) {
	    print "ERROR: unexpected view code in $image_in: $_ \n";
	    exit;
	}
	$img_name =~ s/  */__/g;  # replace one or more space with __
	my ($img_name_r, $img_name_e) = split(/\./, $img_name);
	print IMG_OUT join("|",$img_key,$img_name_r,$stage_hash{$img_stage},$view_hash{$img_view},"","","")."|\n";
	next;
    }
    &checkUnexpectedLine($image_in, $_);  
}
close (IMG_IN);
close (IMG_OUT);


close (ERR);
exit;

#========================================================
# subfunction checkUnexpectedLine 
#
# input: filename
#        line string 
#
sub checkUnexpectedLine ($$) {
    my $file_name = shift;
    my $the_line  = shift;
    if ($the_line =~ /^[a-z]/i) {
	return;
    }elsif ($the_line =~ /[a-z0-9]/i) {
	print "WARNING: unexpected line in $file_name: $the_line \n";
	return
    }
}






