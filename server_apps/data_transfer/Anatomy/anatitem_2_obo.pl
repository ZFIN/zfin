#!/private/bin/perl -w
#
# Query ANATOMY_ITEM and ANATOMY_RELATIONSHIP, generate OBO format output 
# anatomy items. 
#
# Input: 
#        dbname
#        share flag : optional. If share is specified,
#                     the file output goes to OBO site,
#                     otherwise, to curators.
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

#this variable is global, used in sub getParents() too
my $shareVersion;
$shareVersion = 't' if ( $ARGV[0] && $ARGV[0] eq "share") ;

# In dagedit the root cann't have any relationship attribution.
# Since zfin anatomy terms always have start stage and end stage,
# we need to make up a fake root. 
my $rootAnatId = "ZFA:0100000";  #this constant is also referred by parseOBO.pl
print "\n";
print "[Term]\n";
print "id: $rootAnatId \n";
print "name: Zebrafish Anatomy\n";
print "namespace: zebrafish_anatomy\n";


# ---- query anatomy table ----

my $condition = '';
$condition = "where anatitem_obo_id[1,3] = 'ZFA' " if $shareVersion;

my $anat_sql = "select anatitem_zdb_id, anatitem_name, 
                       str.stg_obo_id, stp.stg_obo_id,
                       anatitem_definition, anatitem_obo_id, 
                       anatitem_description, anatitem_is_obsolete
                  from anatomy_item join
                       stage str on str.stg_zdb_id = anatitem_start_stg_zdb_id
                       join stage stp on stp.stg_zdb_id = anatitem_end_stg_zdb_id "
               .$condition
               ." order by anatitem_zdb_id ";


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
	$anatDef =~ s/\n/ /g if $anatDef;  # '\n' would break the OBO parse
	$anatDef =~ s/\"/\'/g if $anatDef;
	
	#--------------------------------
	#-- Basic information
        #--------------------------------
	print "\n";
	print "[Term]\n";
	print "id: $anatOboId\n";
	print "name: $anatName\n";
	print "namespace: zebrafish_anatomy\n";
       	print "xref_analog: ZFIN:$anatId\n";

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
	    my ($anatSynonym,$anatSynAttrib) = split (/\|/,$anatSynonymAttr);
	    $anatSynAttrib = $anatSynAttrib ? "ZFIN:$anatSynAttrib": "";
	    print "related_synonym: \"$anatSynonym\" [$anatSynAttrib] \n";
	}

	#--------------------------------
	#-- Definition
        #--------------------------------
	print "def: \"$anatDef\" [ZFIN:curator]\n" if $anatDef;

	#--------------------------------
	#-- Comment 
        #-- not included in the version sent to OBO
        #--------------------------------
	print "comment: $anatDesc \n" if (! $shareVersion && $anatDesc); 

	#--------------------------------
	#-- Stage
        #--------------------------------
  	print "relationship: start $anatStartStg\n";
	print "relationship: end $anatEndStg\n";

	#--------------------------------
	#-- Relationship
        #--------------------------------
	my @anatParent_Type_array = &getParents ($anatId);
	if (! @anatParent_Type_array) {
	    # fake a parent "Zebrafish Anatomy" to first level terms
	    print "is_a: $rootAnatId\n";
	}
	else {
	    foreach my $anatParent_Type (@anatParent_Type_array) {
		my($anatParentId, $anatDageditId) = split(/\|/, $anatParent_Type);
		if ($anatDageditId eq "is_a") {
		    print "$anatDageditId: $anatParentId \n";
		}else {
		    print "relationship: $anatDageditId $anatParentId \n";
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

    my $alias_sql = "select dalias_alias, recattrib_source_zdb_id
                       from data_alias left outer join record_attribution
                            on dalias_zdb_id = recattrib_data_zdb_id
                      where dalias_data_zdb_id = ?";
    my $alias_sth = $dbh->prepare($alias_sql)
	    or &reportError("Couldn't prepare the statement:$!\n");
    $alias_sth->execute($anatZdbId) or &reportError( "Couldn't execute the statement:$!\n");

    while (my ($alias_name, $alias_attribution) = $alias_sth->fetchrow_array()) {
	$alias_attribution = $alias_attribution ? $alias_attribution : "";
	push @alias_array, join("|", $alias_name, $alias_attribution);
    }
    return @alias_array;
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
    my $condition = '';
    $condition = "and anatitem_obo_id[1,3] = 'ZFA' " if $shareVersion;

    my $arel_sql = "select anatitem_obo_id, anatrel_dagedit_id
                      from anatomy_relationship join 
                           anatomy_item on anatrel_anatitem_1_zdb_id = anatitem_zdb_id 
                     where anatrel_anatitem_2_zdb_id = ? "
                     .$condition;

    my $arel_sth = $dbh->prepare($arel_sql)
	    or  &reportError("Couldn't prepare the statement:$!\n");

    $arel_sth->execute($anatZdbId) or &reportError("Couldn't execute the statement:$!\n");

    while (my @row = $arel_sth->fetchrow_array()) {
	push @arel_array, $row[0]."|".$row[1];
    }
    return @arel_array;
}

