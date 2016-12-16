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
$ENV{"CLIENT_LOCALE"}="en_US.utf8";
$ENV{"DB_LOCALE"}="en_US.utf8";

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

##open (DBG, ">/tmp/mergeDebug.txt") or  die("Unable to open mergeDebug.txt: $!\n");

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

$sqlUpdateSfclg = "update sequence_feature_chromosome_location_generated
                      set sfclg_data_zdb_id = ?
                    where sfclg_data_zdb_id = ?
                      and not exists (select 'x'
                                        from sequence_feature_chromosome_location_generated as s
                                       where s.sfclg_data_zdb_id = ?
                                         and s.sfclg_chromosome = sfclg_chromosome
                                         and nvl(s.sfclg_location_source,'') = nvl(sfclg_location_source,'')
                                         and nvl(s.sfclg_location_subsource,'') = nvl(sfclg_location_subsource,'')
                                         and nvl(s.sfclg_start,'') = nvl(sfclg_start,'')
                                         and nvl(s.sfclg_end,'') = nvl(sfclg_end,'')
                                         and nvl(s.sfclg_acc_num,'') = nvl(sfclg_acc_num,''))";

$curUpdateSfclg = $dbh->prepare_cached($sqlUpdateSfclg);
$curUpdateSfclg->execute($intoId,$mergeId,$intoId);

$curUpdateSfclg->finish();

## for STRs to be merged, if used in fish, delete the fish_str record so as to avoid duplicated one afgter merge
if ($mergeId =~ m/MRPHLNO/ || $mergeId =~ m/CRISP/ || $mergeId =~ m/TALEN/) {
  $getFishStr = "select fstr1.fishstr_fish_zdb_id 
                   from fish_str fstr1
                  where fstr1.fishstr_str_zdb_id = ?
                    and exists(select 'x' from fish_str fstr2
                                where fstr2.fishstr_str_zdb_id = ?
                                  and fstr2.fishstr_fish_zdb_id = fstr1.fishstr_fish_zdb_id);";
 
  $curGetFishStr = $dbh->prepare($getFishStr);
  $curGetFishStr->execute($intoId,$mergeId);
  $curGetFishStr->bind_columns(\$fishStrID);
  @fishStrIDs = ();
  $ctFishStrIDs = 0;
  while ($curGetFishStr->fetch()) {
    $fishStrIDs[$ctFishStrIDs] = $fishStrID;
    $ctFishStrIDs++;
  }
  $curGetFishStr->finish();
  foreach $fishStrTodelete (@fishStrIDs) {
     $sqlDeleteFishStr = "delete from fish_str where fishstr_str_zdb_id = $intoIdintoId and fishstr_str_zdb_id = $fishStrTodelete;";
     $curDeleteFishStr = $dbh->prepare($sqlDeleteFishStr);
     $curDeleteFishStr->execute();
     $curDeleteFishStr->finish();
  }
}

# get the merge action SQLs that contain record_attribution
$recAttrSQL = "select mms_sql, mms_pk_id 
               from merge_markers_sql 
              where mms_mrkr_1_zdb_id = ?
                and mms_mrkr_2_zdb_id = ?
                and mms_sql like '%record_attribution%';";
$curGetRecAttrSQLs = $dbh->prepare($recAttrSQL);
$curGetRecAttrSQLs->execute($mergeId, $intoId);
$curGetRecAttrSQLs->bind_columns(\$recAttrSQL,\$recAttrSQLid);
%recAttrSQLs = ();
while ($curGetRecAttrSQLs->fetch()) {
   if ($recAttrSQL !~ m/xpatres_zdb_id/) {
      $recAttrSQLs{$recAttrSQLid} = $recAttrSQL;
   }
}
$curGetRecAttrSQLs->finish();

$getPersonName = "select full_name from person where zdb_id = ?;";
$curGetPerson = $dbh->prepare($getPersonName);
$curGetPerson->execute($person_id);
$curGetPerson->bind_columns(\$personName);
while ($curGetPerson->fetch()) {
  $personFullName = $personName;
}
$curGetPerson->finish();

## run the merge action SQLs that are related to  the record attribution with first
foreach $sqlID (keys %recAttrSQLs) {
   $recAttrMergeSQL = $recAttrSQLs{$sqlID};

   if ($recAttrMergeSQL =~ m/delete/i) {
     $curRecAttrSQL = $dbh->prepare($recAttrMergeSQL);
     $curRecAttrSQL->execute();
     $curRecAttrSQL->finish();

     $insertUpdatesSQL = "insert into updates(submitter_id,rec_id,new_value,comments,when,submitter_name)
			  values(?,?,'DELETE',?,CURRENT,?);";
     $curInsertUpdates = $dbh->prepare($insertUpdatesSQL);
     $curInsertUpdates->execute($person_id,$intoId,$recAttrMergeSQL,$personFullName); 
     $curInsertUpdates->finish();  

   }
}

## do the record attribution with no delete secondly
foreach $sqlID (keys %recAttrSQLs) {
   $recAttrMergeSQL = $recAttrSQLs{$sqlID};
   if ($recAttrMergeSQL !~ m/delete/i) {
     $curRecAttrSQL = $dbh->prepare($recAttrMergeSQL);
     $curRecAttrSQL->execute();
     $curRecAttrSQL->finish();
   }
}

## deal with root GO term from either of the party whenever there is non-root GO term for the other party, see FB case 11048
$deleteMrkrGoEvd = "delete from zdb_active_data where zactvd_zdb_id = ?;"; 
$curDeleteMrkrGoEvd = $dbh->prepare_cached($deleteMrkrGoEvd);

$getNonRootBioProcess = "select * from marker_go_term_evidence, term 
                          where mrkrgoev_mrkr_zdb_id = ? 
                            and mrkrgoev_term_zdb_id != 'ZDB-TERM-091209-6070'
                            and mrkrgoev_term_zdb_id = term_zdb_id 
                            and term_ontology = 'biological_process';";
$curNonRootBioProcess = $dbh->prepare_cached($getNonRootBioProcess);
$getRootBioProcess = "select mrkrgoev_zdb_id from marker_go_term_evidence where mrkrgoev_mrkr_zdb_id = ? and mrkrgoev_term_zdb_id = 'ZDB-TERM-091209-6070';";
$curRootBioProcess = $dbh->prepare_cached($getRootBioProcess);
$curRootBioProcess->bind_columns(\$mrkrGoEvdId);

$curNonRootBioProcess->execute($mergeId);
while ($curNonRootBioProcess->fetch()) {
   $curRootBioProcess->execute($intoId);
   while ($curRootBioProcess->fetch()) {
      $curDeleteMrkrGoEvd->execute($mrkrGoEvdId);
   }
}

$curNonRootBioProcess->execute($intoId);
while ($curNonRootBioProcess->fetch()) {        
   $curRootBioProcess->execute($mergeId); 
   while ($curRootBioProcess->fetch()) {    
      $curDeleteMrkrGoEvd->execute($mrkrGoEvdId);
   }
}

$curRootBioProcess->finish();
$curNonRootBioProcess->finish();

$getNonRootMolFunc = "select * from marker_go_term_evidence, term
                       where mrkrgoev_mrkr_zdb_id = ?
                         and mrkrgoev_term_zdb_id != 'ZDB-TERM-091209-2432'
                         and mrkrgoev_term_zdb_id = term_zdb_id
                         and term_ontology = 'molecular_function';";
$curNonRootMolFunc = $dbh->prepare_cached($getNonRootMolFunc);
$getRootMolFunc = "select mrkrgoev_zdb_id from marker_go_term_evidence where mrkrgoev_mrkr_zdb_id = ? and mrkrgoev_term_zdb_id = 'ZDB-TERM-091209-2432';";
$curRootMolFunc = $dbh->prepare_cached($getRootMolFunc);
$curRootMolFunc->bind_columns(\$mrkrGoEvdId);

$curNonRootMolFunc->execute($mergeId);
while ($curNonRootMolFunc->fetch()) {
   $curRootMolFunc->execute($intoId);
   while ($curRootMolFunc->fetch()) {
      $curDeleteMrkrGoEvd->execute($mrkrGoEvdId);
   }
}

$curNonRootMolFunc->execute($intoId);
while ($curNonRootMolFunc->fetch()) {
   $curRootMolFunc->execute($mergeId);
   while ($curRootMolFunc->fetch()) {
      $curDeleteMrkrGoEvd->execute($mrkrGoEvdId);
   }
}

$curRootMolFunc->finish();
$curNonRootMolFunc->finish();

$getNonRootCelComp = "select * from marker_go_term_evidence, term
                       where mrkrgoev_mrkr_zdb_id = ?
                         and mrkrgoev_term_zdb_id != 'ZDB-TERM-091209-4029'
                         and mrkrgoev_term_zdb_id = term_zdb_id
                         and term_ontology = 'cellular_component';";
$curNonRootCelComp = $dbh->prepare_cached($getNonRootCelComp);
$getRootCelComp = "select mrkrgoev_zdb_id from marker_go_term_evidence where mrkrgoev_mrkr_zdb_id = ? and mrkrgoev_term_zdb_id = 'ZDB-TERM-091209-4029';";
$curRootCelComp = $dbh->prepare_cached($getRootCelComp);
$curRootCelComp->bind_columns(\$mrkrGoEvdId);

$curNonRootCelComp->execute($mergeId);
while ($curNonRootCelComp->fetch()) {
   $curRootCelComp->execute($intoId);
   while ($curRootCelComp->fetch()) {
      $curDeleteMrkrGoEvd->execute($mrkrGoEvdId);
   }
}

$curNonRootCelComp->execute($intoId);
while ($curNonRootCelComp->fetch()) {
   $curRootCelComp->execute($mergeId);
   while ($curRootCelComp->fetch()) {
      $curDeleteMrkrGoEvd->execute($mrkrGoEvdId);
   }
}

$curRootCelComp->finish();
$curNonRootCelComp->finish();

$curDeleteMrkrGoEvd->finish();
 
# all_map_names
# get the allmapnm_name for the marker to be deleted
$getAllMapNamesSQL = "select allmapnm_name, allmapnm_serial_id
                        from all_map_names
                       where allmapnm_zdb_id = ? ;";
$curGetAllMapNamesDeletedMrkr = $dbh->prepare($getAllMapNamesSQL);
$curGetAllMapNamesDeletedMrkr->execute($mergeId);
$curGetAllMapNamesDeletedMrkr->bind_columns(\$mapNameDeletedMrkr, \$mapIdDeletedMrkr);
%allMapNamesDeletedMrkr = ();
while ($curGetAllMapNamesDeletedMrkr->fetch()) {
   $allMapNamesDeletedMrkr{$mapIdDeletedMrkr} = $mapNameDeletedMrkr;
}
$curGetAllMapNamesDeletedMrkr->finish();

# get the allmapnm_name for the marker to be merged into              
$curGetAllMapNamesIntoMrkr = $dbh->prepare($getAllMapNamesSQL);
$curGetAllMapNamesIntoMrkr->execute($intoId);         
$curGetAllMapNamesIntoMrkr->bind_columns(\$mapNameIntoMrkr, \$mapIdIntoMrkr);
%allMapNamesIntoMrkr = (); 
while ($curGetAllMapNamesIntoMrkr->fetch()) {
   $allMapNamesIntoMrkr{$mapIdIntoMrkr} = $mapNameIntoMrkr;
}
$curGetAllMapNamesIntoMrkr->finish();

# delete the allmapnm_name that are the same between that of the marker to be deleted and the marker to merge into
$deleteMapSQL = "delete from all_map_names where allmapnm_zdb_id = ? and allmapnm_name = ? ;";
foreach $mrkrId (keys %allMapNamesDeletedMrkr) {
  $mapNameDeletedMrkr = $allMapNamesDeletedMrkr{$mrkrId};
  foreach $intoMrkrId (keys %allMapNamesIntoMrkr) {
    $mapNameIntoMrkr = $allMapNamesIntoMrkr{$intoMrkrId};
    if ($mapNameIntoMrkr eq $mapNameDeletedMrkr) {
       $curDeleteMap = $dbh->prepare($deleteMapSQL);
       $curDeleteMap->execute($mergeId,$mapNameIntoMrkr);
       $curDeleteMap->finish();
    }
  }
}

# db_link
# get dblink_acc_num and dblink_fdbcont_zdb_id for the marker to be deleted
$getAlternateKeyDblinkSQL = "select dblink_acc_num, dblink_fdbcont_zdb_id, dblink_zdb_id 
                               from db_link
                              where dblink_linked_recid = ? ;";
$curGetAlternateKeyDblinkDeletedMrkr = $dbh->prepare($getAlternateKeyDblinkSQL);
$curGetAlternateKeyDblinkDeletedMrkr->execute($mergeId);
$curGetAlternateKeyDblinkDeletedMrkr->bind_columns(\$accNumDeletedMrkr, \$fdbcntIdDeletedMrkr, \$dblinkIdDeletedMrkr);
%alternateKeysDblinkDeletedMrkr = ();
while ($curGetAlternateKeyDblinkDeletedMrkr->fetch()) {
   $alternateKeysDblinkDeletedMrkr{$dblinkIdDeletedMrkr} = $accNumDeletedMrkr.$fdbcntIdDeletedMrkr;
}
$curGetAlternateKeyDblinkDeletedMrkr->finish();

# get dblink_acc_num and dblink_fdbcont_zdb_id for the marker to be merged into
$curGetAlternateKeyDblinkIntoMrkr = $dbh->prepare($getAlternateKeyDblinkSQL);
$curGetAlternateKeyDblinkIntoMrkr->execute($intoId);
$curGetAlternateKeyDblinkIntoMrkr->bind_columns(\$accNumIntoMrkr, \$fdbcntIdIntoMrkr, \$dblinkIdIntoMrkr);
%alternateKeysDblinkIntoMrkr = ();
while ($curGetAlternateKeyDblinkIntoMrkr->fetch()) {
   $alternateKeysDblinkIntoMrkr{$dblinkIdIntoMrkr} = $accNumIntoMrkr.$fdbcntIdIntoMrkr;
}
$curGetAlternateKeyDblinkIntoMrkr->finish();

# delete dblink_acc_num and dblink_fdbcont_zdb_id that are the same between those of the marker to be deleted and the marker to merge into
$deleteDblinkSQL = "delete from db_link where dblink_zdb_id = ? and dblink_linked_recid = ? ;";
foreach $dblinkId (keys %alternateKeysDblinkDeletedMrkr) {
  $alternateKeyDblinkDeletedMrkr = $alternateKeysDblinkDeletedMrkr{$dblinkId};
  foreach $dblinkIdIntoMrkr (keys %alternateKeysDblinkIntoMrkr) {
    $alternateKeyDblinkIntoMrkr = $alternateKeysDblinkIntoMrkr{$dblinkIdIntoMrkr};
    if ($alternateKeyDblinkIntoMrkr eq $alternateKeyDblinkDeletedMrkr) {
       $curDeleteDblink = $dbh->prepare($deleteDblinkSQL);
       $curDeleteDblink->execute($dblinkId,$mergeId);
       $curDeleteDblink->finish();
    }
  }
}

$getMarkerAbbrev = "select mrkr_abbrev, mrkr_name, get_id('DALIAS') as daliasid from marker where mrkr_zdb_id = ?;";
$curGetMarkerAbbrev = $dbh->prepare($getMarkerAbbrev);
$curGetMarkerAbbrev->execute($intoId);
$curGetMarkerAbbrev->bind_columns(\$mrkrAbbrev, \$mrkrName, \$daliasID);
while ($curGetMarkerAbbrev->fetch()) {
  $mrkrAbbrevInto = $mrkrAbbrev;
  $mrkrNameInto = $mrkrName;
  $newDaliasID = $daliasID;
}
$curGetMarkerAbbrev->finish();

# data alias  (FB case 14531)
$updateMrkrHistory = "update marker_history set mhist_dalias_zdb_id = null where mhist_dalias_zdb_id = ? ;"; 
$curUpdateMrkrHistory = $dbh->prepare_cached($updateMrkrHistory);

$getDAliases = "select dalias_zdb_id, dalias_alias from data_alias where dalias_data_zdb_id = ? ;";
$curGetDAliases = $dbh->prepare_cached($getDAliases);
$curGetDAliases->execute($mergeId);
$curGetDAliases->bind_columns(\$daliasId,\$dalias);
%daliasesMerge = ();
while ($curGetDAliases->fetch()) {
   $daliasesMerge{$daliasId} = $dalias;
}
$curGetDAliases->execute($intoId);
$curGetDAliases->bind_columns(\$daliasId2,\$dalias2);
%daliasesInto = ();
while ($curGetDAliases->fetch()) {
   $daliasesInto{$daliasId2} = $dalias2;
}
$curGetDAliases->finish();

$delete = "delete from zdb_active_data where zactvd_zdb_id = ?;"; 
$curDelete = $dbh->prepare_cached($delete);

foreach $dataAliasMergeId (keys %daliasesMerge) {
  $dataAliasMerge = $daliasesMerge{$dataAliasMergeId};
  if ($dataAliasMerg eq $mrkrAbbrevInto) {
    $curUpdateMrkrHistory->execute($dataAliasMergeId);
    $curDelete->execute($dataAliasMergeId);
  } else {
      foreach $dataAliasIntoId (keys %daliasesInto) {
        $dataAliasInto = $daliasesInto{$dataAliasIntoId};
        if ($dataAliasInto eq $dataAliasMerge) {
          $curUpdateMrkrHistory->execute($dataAliasMergeId);
          $curDelete->execute($dataAliasMergeId);
        }
      }
  }
}

$curUpdateMrkrHistory->finish();

# run the merge action SQLs that do not contain record_attribution
$nonRecAttrSQL = "select mms_sql, mms_pk_id 
                    from merge_markers_sql 
                   where mms_mrkr_1_zdb_id = ?
                     and mms_mrkr_2_zdb_id = ?
                     and mms_sql not like '%record_attribution%'
                order by 2;";
$curGetNonRecAttrSQLs = $dbh->prepare($nonRecAttrSQL);
$curGetNonRecAttrSQLs->execute($mergeId, $intoId);
$curGetNonRecAttrSQLs->bind_columns(\$nonRecAttrSQL,\$nonRecAttrSQLid);
%nonRecAttrMergeSQLs = ();
while ($curGetNonRecAttrSQLs->fetch()) {
   $nonRecAttrSQLs{$nonRecAttrSQLid} = $nonRecAttrSQL;
}
$curGetNonRecAttrSQLs->finish();

foreach $sqlID (keys %nonRecAttrSQLs) {
   $nonRecAttrMergeSQL = $nonRecAttrSQLs{$sqlID};
   $curNonRecAttrSQL = $dbh->prepare($nonRecAttrMergeSQL);
   $curNonRecAttrSQL->execute();
   $curNonRecAttrSQL->finish();
    
   if ($nonRecAttrMergeSQL =~ m/delete/i) {
     $insertUpdatesSQL = "insert into updates(submitter_id,rec_id,new_value,comments,when,submitter_name)
                          values(?,?,'DELETE',?,CURRENT,?);";
     $curInsertUpdates = $dbh->prepare($insertUpdatesSQL);
     $curInsertUpdates->execute($person_id,$intoId,$nonRecAttrMergeSQL,$personFullName);
     $curInsertUpdates->finish();
   }
}

$getMarkerAbbrev2 = "select mrkr_abbrev, get_id('NOMEN') as daliasid from marker where mrkr_zdb_id = ?;";
$curGetMarkerAbbrev2 = $dbh->prepare($getMarkerAbbrev2);
$curGetMarkerAbbrev2->execute($mergeId);
$curGetMarkerAbbrev2->bind_columns(\$mrkrAbbrev, \$nomenID);
while ($curGetMarkerAbbrev2->fetch()) {
  $mrkrDeletedAbbrev = $mrkrAbbrev;
  $newNomenID = $nomenID;
}
$curGetMarkerAbbrev2->finish();

$updateMarkerHistory = "update marker_history set mhist_dalias_zdb_id = null where mhist_dalias_zdb_id = (select dalias_zdb_id from data_alias where dalias_alias = ? and dalias_data_zdb_id = ?) ;";
$curUpdateMarkerHistoy = $dbh->prepare_cached($updateMarkerHistory);
$curUpdateMarkerHistoy->execute($mrkrAbbrevInto,$mergeId);
$curUpdateMarkerHistoy->execute($mrkrDeletedAbbrev,$intoId);
$curUpdateMarkerHistoy->finish();

$deleteAlias = "delete from zdb_active_data where zactvd_zdb_id = (select dalias_zdb_id from data_alias where dalias_alias = ? and dalias_data_zdb_id = ?) ;";
$curDeleteAlias = $dbh->prepare_cached($deleteAlias);
$curDeleteAlias->execute($mrkrAbbrevInto,$mergeId);
$curDeleteAlias->execute($mrkrDeletedAbbrev,$intoId);
$curDeleteAlias->finish();

$updateAlias = "update data_alias set dalias_data_zdb_id = ? where dalias_data_zdb_id = ? ;";
$curUpdateAlias = $dbh->prepare($updateAlias);   
$curUpdateAlias->execute($intoId,$mergeId);
$curUpdateAlias->finish();

$getDaliasIdSQL = "select going.mrkr_zdb_id 
                     from data_alias, marker going, marker staying
                    where dalias_alias = lower(going.mrkr_abbrev)
                      and dalias_data_zdb_id = staying.mrkr_zdb_id
                      and going.mrkr_zdb_id = ?
                      and staying.mrkr_zdb_id = ? ;";

$curgetDaliasId = $dbh->prepare($getDaliasIdSQL);
$curgetDaliasId->execute($mergeId,$intoId);
$curgetDaliasId->bind_columns(\$mrkrId);
while ($curgetDaliasId->fetch()) {
  $daliasMrkrId = $mrkrAbbrev;
}
$curgetDaliasId->finish();

$insertZdbActiveData = "insert into zdb_active_data values(?);";
$curInsertZdbActiveData = $dbh->prepare_cached($insertZdbActiveData);
if (defined $daliasMrkrId && $daliasMrkrId eq $mergeId) {
    $newDaliasID = "";
} else {
    $curInsertZdbActiveData->execute($newDaliasID);
    $addNewDataAlias = "insert into data_alias (
                     		dalias_zdb_id, dalias_data_zdb_id,
                    		dalias_alias, dalias_group_id,
		     		dalias_alias_lower ) 
                         values ( ?, ?, ?, '1', ? );";
    $curAddNewDataAlias = $dbh->prepare($addNewDataAlias);
    $curAddNewDataAlias->execute($newDaliasID,$intoId,$mrkrDeletedAbbrev,$mrkrDeletedAbbrev);
    $curAddNewDataAlias->finish();
}

# marker_history
$curInsertZdbActiveData->execute($newNomenID);
$curInsertZdbActiveData->finish();

$addNewMarkerHistory = "insert into marker_history (
                                       mhist_zdb_id, mhist_mrkr_zdb_id, 
                                       mhist_event, mhist_reason, mhist_date,
                                       mhist_mrkr_name_on_mhist_date,
                                       mhist_mrkr_abbrev_on_mhist_date,
                                       mhist_comments,mhist_dalias_zdb_id )
                            values ( ?, ?, 'merged', 'same marker', CURRENT, ?, ?, 'none', ?);";
$curAddNewMarkerHistory = $dbh->prepare($addNewMarkerHistory);
$curAddNewMarkerHistory->execute($newNomenID,$intoId,$mrkrNameInto,$mrkrAbbrevInto,$newDaliasID);
$curAddNewMarkerHistory->finish();

$updateMarkerHistory = "update marker_history set mhist_mrkr_zdb_id = ? where mhist_mrkr_zdb_id = ? ;";
$curUpdateMarkerHistory = $dbh->prepare($updateMarkerHistory);   
$curUpdateMarkerHistory->execute($intoId,$mergeId);
$curUpdateMarkerHistory->finish();

# regen_names
$regenNames = "execute procedure regen_names_marker(?);";
$curRegenNames = $dbh->prepare($regenNames);
$curRegenNames->execute($intoId);
$curRegenNames->finish();

# regne_genox
$regenGenox = "execute procedure regen_genox_marker(?);";
$curRegenGenox = $dbh->prepare($regenGenox);             
$curRegenGenox->execute($intoId);
$curRegenGenox->finish();

# zdb_replaced_data and delete the marker that will be merged 
$updateZdbReplacedData = "update zdb_replaced_data set zrepld_new_zdb_id = ?, zrepld_old_name = ?  where zrepld_new_zdb_id = ? ;";
$curUpdateZdbReplacedData = $dbh->prepare($updateZdbReplacedData);   
$curUpdateZdbReplacedData->execute($intoId,$mrkrDeletedAbbrev,$mergeId);
$curUpdateZdbReplacedData->finish();

$deleteTheMarker = "delete from zdb_active_data where zactvd_zdb_id = ? ;";
$curDeleteTheMarker = $dbh->prepare($deleteTheMarker);
$curDeleteTheMarker->execute($mergeId);
$curDeleteTheMarker->finish();

$addNewZdbReplacedData = "insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
                                                 values ( ?, ?, ? );";
$curAdddNewZdbReplacedData = $dbh->prepare($addNewZdbReplacedData);
$curAdddNewZdbReplacedData->execute($mergeId,$intoId,$mrkrDeletedAbbrev);
$curAdddNewZdbReplacedData->finish();

##close DBG;

### close database connection
$dbh->disconnect();

print <<EOA;

<HEAD>
    <script language="JavaScript">    
    window.location.href='/action/marker/view/$intoId';
</script>  
</HEAD>
<BODY>
    Database scanned. Now merging.
</BODY>
</HTML>
EOA
    
exit;

