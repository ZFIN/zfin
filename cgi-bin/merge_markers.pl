#!/private/bin/perl 
# code that walks the system tables in the database,
# extracts keys and constraints and tries to delete or merge
# based on these releationships.  

use CGI;
use CGI::Carp 'fatalsToBrowser';
use DBI;
use HTTP::Request::Common qw/GET/;
use LWP::UserAgent;
use URI::Escape qw/uri_escape/;


# set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";


# permission variables

$person_id = "";
$access = "";
$owner_id = "";
$cookie = "";

sub confirmLogin () {

    if ($ENV{'HTTP_COOKIE'}){ # get cookie sent from browser, Zfin only sends 
	                      # one cookie.

	$cookie = $ENV{'HTTP_COOKIE'};

	# split the cookie into 2 parts, its cookie name (at ZFIN, that's
	# zfin_login, and its value).

	my $semicolon = ";";
	my $space = " ";
	$cookie = $cookie.$semicolon.$space;
	
	@pairs = split(/; /, $cookie);

        foreach $pair (@pairs) {
          ($name, $value) = split(/=/, $pair);
	  
          $find_cookie{$name} = $value;
        }

        $ZDB_cookie = $find_cookie{"zfin_login"};

	#($name, $value) = split (/=/, $cookie);
	#$ZDB_cookie = $value ;
	
        # connect to the database and see if the passed cookie also
        # has the right set of permissions.

        my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
			       '', 
			       '', 
			       {AutoCommit => 1,RaiseError => 1}
			       )
	    || &emailError("Failed while connecting to <!--|DB_NAME|--> ");

	my $cur = $dbh->prepare("select zdb_id, access
                                   from zdb_submitters
                                    where cookie = '$ZDB_cookie';"
				);
	$cur->execute;
	my ($db_person_OID, $db_access);
	$cur->bind_columns(\$db_person_OID, \$db_access);
	while ($cur->fetch()) {
	    $person_id = $db_person_OID;
	    $access = $db_access;
        }
        $dbh->disconnect();

	# make sure person_id is not null, if it is null, then cookie
	# was not found in zdb_submitters

	if ($person_id eq "") {
	 
	  ##  &access_error('no person_id listed in zdb_submitters');

	} # end if person_id is null

    } # end if cookie

    else {
	
	## &access_error ('no cookies');        
    }
}

my $data = new CGI();

&confirmLogin;


$dbname = "<!--|DB_NAME|-->";
$mergeId = $data->param("OID");
$intoId = $data->param("merge_oid");

unlink "/tmp/mergeMarkersLog";
open (LOG, ">>/tmp/mergeMarkersLog") or  die("Unable to open log: $!\n");

### open (DBG, ">/tmp/mergeDebug.txt") or  die("Unable to open mergeDebug.txt: $!\n");

###open (LOG2, ">>/tmp/mergeMarkersLog2") or  die("Unable to open log: $!\n");

   

sub cleanTail ($) {
    my $var = $_[0];

    while ($var !~ /\w$/) {
	chop ($var);
    }
    
    return $var;
}

print "Content-type: text/HTML\n\n<HTML>\n\n";


### open a handle on the db
my $dbh = DBI->connect("DBI:Informix:$dbname",
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
    )
    || die("Failed while connecting to $dbname "); 


# go through marker first, looking for dependent tables via FK relationships.  After going through marker,
# go through zdb_active_data
for ($base=0; $base<2; $base++){
    if ($base == 0){
	$master = "marker";
    }
    else{
	$master = "zdb_active_data";
    }
    
    @tablecolumn = ();
    my %tabid = ();	
    
    # select the table ids and table names where the column from the child table
    # is part of an index that supports the foreign key constraint.  
    # ie: marker is master, then the row returned for marker_go_term_evidence looks like this:
    # c.constrname = mrkrgoev_mrkr_zdb_id_foreign_key
    # c.idxname = marker_go_Term_evidence_marker_foreign_key
    # t.tabname = marker_go_Term_Evidence
    # l.colno = 2
    # The column also has to be in the constraint 
    $sql_command = "
		select distinct t.tabid, t.tabname
		from sysconstraints c, sysreferences r, systables t, systables t2, sysindexes i, syscolumns l
		where ptabid = t2.tabid 
		  and c.constrid = r.constrid 
		  and c.tabid = t.tabid 
		  and c.constrtype = 'R'
		  and t2.tabname = '$master' 
		  and c.idxname = i.idxname
		  and l.tabid = t.tabid
		  and l.colno in (i.part1,i.part2,i.part3,i.part4,part5,part6,part7);";
    
    my $cur = $dbh->prepare($sql_command);
    my($tabname);
    my($tabid);
    
    $cur->bind_columns(\$tabid,\$tabname) ;
    
    $cur->execute;
    
    ### polpulate two lists (table list, table with constraint info list)
    
    while ($cur->fetch) {
	$tabid =~ cleanTail($tabid);
	$tabname =~ cleanTail($tabname);
	
	$tabid{$tabid} = $tabname;
    }

    @prime_list = ();;

    ### make a list of primary keys and do not use those columns
    while ( ($tabid,$tabname) = each(%tabid) ){
	$sql_primes = "select o.colname 
                             from systables t, sysconstraints c, sysindexes x, syscolumns o 
                             where t.tabname = \"$tabname\" 
                              and t.tabid = c.tabid 
                              and constrtype = \"P\" 
                              and c.idxname = x.idxname 
                              and o.tabid = t.tabid 
                              and o.colno = 1";
	
	$cur=$dbh->prepare($sql_primes);
	$cur->bind_columns(\$primecol);
	$cur->execute();
	while ($cur->fetch) {
	    $primecol =~ cleanTail($primecol);
	    push(@prime_list, $primecol);
	}    
    }

    if ($master eq "marker"){
	$primecollist = join ', ', map { "'$_'" } @prime_list;
    }
    else{
	$primecollist = "'mrkr_zdb_id'";
	}
    
    # find the tables in the list above that don't satisfy this query
    # with their primary key columns.
    $sql_command = "
		select distinct t.tabid, t.tabname, colname, i.idxname
		from sysconstraints c, sysreferences r, systables t, systables t2, sysindexes i, syscolumns l
		where ptabid = t2.tabid 
		  and c.constrid = r.constrid 
		  and c.tabid = t.tabid 
		  and c.constrtype = 'R'
		  and t2.tabname = '$master' 
		  and c.idxname = i.idxname
		  and l.tabid = t.tabid
		  and l.colno in (i.part1,i.part2,i.part3,i.part4,part5,part6,part7)
		  and colname not in ($primecollist)
		order by 2;";

    my $cur = $dbh->prepare($sql_command);
    my($tabname);
    my($tabid);
    my($colname);
    my($idxname);
    
    $cur->bind_columns(\$tabid,\$tabname,\$colname,\$idxname) ;
    
    $cur->execute;
    
    ### polpulate two lists (table list, table with constraint info list)
    
    while ($cur->fetch) {
	$tabid =~ cleanTail($tabid);
	$tabname =~ cleanTail($tabname);
	$colname =~ cleanTail($colname);
	$idxname =~ cleanTail($idxname);
	
	push(@tablecolumn, "$tabname $colname $idxname");
    }


    ### for each table, find other constraints that use the column our script is going to update.
    
    while ( ($tabid,$tabname) = each(%tabid) ){
	@tabidx = ();
	@tabconst = ();
	$tabrowcount;
	@updateRowCol = ();
	@updateRow = ();
	@rowSelect = ();
	@TableDef = ();
	
	foreach $tci (@tablecolumn){
	    ($t,$c, $i) = split(" ",$tci);
	    if ($t eq $tabname){
		push (@tabidx,$i);
	    }
	}
	#    local $" = '","';    
	$idxlist = join ', ', map { "'$_'" } @tabidx;
	#    ###print "$tabname $idxlist \n\n\n";
	
	
	### make a list of constraints to check
	foreach $tci (@tablecolumn){
	    ($t,$c, $i) = split(" ",$tci);
	    if ($t eq $tabname){
		$sql_command = "
			select distinct i.idxname
			from sysindexes i, syscolumns c, systables t
			where idxname not in ($idxlist)
			  and c.colno in (i.part1,i.part2,i.part3,i.part4)
			  and colname == \"$c\"
			  and idxtype == 'U'
			  and i.tabid == t.tabid
			  and t.tabname == \"$tabname\";";
		
		$cur=$dbh->prepare($sql_command);
		$cur->bind_columns(\$idxname);
		$cur->execute();
		while ($cur->fetch) {
		    $idxname =~ cleanTail($idxname);
		    push(@tabconst, $idxname);
		    ###print "$tabname $idxname \n";
		}
		
	    }# if tabname
	}# foreach @tablecolumn    
	
	
	
	### make a list of rows to update (alternate keys, primary key)
	foreach $tci (@tablecolumn){
		@updateRow = ();
		($t,$c, $i) = split(" ",$tci);
		
		if ($t eq $tabname){
		    
		    ### make a list of constraints for the table that contain the column and are unique
		   
		    @U_idx = ();
		    $sql = "select distinct i.idxname
		    from syscolumns l, sysindexes i, sysconstraints c
		      where l.tabid = \"$tabid\"  
		       and i.tabid = \"$tabid\"
		       and c.constrtype = 'U'
		       and c.idxname = i.idxname 
		       and l.colname = \"$c\" ;";          

		    $cur=$dbh->prepare($sql);
		    $cur->bind_columns(\$U_idxname);
		    $cur->execute();
		    while ($cur->fetch) {
			$U_idxname =~ cleanTail($U_idxname);
		       push(@U_idx, $U_idxname);
		    }	    
		    $U_idxlist = join ', ', map { "'$_'" } @U_idx;

		    print LOG "UL $U_idxlist \n";
		    
		    ### get the number of col in the table for the index
		    if ( "$U_idxlist " ne " "){
			$sql = "select l.colname, l.colno, l.coltype
		            from syscolumns l, sysindexes i, sysconstraints c
		            where l.tabid = \"$tabid\"  
		             and i.tabid = \"$tabid\"
		             and c.constrtype = 'U'
		             and c.idxname = i.idxname 
		             and i.idxname in ($U_idxlist)
		             and l.colno in (i.part1,i.part2,i.part3,i.part4,part5,part6,part7);";

			###print "INDEX COLS\n$sql \n";

			$cur=$dbh->prepare($sql);
			$cur->bind_columns(\$tabcolname, \$tabcolno, \$tabcoltype);
			$cur->execute();
			$z = 0;
			while ( $cur->fetch ){
			    ###print $tabcoltype. "\n";
			    
			    if( $tabcoltype == 0 || $tabcoltype == 12 || $tabcoltype == 13 || $tabcoltype == 43 || $tabcoltype == 45 || $tabcoltype == 256 || $tabcoltype == 269){
				$updateRowCol[$z] = $tabcolname; 
				$TableDef[$tabcolno] = $tabcolname;
				$TableColType[$z] = "'";
				$rowSelect[$z] = $tabcolname;
				$z++;
			    }#if

			    if( $tabcoltype == 1 || $tabcoltype == 2 || $tabcoltype == 3 || $tabcoltype == 4 || $tabcoltype == 5 || $tabcoltype == 6 || $tabcoltype == 17 || $tabcoltype == 18 || $tabcoltype == 52 || $tabcoltype == 53 ){
				$updateRowCol[$z] = $tabcolname; 
				$TableDef[$tabcolno] = $tabcolname;
				$TableColType[$z] = "";
				$rowSelect[$z] = $tabcolname;
				$z++;
			    }#if       
			}#while
			
			$tabselectcols = join (",",@rowSelect);
			
			###print $tabselectcols . "\n";
			
			
			$sql = "
			      select $tabselectcols
			        from $tabname
			        where $c = \"$mergeId\";";
			
			#print LOG "$t -- $c -- $i"."\n";
			#print LOG $sql . "\n";
			print LOG $#updateRowCol . " ROWS"."\n";
			
			$cur=$dbh->prepare($sql);
			$cur->bind_columns( \( @rowSelect ) );
		        $cur->execute();
		    while ($cur->fetch) {
			#print join ("\t", @rowSelect ) . "\n";
			push (@updateRow, join ("\t", @rowSelect ));
		    }
		} #if U_idxlist not empty  
		
		
		### check each row against each constraint and remove violations
		foreach $URV (@updateRow){       
		    @UR = split("\t",$URV);
		    ## make the where clause
		    $sql_where = "where ";
		    $sql_set_where = "where ";
		    for($k=0; $k<$#updateRowCol+1; $k++){
			if ($UR[$k] eq $mergeId){
			    $setUpdateColumn = $updateRowCol[$k];
		      }
			
			if ($k==0){
			    if ($UR[$k] eq $mergeId){
				$sql_set_where = $sql_set_where.$updateRowCol[$k]." = ".$TableColType[$k].$UR[$k].$TableColType[$k]." ";
				$sql_where = $sql_where.$updateRowCol[$k]." = ".$TableColType[$k].$intoId.$TableColType[$k]." ";                 
			    }
			    else{
				$sql_where = $sql_where.$updateRowCol[$k]." = ".$TableColType[$k].$UR[$k].$TableColType[$k]." ";                 
				$sql_set_where = $sql_set_where.$updateRowCol[$k]." = ".$TableColType[$k].$UR[$k].$TableColType[$k]." ";                 
			    }
			}
			else{
			    if ( "$UR[$k] " eq " " ) {
				$sql_where = $sql_where." and ".$updateRowCol[$k]." is null ";
				$sql_set_where = $sql_set_where." and ".$updateRowCol[$k]." is null ";
			    }
			    elsif ($UR[$k] eq $mergeId){
				$sql_where = $sql_where." and ".$updateRowCol[$k]." = ".$TableColType[$k].$intoId.$TableColType[$k]." ";
				$sql_set_where = $sql_set_where." and ".$updateRowCol[$k]." = ".$TableColType[$k].$UR[$k].$TableColType[$k]." "; 
			    }
			    else {
				$sql_where = $sql_where." and ".$updateRowCol[$k]." = ".$TableColType[$k].$UR[$k].$TableColType[$k]." ";
				$sql_set_where = $sql_set_where." and ".$updateRowCol[$k]." = ".$TableColType[$k].$UR[$k].$TableColType[$k]." ";
			    }
			}
		    }#colval
		    
		    $sql = "
			select count(*)
			from $tabname 
			$sql_where ;";
		    
		    $sql_delete = "delete from $tabname $sql_set_where ; ";
		    print LOG $sql_delete;
		    #           $sql_set_update = "
		    #                update $tabname 
		    #                set $setUpdateColumn = '$intoId'
		    #                $sql_set_where ;";
		    
		    #print LOG $sql_delete . "\n";
		    
		    #           $rowcount=0;
		    $cur=$dbh->prepare($sql);
		    $cur->bind_columns( \$rowcount );
		    $cur->execute();
		    $cur->fetch();
		    #print LOG "count: $rowcount \n"; 	   
		    if ( int($rowcount) < 1){
			# This is a record that will be merged.
		    }
		    else{
			push(@sql_update_list, $sql_delete);
		    }
		}#UR
		
		push(@sql_update_list, "update $t set $c = \"$intoId\" where $c = \"$mergeId\";");
		
#	       $sql_update_list = $sql_update_list . " \n update $t set $c = \"$intoId\" where $c = \"$mergeId\"; \n";   
	}#t
    }#tci
	
} #while %tabid

}#marker vs zdb_active_data


### update the rows

foreach $sql_update (@sql_update_list){
    $sql_update =~ s/\'/\"/g;
    $sql = qq(insert into merge_markers_sql(mms_mrkr_1_zdb_id,mms_mrkr_2_zdb_id,mms_sql) values ('$mergeId','$intoId','$sql_update'););
###  print LOG2 "$sql\n";
    $cur=$dbh->prepare($sql);
    $cur->execute();
#    $cur=$dbh->prepare($sql_update);
#    $cur->execute();
}

close LOG;

### FB case 11133

$goawayFieldValue = "ZFIN:".$mergeId;
$intoFieldValue = "ZFIN:".$intoId;

$sqlGetPrimaryKeysInferenceGroupMember = "select distinct infgrmem_mrkrgoev_zdb_id, infgrmem_inferred_from from inference_group_member where infgrmem_inferred_from = ? ;";
$curGetPrimaryKeysInferenceGroupMembe = $dbh->prepare_cached($sqlGetPrimaryKeysInferenceGroupMember);
$curGetPrimaryKeysInferenceGroupMembe->execute($intoFieldValue);
$curGetPrimaryKeysInferenceGroupMembe->bind_columns(\$infgrmemMrkrgoevZdbId,\$infgrmemInferredFrom);

%primaryKeysInferenceGroupMember = ();
while ($curGetPrimaryKeysInferenceGroupMembe->fetch()) {
  $primaryKey = $infgrmemMrkrgoevZdbId . $infgrmemInferredFrom;
  $primaryKeysInferenceGroupMember{$primaryKey} = $infgrmemInferredFrom;
}

$curGetPrimaryKeysInferenceGroupMembe->finish();

$sqlGetMrkrgoevZdbIds = "select distinct infgrmem_mrkrgoev_zdb_id from inference_group_member where infgrmem_inferred_from = ? ;";
$curGetMrkrgoevZdbIds = $dbh->prepare_cached($sqlGetMrkrgoevZdbIds);
$curGetMrkrgoevZdbIds->execute($goawayFieldValue);
$curGetMrkrgoevZdbIds->bind_columns(\$infgrmemMrkrgoevZdbIdWithGene1);

$sqlUpdateInferenceGroupMembe = "update inference_group_member set infgrmem_inferred_from = ? where infgrmem_mrkrgoev_zdb_id = ? and infgrmem_inferred_from = ?;";
$curUpdateInferenceGroupMembe = $dbh->prepare_cached($sqlUpdateInferenceGroupMembe);

while ($curGetMrkrgoevZdbIds->fetch()) {
  $primaryKey = $infgrmemMrkrgoevZdbIdWithGene1 . $intoFieldValue;
  $curUpdateInferenceGroupMembe->execute($intoFieldValue, $infgrmemMrkrgoevZdbIdWithGene1, $goawayFieldValue) if !exists($primaryKeysInferenceGroupMember{$primaryKey});
  $primaryKeysInferenceGroupMember{$primaryKey} = $intoFieldValue;
}

$curGetMrkrgoevZdbIds->finish();
$curUpdateInferenceGroupMembe->finish();


## close LOG2;

### FB case 14015

$sqlDeletePossibleRedundant = "delete from clean_expression_fast_search where cefs_mrkr_zdb_id = ? ;";
$curDeletePossibleRedundant = $dbh->prepare_cached($sqlDeletePossibleRedundant);
$curDeletePossibleRedundant->execute($intoId);
$curDeletePossibleRedundant->finish();

### FB case 10333

$sqlGetUnspecifiedAllele = "select fmrel_ftr_zdb_id from feature_marker_relationship,feature where fmrel_ftr_zdb_id = feature_zdb_id and fmrel_mrkr_zdb_id = ? and feature_unspecified = 't' ;";
$curGetUnspecifiedAllele = $dbh->prepare_cached($sqlGetUnspecifiedAllele);
$curGetUnspecifiedAllele->execute($mergeId);
$curGetUnspecifiedAllele->bind_columns(\$unspecifiedAlleleWithGeneToBeDeleted);

$thereIsUnspecifiedAlleleWithGeneToBeDeleted = 0;
while ($curGetUnspecifiedAllele->fetch()) {
  $thereIsUnspecifiedAlleleWithGeneToBeDeleted++;
}

$curGetUnspecifiedAllele->execute($intoId);
$curGetUnspecifiedAllele->bind_columns(\$unspecifiedAlleleWithGeneRetained);

$thereIsUnspecifiedAlleleWithGeneRetained = 0;
while ($curGetUnspecifiedAllele->fetch()) {
  $thereIsUnspecifiedAlleleWithGeneRetained++;
}

$curGetUnspecifiedAllele->finish();

$sqlGetGeneAbbrev = "select mrkr_abbrev from marker where mrkr_zdb_id = ? ;";
$curGetGeneSymbol = $dbh->prepare_cached($sqlGetGeneAbbrev);
$curGetGeneSymbol->execute($mergeId);
$curGetGeneSymbol->bind_columns(\$geneSymbolToBeDeleted);
while ($curGetGeneSymbol->fetch()) {
   $unspecifiedAllele1Gene1 = $geneSymbolToBeDeleted . '_unspecified';
}
 
$curGetGeneSymbol->execute($intoId);
$curGetGeneSymbol->bind_columns(\$geneSymbolRetained);

while ($curGetGeneSymbol->fetch()) {
   $unspecifiedAllele1Gene2 = $geneSymbolRetained . '_unspecified';
}
   
$curGetGeneSymbol->finish();

if ($thereIsUnspecifiedAlleleWithGeneRetained == 0 && $thereIsUnspecifiedAlleleWithGeneToBeDeleted > 0) {

### print DBG "intoId: $intoId  geneSymbolToBeDeleted: $geneSymbolToBeDeleted  geneSymbolRetained: $geneSymbolRetained   \n   unspecifiedAllele1Gene2: $unspecifiedAllele1Gene2    unspecifiedAlleleWithGeneToBeDeleted: $unspecifiedAlleleWithGeneToBeDeleted \n\n";

    $sqlRenameAlleleName = "update feature set (feature_name, feature_abbrev) = (?,?) where feature_zdb_id = ? ;";
    $sqlRenameAlleleName = $dbh->prepare_cached($sqlRenameAlleleName);
   
    $sqlRenameAlleleName->execute($unspecifiedAllele1Gene2, $unspecifiedAllele1Gene2, $unspecifiedAlleleWithGeneToBeDeleted);
    $sqlRenameAlleleName->finish();

   $sqlGetGenotypesWithUnspecifiedAllele = "select geno_zdb_id, geno_display_name, geno_handle from genotype, genotype_feature where genofeat_feature_zdb_id = ? and genofeat_geno_zdb_id = geno_zdb_id;";
   
   $curGetGenotypes = $dbh->prepare_cached($sqlGetGenotypesWithUnspecifiedAllele);

   $sqlRenameRelatedGenotypes = "update genotype set (geno_display_name, geno_handle) = (?,?) where geno_zdb_id = ?;";
   
   $curRenameGenotypes = $dbh->prepare_cached($sqlRenameRelatedGenotypes);
      
   $curGetGenotypes->execute($unspecifiedAlleleWithGeneToBeDeleted);
   $curGetGenotypes->bind_columns(\$genoId,\$genoDisplayName,\$genoHandle);
   
   while ($curGetGenotypes->fetch()) {
###   print DBG "genoId: $genoId \t genoDisplayName: $genoDisplayName \t genoHandle: $genoHandle \n";
      $genoDisplayName =~ s/$geneSymbolToBeDeleted/$geneSymbolRetained/;
      $genoHandle =~ s/$geneSymbolToBeDeleted/$geneSymbolRetained/;
      $curRenameGenotypes->execute($genoDisplayName,$genoHandle,$genoId);
   }
   
   $curGetGenotypes->finish();
   $curRenameGenotypes->finish();

}


### FB case 13983, update genotype display names and fish names when merging genes

$sqlGetGenotypeDisplayName = "select geno_zdb_id, geno_display_name from feature_marker_relationship, genotype_feature, genotype where fmrel_mrkr_zdb_id  = ? and fmrel_ftr_zdb_id= genofeat_feature_zdb_id and genofeat_geno_zdb_id = geno_zdb_id;";

$curGetGenotypeDisplayName = $dbh->prepare_cached($sqlGetGenotypeDisplayName);

$sqlUpdtateGenotypeDislayName = "update genotype set geno_display_name = ? where geno_zdb_id = ?;";

$curUpdtateGenotypeDislayName = $dbh->prepare_cached($sqlUpdtateGenotypeDislayName);

$curGetGenotypeDisplayName->execute($mergeId);

$curGetGenotypeDisplayName->bind_columns(\$genotypeId,\$genotypeDisplayName);

while ($curGetGenotypeDisplayName->fetch()) {
   $genotypeDisplayName =~ s/$geneSymbolToBeDeleted/$geneSymbolRetained/;
   $curUpdtateGenotypeDislayName->execute($genotypeDisplayName,$genotypeId);
}

$curGetGenotypeDisplayName->finish();
$curUpdtateGenotypeDislayName->finish();

$sqlGetFishName = "select fish_zdb_id, fish_name from feature_marker_relationship, genotype_feature, fish where fmrel_mrkr_zdb_id  = ? and fmrel_ftr_zdb_id= genofeat_feature_zdb_id and genofeat_geno_zdb_id = fish_genotype_zdb_id;";

$curGetFishName = $dbh->prepare_cached($sqlGetFishName);

$sqlUpdtateFishName = "update fish set fish_name = ? where fish_zdb_id = ?;";

$curUpdateFishName = $dbh->prepare_cached($sqlUpdtateFishName);

$curGetFishName->execute($mergeId);

$curGetFishName->bind_columns(\$fishId,\$fishName);

while ($curGetFishName->fetch()) {
   $fishName =~ s/$geneSymbolToBeDeleted/$geneSymbolRetained/;
   $curUpdateFishName->execute($fishName,$fishId);
}

$curGetFishName->finish();
$curUpdateFishName->finish();

### FB case 14194

$sqlUpdateSfclg = "update sequence_feature_chromosome_location_generated set sfclg_data_zdb_id = ? where sfclg_data_zdb_id = ? and not exists (select 'x' from sequence_feature_chromosome_location_generated as s where s.sfclg_data_zdb_id = ? and s.sfclg_chromosome = sfclg_chromosome and s.sfclg_location_source = sfclg_location_source and s.sfclg_location_subsource = sfclg_location_subsource and s.sfclg_start = sfclg_start and s.sfclg_end = sfclg_end and s.sfclg_acc_num = sfclg_acc_num )";
$curUpdateSfclg = $dbh->prepare_cached($sqlUpdateSfclg);
$curUpdateSfclg->execute($intoId,$mergeId, $intoId);

$curUpdateSfclg->finish();

##close DBG;

### close database connection
$dbh->disconnect();

print <<EOA;

<HEAD>
    <script language="JavaScript">    
    window.location.href='/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-process_delete.apg&OID=$mergeId&merge_oid=$intoId&rtype=marker';
</script>  
</HEAD>
<BODY>
    Database scanned. Now merging.
</BODY>
</HTML>
EOA
    
exit;

