#!/private/bin/perl -w
#
# Query ANATOMY_ITEM and ANATOMY_RELATIONSHIP, generate OBO format output 
# anatomy items. 
#
# Input: 
#        dbname
#   
#
# Output: STDOUT
#
# Info: source the environment first
#
use strict;
use DBI;
require "err_report.pl";

#======================================================
# main

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
    || &reportError("Failed while connecting to <!--|DB_NAME|--> "); 

# In dagedit the root cann't have any relationship attribution.
# Since zfin anatomy terms always have start stage and end stage,
# we need to make up a fake root. 
my $rootAnatId = "ZFA:0100000";  #this constant is also referred by parseOBO.pl
my $rootAnatName = "zebrafish anatomical entity";
print "\n";
print "[Term]\n";
print "id: $rootAnatId \n";
print "name: zebrafish anatomical entity\n";
print "namespace: zebrafish_anatomy\n";


# ---- query anatomy table ----


my $anat_sql = "select anatitem_zdb_id, anatitem_name, 
                       str.stg_obo_id, stp.stg_obo_id,
                       anatitem_definition, anatitem_obo_id, 
                       anatitem_description, anatitem_is_obsolete,
                       anatitem_is_cell, 
                       str.stg_name, stp.stg_name
                  from anatomy_item, stage str, stage stp
                 where str.stg_zdb_id = anatitem_start_stg_zdb_id
                   and stp.stg_zdb_id = anatitem_end_stg_zdb_id
                 order by anatitem_obo_id ";


my $anat_sth = $dbh->prepare($anat_sql) 
    or &reportError("Couldn't prepare the statement:$!\n");
$anat_sth->execute() or &reportError("Couldn't execute the statement:$!\n");

# ---- output each anatomy item as an item, with id, name, and so ----
while (my @data = $anat_sth->fetchrow_array()) {

	my $anatId       = $data[0];
	my $anatName     = $data[1];
	my $anatStartStg = $data[2];
	my $anatEndStg   = $data[3];
	my $anatDef      = $data[4];
	my $anatOboId    = $data[5];
	my $anatDesc     = $data[6];
	my $anatIsObsolete = $data[7];
	my $anatIsCell   = $data[8];
	my $anatStartStgName = $data[9];
	my $anatEndStgName = $data[10];

	$anatDef =~ s/\n/ /g if $anatDef;  # '\n' would break the OBO parse
	$anatDef =~ s/\"/\'/g if $anatDef;
	
	#--------------------------------
	#-- Id
        #--------------------------------
	print "\n";
	print "[Term]\n";
	print "id: $anatOboId\n";

        #----------------------------------
        #-- Alternative ZFA ids
        #-- 
	#-----------------------------------
	foreach my $anatAltId (&getAltId ($anatId)) {
	    print "alt_id: $anatAltId\n";
	}

	#--------------------------------
	#-- Name & ZDB id
        #--------------------------------

	print "name: $anatName\n";
	print "namespace: zebrafish_anatomy\n";
       	print "xref: ZFIN:$anatId\n";

	#--------------------------------
	#-- Dblink ids
        #--------------------------------
	foreach my $anatDblinkAcc (&getDblinkAcc ($anatId)) {
	    print "xref: $anatDblinkAcc\n";
	}

        #----------------------------------
        #-- Obsolete term
        #-- stage info are kept in db only for constraint reason. 
	#-----------------------------------
	if ( $anatIsObsolete eq "t" ) {
	    print "is_obsolete: true\n";
	    next;
	}

	#--------------------------------
	#-- Synonym
        #-- we try to avoid more than one entry(attribution),
        #-- but even worse case comes, Dagedit does handle it. 
        #--------------------------------
	foreach my $anatSynonymAttr (&getSynonyms ($anatId)) {
	    my ($anatSynonym,$anatSynGroup,$anatSynAttrib) = split (/\|/,$anatSynonymAttr);
	    #$anatSynAttrib = $anatSynAttrib ? "ZFIN:$anatSynAttrib": "";
	   #print "synonym: \"$anatSynonym\" RELATED $anatSynGroup [$anatSynAttrib] \n";
            if ($anatSynGroup eq "exact plural") {
	      print "synonym: \"$anatSynonym\" EXACT PLURAL [$anatSynAttrib] \n";
	}
            if ($anatSynGroup eq "related plural") {
	       print "synonym: \"$anatSynonym\" RELATED PLURAL [$anatSynAttrib] \n";
	}
            if ($anatSynGroup eq "related alias") {
	      print "synonym: \"$anatSynonym\" RELATED [$anatSynAttrib] \n";
	}
            if ($anatSynGroup eq "exact alias") {
	      print "synonym: \"$anatSynonym\" EXACT [$anatSynAttrib] \n";
	}
  }

	#--------------------------------
	#-- Definition
        #--------------------------------
	my $anatDefAttr = "";
	$anatDefAttr = join (", ZFIN:", &getDefAttrib ($anatId));
	if ($anatDefAttr) {
	    $anatDefAttr = "[ZFIN:".$anatDefAttr."]" if $anatDefAttr;
	}else {
	    $anatDefAttr = ($anatIsCell eq "t") ? "[CL:curator]" :"[ZFIN:curator]";
	}

	print "def: \"$anatDef\" $anatDefAttr\n" if $anatDef;

	#--------------------------------
	#-- Comment 
        #-- not included in the version sent to OBO
        #--------------------------------
	print "comment: $anatDesc \n" if ($anatDesc); 

	#--------------------------------
	#-- Stage
        #--------------------------------
  	print "relationship: start $anatStartStg ! $anatStartStgName\n";
	print "relationship: end $anatEndStg ! $anatEndStgName\n";

	#--------------------------------
	#-- Relationship
        #--------------------------------
	my @anatParent_Type_array = &getParents ($anatId);
	if (! @anatParent_Type_array) {
	    # fake a parent "Zebrafish Anatomy" to first level terms
	    print "is_a: $rootAnatId ! $rootAnatName\n";
	}
	else {
	    foreach my $anatParent_Type (@anatParent_Type_array) {
		my($anatParentId,  $anatParentName, $anatDageditId) = split(/\|/, $anatParent_Type);
		if ($anatDageditId eq "is_a") {
		    print "$anatDageditId: $anatParentId ! $anatParentName\n";
		}else {
		    print "relationship: $anatDageditId $anatParentId  ! $anatParentName\n";
		}
	    }
	}  #---------- end of if exists anatomy relationship 
    }  #------- end while loop --------

$dbh->disconnect;
exit;

#===============================================
# sub getSynonyms
#
# input:  anatitemZdbId
# ouput:  anatomy synonym array, each element
#         composes of synonym|attribution        
#
sub getSynonyms ($){
    my $anatZdbId = $_[0];
    my @alias_array = ();

    my $alias_sql = "select dalias_alias, dalias_group, recattrib_source_zdb_id
                       from data_alias left outer join record_attribution
                            on dalias_zdb_id = recattrib_data_zdb_id
                      where dalias_data_zdb_id = ?
                        and dalias_group in ('related alias','related plural', 'exact alias', 'exact plural')";
    my $alias_sth = $dbh->prepare($alias_sql)
	    or &reportError("Couldn't prepare the statement:$!\n");
    $alias_sth->execute($anatZdbId) or &reportError( "Couldn't execute the statement:$!\n");

    while (my ($alias_name, $alias_group, $alias_attribution) = $alias_sth->fetchrow_array()) {
	$alias_attribution = $alias_attribution ? $alias_attribution : "";
	push @alias_array, join("|", $alias_name, $alias_group, $alias_attribution);
    }
    return @alias_array;
}

#===============================================
# sub getAltId
#
# input:  anatitemZdbId
# ouput:  anatomy ZFA alt id array      
#
sub getAltId ($){
    my $anatZdbId = $_[0];
    my @altid_array = ();

    my $altid_sql = "select dalias_alias
                       from data_alias 
                      where dalias_data_zdb_id = ?
                        and dalias_group = 'secondary id'";
    my $altid_sth = $dbh->prepare($altid_sql)
	    or &reportError("Couldn't prepare the statement:$!\n");
    $altid_sth->execute($anatZdbId) or &reportError( "Couldn't execute the statement:$!\n");

    while (my $alt_id = $altid_sth->fetchrow_array()) {
	push @altid_array,  $alt_id;
    }
    return @altid_array;
}
#===============================================
# sub getDblinkAcc
#
# input:  anatitemZdbId
# ouput:  anatomy ZFA dblink accession array      
#
sub getDblinkAcc ($){
    my $anatZdbId = $_[0];
    my @dblinkacc_array = ();

    my $dblinkacc_sql = "select dblink_acc_num
                       from db_link
                      where dblink_linked_recid = ?";
    my $dblinkacc_sth = $dbh->prepare($dblinkacc_sql)
	    or &reportError("Couldn't prepare the statement:$!\n");
    $dblinkacc_sth->execute($anatZdbId) or &reportError( "Couldn't execute the statement:$!\n");

    while (my $dblink_acc = $dblinkacc_sth->fetchrow_array()) {
	push @dblinkacc_array,  $dblink_acc;
    }
    return @dblinkacc_array;
}

#===============================================
# sub getDefAttrib
#
# input:  anatitemZdbId
# ouput:  definition attribution
#
sub getDefAttrib ($) {
   my $anatZdbId = $_[0];
   my @attrib_array = ();
   
   my $defattrib_sql = "select recattrib_source_zdb_id
                       from record_attribution
                      where recattrib_data_zdb_id = ?
                        and recattrib_source_type = 'anatomy definition'";
    my $defattrib_sth = $dbh->prepare($defattrib_sql)
	    or &reportError("Couldn't prepare the statement:$!\n");
    $defattrib_sth->execute($anatZdbId) or &reportError( "Couldn't execute the statement:$!\n");

    while (my $attrib_id = $defattrib_sth->fetchrow_array()) {
	push @attrib_array,  $attrib_id;
    }
    return @attrib_array;
}


#===============================================
# sub getParents
#
# input:  anatitemZdbId
# ouput:  an array of parentId|dagedit_Id
#
sub getParents ($){
    my $anatZdbId = $_[0];
    my @arel_array = ();
  
    my $arel_sql = "select anatitem_obo_id, anatitem_name, anatrel_dagedit_id
                      from anatomy_relationship join 
                           anatomy_item a1 
                             on anatrel_anatitem_1_zdb_id = a1.anatitem_zdb_id 
                     where anatrel_anatitem_2_zdb_id = ? ";

    my $arel_sth = $dbh->prepare($arel_sql)
	    or  &reportError("Couldn't prepare the statement:$!\n");

    $arel_sth->execute($anatZdbId) or &reportError("Couldn't execute the statement:$!\n");

    while (my @row = $arel_sth->fetchrow_array()) {
	push @arel_array, join("|",@row);
    }
    return @arel_array;
}
