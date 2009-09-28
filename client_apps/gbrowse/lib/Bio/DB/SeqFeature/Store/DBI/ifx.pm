package Bio::DB::SeqFeature::Store::DBI::ifx;

use Devel::StackTrace; # TEC Debugging
#my $trace = Devel::StackTrace->new;  # TEC Debugging
#print $trace->as_string; # like carp # TEC Debugging
#while(my $frame = $trace->next_frame){
#	print "#########################################################\n";
#	print  $frame->as_string ." \n";
#}


=head1 NAME

Bio::DB::SeqFeature::Store::DBI::ifx -- Informix implementation of Bio::DB::SeqFeature::Store

=head1 SYNOPSIS

  use Bio::DB::SeqFeature::Store;

  # Open the sequence database
  my $db = Bio::DB::SeqFeature::Store->new(-adaptor => 'DBI::ifx',
                                          -dsn     => 'dbi:Informix:gff3_schemat');

  # get a feature from somewhere
  my $feature = Bio::SeqFeature::Generic->new(...);

  # store it
  $db->store($feature) or die "Couldn't store!";

  # primary ID of the feature is changed to indicate its primary ID
  # in the database...
  my $id = $feature->primary_id;

  # get the feature back out
  my $f  = $db->fetch($id);

  # change the feature and update it
  $f->start(100);
  $db->update($f) or die "Couldn't update!";

  # searching...
  # ...by id
  my @features = $db->fetch_many(@list_of_ids);

  # ...by name
  @features = $db->get_features_by_name('ZK909');

  # ...by alias
  @features = $db->get_features_by_alias('sma-3');

  # ...by type
  @features = $db->get_features_by_name('gene');

  # ...by location
  @features = $db->get_features_by_location(-seq_id=>'Chr1',-start=>4000,-end=>600000);

  # ...by attribute
  @features = $db->get_features_by_attribute({description => 'protein kinase'})

  # ...by the GFF "Note" field
  @result_list = $db->search_notes('kinase');

  # ...by arbitrary combinations of selectors
  @features = $db->features(-name => $name,
                            -type => $types,
                            -seq_id => $seqid,
                            -start  => $start,
                            -end    => $end,
                            -attributes => $attributes);

  # ...using an iterator
  my $iterator = $db->get_seq_stream(-name => $name,
                                     -type => $types,
                                     -seq_id => $seqid,
                                     -start  => $start,
                                     -end    => $end,
                                     -attributes => $attributes);

  while (my $feature = $iterator->next_seq) {
    # do something with the feature
  }

  # ...limiting the search to a particular region
  my $segment  = $db->segment('Chr1',5000=>6000);
  my @features = $segment->features(-type=>['mRNA','match']);

  # getting & storing sequence information
  # Warning: this returns a string, and not a PrimarySeq object
  $db->insert_sequence('Chr1','GATCCCCCGGGATTCCAAAA...');
  my $sequence = $db->fetch_sequence('Chr1',5000=>6000);

  # what feature types are defined in the database?
  my @types    = $db->types;

  # create a new feature in the database
  my $feature = $db->new_feature(-primary_tag => 'mRNA',
                                 -seq_id      => 'chr3',
                                 -start      => 10000,
                                 -end        => 11000);

=head1 DESCRIPTION

Bio::DB::SeqFeature::Store::ifx is the Informix adaptor for
Bio::DB::SeqFeature::Store. You will not create it directly, but
instead use Bio::DB::SeqFeature::Store-E<gt>new() to do so.

See L<Bio::DB::SeqFeature::Store> for complete usage instructions.

=head2 Using the Informix adaptor

Before you can use the adaptor, you must use the dbaccess admin tool to
create a database and establish a user account with write permission.

To establish a connection to the database, call
Bio::DB::SeqFeature::Store-E<gt>new(-adaptor=E<gt>'DBI::ifx',@more_args). The
additional arguments are as follows:

  Argument name       Description
  -------------       -----------

 -dsn              The database name. You can abbreviate
                   "dbi:ifx:foo" as "foo" if you wish.

 -user             Username for authentication.

 -pass             Password for authentication.

 -namespace        A prefix to attach to each table. This allows you
                   to have several virtual databases in the same
                   physical database.

 -temp             Boolean flag. If true, a temporary database
                   will be created and destroyed as soon as
                   the Store object goes out of scope. (synonym -temporary)

 -autoindex        Boolean flag. If true, features in the database will be
                   reindexed every time they change. This is the default.


 -tmpdir           Directory in which to place temporary files during "fast" loading.
                   Defaults to File::Spec->tmpdir(). (synonyms -dump_dir, -dumpdir, -tmp)

 -dbi_options      A hashref to pass to DBI->connect's 4th argument, the "attributes."
                   (synonyms -options, -dbi_attr)

 -write            Pass true to open database for writing or updating.

If successful, a new instance of
Bio::DB::SeqFeature::Store::DBI::ifx will be returned.

In addition to the standard methods supported by all well-behaved
Bio::DB::SeqFeature::Store databases, several following
adaptor-specific methods are provided. These are described in the next
sections.

=cut

use strict;

use base 'Bio::DB::SeqFeature::Store';
use Bio::DB::SeqFeature::Store::DBI::Iterator;
use DBD::Informix qw(:ix_types);
use Memoize;
use Cwd 'abs_path';
use Bio::DB::GFF::Util::Rearrange 'rearrange';
use Bio::SeqFeature::Lite;
use File::Spec;
use constant DEBUG=>1;

# from the MySQL documentation...
# WARNING: if your sequence uses coordinates greater than 2 GB, you are out of luck!
use constant MAX_INT =>  2_147_483_647;
use constant MIN_INT => -2_147_483_648;
use constant MAX_BIN =>  1_000_000_000;  # size of largest feature = 1 Gb
use constant MIN_BIN =>  1000;           # smallest bin we'll make - on a 100 Mb chromosome, there'll be 100,000 of these
#print STDERR "$_\n" foreach @INC;

memoize('_typeid');
memoize('_locationid');
memoize('_attributeid');
memoize('dump_path');

###
# object initialization
#
sub init {
  my $self          = shift;
  my ($dsn,
      $is_temporary,
      $autoindex,
      $namespace,
      $dump_dir,
      $dbi_options,
      $writeable,
      $create,
     ) = rearrange(['DSN',
		    ['TEMP','TEMPORARY'],
		    'AUTOINDEX',
		    'NAMESPACE',
		    ['DUMP_DIR','DUMPDIR','TMP','TMPDIR'],
		    ['OPTIONS','DBI_OPTIONS','DBI_ATTR'],
		    ['WRITE','WRITEABLE'],
		    'CREATE',
		   ],@_);
  $dbi_options  ||= {};
  $writeable    = 1 if $is_temporary or $dump_dir;

  $dsn or $self->throw("Usage: ".__PACKAGE__."->init(-dsn => \$dbh || \$dsn)");

  my $dbh;
  if (ref $dsn) {
    $dbh = $dsn;
  } else {
    $dsn = "dbi:Informix:$dsn" unless $dsn =~ /^dbi:/;
    $dbh = DBI->connect($dsn,$dbi_options) or $self->throw($DBI::errstr);
    #$dbh->{mysql_auto_reconnect} = 1;  #TEC is not safe or recomended
  }
  $self->{dbh}       = $dbh;
  $self->{is_temp}   = $is_temporary;
  $self->{namespace} = $namespace;
  $self->{writeable} = $writeable;

  $self->default_settings;
  $self->autoindex($autoindex)                   if defined $autoindex;
  $self->dumpdir($dump_dir)                      if $dump_dir;
  if ($self->is_temp) {
    $self->init_tmp_database();
  } elsif ($create) {
    $self->init_database('erase');
  }
}

sub writeable { shift->{writeable} }

sub can_store_parentage { 1 }

sub table_definitions {
  my $self = shift;
  my $fragment_by = "fragment by round robin in tbldbs1,tbldbs2,tbldbs3\n";
  my $extent_size = "extent size 16384 next size 16384\n";
  return {
	  feature => <<END,
(
  id       serial   not null constraint gb_feature_id_not_null,
  typeid   integer  default 0 not null constraint gb_feature_typeid_not_null,
  seqid    integer  not null  constraint gb_feature_seqid_not_null,
  start    integer,
  end      integer,
  strand   smallint default 0,
  tier     smallint,
  bin      integer,
  indexed  smallint default 1,
  object   blob     not null constraint gb_feature_object_not_null,
  primary key(id) constraint pk_gb_feature,
  foreign key(seqid) references locationlist(id) constraint gb_feature_locationlist_fk,
  foreign key(typeid) references typelist(id) constraint gb_feature_typelist_fk
)$fragment_by  PUT object in (smartbs1)(log) $extent_size  lock mode row
END

	  locationlist => <<END,
(
  id         serial        not null constraint gb_locationlist_id_not_null,
  seqname    varchar(50)   not null constraint gb_locationlist_seqname_not_null,
  primary key (id) constraint pk_gb_locationlist
)$fragment_by $extent_size  lock mode row
END

	  typelist => <<END,
(
  id       serial   not null constraint gb_typelist_id_not_null,
  tag      varchar(40)  not null constraint gb_typelist_tag_not_null,
  primary key (id) constraint pk_gb_typelist 
)$fragment_by $extent_size  lock mode row
END
	  name => <<END,
(
  id           integer       not null constraint gb_name_id_not_null,
  name         varchar(128)  not null constraint gb_name_name_not_null,
  display_name smallint      default 0,
  foreign key(id) references feature(id) constraint gb_name_feature_fk
)$fragment_by $extent_size  lock mode row
END

	  attribute => <<END,
(
  id               integer   not null constraint gb_attribute_attribute_id_not_null,
  attribute_id     integer   not null constraint gb_attribute_attribute_value_not_null,
  attribute_value  varchar(255),
  foreign key(id) references feature(id) constraint gb_attribute_feature_fk,
  foreign key(attribute_id) references attributelist(id) constraint gb_attribute_attributelist_fk
)$fragment_by $extent_size  lock mode row
END

	  attributelist => <<END,
(
  id      serial   not null constraint gb_attributelist_id_not_null,
  tag     varchar(50)  not null constraint gb_attributelist_tag_not_null,
  primary key (id) constraint pk_gb_attributelist
)$fragment_by $extent_size  lock mode row
END
	  parent2child => <<END,
(
  id      integer       not null constraint gb_parent2child_id_not_null,
  child   integer       not null constraint gb_parent2child_child_not_null,
  primary key (id,child) constraint pk_gb_parent2child,
  foreign key(id) references feature(id) constraint gb_parent2child_id_feature_fk, 
  foreign key(child) references feature(id) constraint gb_parent2child_child_feature_fk

)$fragment_by $extent_size  lock mode row
END

	  meta => <<END,
(
  name      varchar(128) not null constraint gb_meta_name_not_null,
  value     varchar(128) not null constraint gb_meta_value_not_null,
  primary key (name) constraint pk_gb_meta
)$fragment_by $extent_size  lock mode page
END
	  sequence => <<END,
(
  id       integer not null constraint gb_sequence_id_not_null,
  offset   integer not null constraint gb_sequence_offset_not_null,
  sequence clob,
  PRIMARY KEY (id,offset) CONSTRAINT pk_gb_sequence,
  foreign key(id) references locationlist(id) constraint gb_sequence_locationlist_fk
)$fragment_by PUT sequence in (smartbs1)(log) $extent_size  lock mode page
END

	 };
}
# informix creates non key indexes in their own statments
sub table_refinements {
  my $self = shift;
  return {
	  feature => <<END,
CREATE INDEX gb_feature_seq_tier_bin_type_idx on feature(seqid,tier,bin,typeid)in idxdbs3
END
	  locationlist => <<END,
CREATE INDEX gb_locationlist_seqname_idx ON locationlist(seqname) in idxdbs2
END
	  typelist => <<END,
CREATE INDEX gb_typelist_tag_idx ON typelist(tag) in idxdbs1
END
	  name => <<END,
CREATE INDEX gb_name_name_idx ON name(name) in idxdbs3
END
	  attribute => <<END,
CREATE INDEX gb_attribute_att_id_att_value_idx ON attribute(attribute_id,attribute_value) in idxdbs2
END
	  attributelist => <<END,
CREATE INDEX gb_attributelist_tag_idx ON attributelist(tag) in idxdbs3
END
	 };
}


###
# default settings -- will create and populate meta table if needed
#
sub default_settings {
  my $self = shift;
  $self->maybe_create_meta();
  $self->SUPER::default_settings;
  $self->autoindex(1);
  $self->dumpdir(File::Spec->tmpdir);
}


###
# retrieve database handle
#
sub dbh {
  my $self = shift;
  my $d    = $self->{dbh};
  $self->{dbh} = shift if @_;
  $d;
}

sub clone {
    my $self = shift;
    $self->{dbh}{InactiveDestroy} = 1;
    $self->{dbh} = $self->{dbh}->clone
	unless $self->is_temp;
}

###
# get/set directory for bulk load tables
#
sub dumpdir {
  my $self = shift;
  my $d = $self->{dumpdir};
  $self->{dumpdir} = abs_path(shift) if @_;
  $d;
}

###
# table namespace (multiple dbs in one mysql db)
#
sub namespace {
  my $self = shift;
  my $d    = $self->{namespace};
  $self->{namespace} = shift if @_;
  $d;
}

###
# find a path that corresponds to a dump table
#
sub dump_path {
  my $self = shift;
  my $table = $self->_qualify(shift);
  return "$self->{dumpdir}/$table.$$";
}

###
# make a filehandle (writeable) that corresponds to a dump table
#
sub dump_filehandle {
  my $self = shift;
  my $table = shift;
  eval "require IO::File" unless IO::File->can('new');
  my $path  = $self->dump_path($table);
  my $fh = $self->{filehandles}{$path} ||= IO::File->new(">$path");
  $fh;
}

###
# find the next ID for a feature (used only during bulk loading)
#
sub next_id {
  my $self = shift;
  $self->{max_id} ||= $self->max_id;
  return ++$self->{max_id};
}

###
# find the maximum ID for a feature (used only during bulk loading)
#
sub max_id {
  my $self = shift;
  my $sth  = $self->_prepare("SELECT max(id) from feature");
  $sth->execute or $self->throw($sth->errstr);
  my ($id) = $sth->fetchrow_array;
  $id;
}

###
# wipe database clean and reinstall schema
#
sub _init_database {
  my $self = shift;
  my $erase = shift;

  my $dbh    = $self->dbh;
  my $tables = $self->table_definitions;
  my $refine = $self->table_refinements;
  #foreach (keys %$tables) {
  # when foreign keys exist the order tables are created in matter
  # the order the hash is created in is not kept, so recreating
  
  my @tblst = (
   	'typelist', 
	'locationlist',
		'sequence',
		'feature',
			'name',
			'parent2child',
			'attributelist',
				'attribute','meta');
				
				
	foreach(reverse @tblst){# delete in reverse order
		if ($erase &&  $_ ne 'meta'){
			my $table = $self->_qualify($_);
			eval { # TEC drop existing
				local $self->dbh->{PrintError} = 0;
				local $self->dbh->{RaiseError} = 0;
				$self->dbh->do("DROP TABLE $table");
			};	
		}
	}		
	foreach(@tblst){# (re)create in order
		next if $_ eq 'meta';      # don't get rid of meta data!
		my $table = $self->_qualify($_);
		if ($erase){
			eval { # TEC experimental... dont create existing
				local $self->dbh->{PrintError} = 0;
				local $self->dbh->{RaiseError} = 1;
				$self->dbh->do("select first 1 'a' from $table");
			};
			if($@){
				my $query = "CREATE TABLE $table $tables->{$_}";
				$self->dbh->do($query) or $self->throw($self->dbh->errstr);
				if ($refine->{$_}){
					$self->dbh->do($refine->{$_}) or $self->throw($self->dbh->errstr);
				}
			}
		}
  }
  $self->subfeatures_are_indexed(1) if $erase;
  1;
}

sub maybe_create_meta {
  my $self = shift;
  return unless $self->writeable;
  my $table  = $self->_qualify('meta');
  my $tables = $self->table_definitions;
  my $refine = $self->table_refinements;
  my $temporary = $self->is_temp ? 'TEMP' : '';

  	  ######################################
	  eval { # TEC experimental... check existing or create new
		local $self->dbh->{PrintError} = 0;
		local $self->dbh->{RaiseError} = 1;
		$self->dbh->do("Select 1 from meta");
	  };
	  if($@){
		$self->dbh->do("CREATE $temporary TABLE $table $tables->{meta}");
	  }
	  ########################################
}
###
### This may be a problem since informix does
### not create tables and TEMP tables in the same dbspaces
### and ZFIN does not create tables in the default dbspaces
###


sub init_tmp_database {
  my $self = shift;
  my $dbh    = $self->dbh;
  my $tables = $self->table_definitions;
  my $refine = $self->table_refinements;
  for my $t (keys %$tables) {
      my $table = $self->_qualify($t);
	  ######################################
	  eval { # TEC experimental... truncate existing or create new
		local $self->dbh->{PrintError} = 0;
		local $self->dbh->{RaiseError} = 1;
		$self->dbh->do("TRUNCATE TABLE $table");
	  };
	  if($@){
		my $query = "CREATE TEMP TABLE $table $tables->{$t} with no log";
		#$self->dbh->trace(1);
		$self->dbh->do($query) or $self->throw($self->dbh->errstr);
		#$self->dbh->trace(0);
		$self->dbh->do($refine->{$t}) or $self->throw($self->dbh->errstr);
	  }
	  ##########################################
  }
  1;
}

###
# use temporary tables
#
sub is_temp {
  shift->{is_temp};
}

sub attributes {
    my $self = shift;
    my $dbh  = $self->dbh;
    my $attributelist_table = $self->_attributelist_table;
    
    my $a    = $dbh->selectcol_arrayref("SELECT tag FROM $attributelist_table")
       or $self->throw($dbh->errstr);
    return @$a;
}

sub _store {
  my $self    = shift;

  # special case for bulk updates
  return $self->_dump_store(@_) if $self->{bulk_update_in_progress};

  my $indexed = shift;
  my $count = 0;

  my $autoindex = $self->autoindex;

  my $dbh = $self->dbh;
  local $dbh->{RaiseError} = 1;
  $dbh->begin_work;
  eval {
    for my $obj (@_) {	
      $self->replace($obj,$indexed);
      $self->_update_indexes($obj) if $indexed && $autoindex;
      $count++;
    }
  };

  if ($@) {
    warn "Transaction aborted because $@";
    $dbh->rollback;
  }
  else {
    $dbh->commit;
  }

  # remember whether we are have ever stored a non-indexed feature
  unless ($indexed or $self->{indexed_flag}++) {
    $self->subfeatures_are_indexed(0);
  }
  $count;
}

# we memoize this in order to avoid making zillions of calls
sub autoindex {
  my $self = shift;

  # special case for bulk update -- need to build the indexes
  # at the same time we build the main feature table
  return 1 if $self->{bulk_update_in_progress};
  my $d = $self->setting('autoindex');
  $self->setting(autoindex=>shift) if @_;
  $d;
}

sub _start_bulk_update {
  my $self = shift;
  my $dbh  = $self->dbh;
  $self->{bulk_update_in_progress}++;
}

# tables written to the filesystem then loaded
# feature, name, attribute, parent2child

sub _finish_bulk_update { 
  my $self = shift;
  my $dbh  = $self->dbh;
  my $dir = $self->{dumpdir} || '.';
  #TEC for my $t ('feature','feature_blob',$self->index_tables){print "\n$t\n"; system "cp /tmp/$t.* /tmp/tomc/"; }
  	
  for my $table ('feature',$self->index_tables) {	
	my $fh = $self->dump_filehandle($table);
    my $path = $self->dump_path($table);
    $fh->close;	
    my $qualified_table = $self->_qualify($table);
	
	my $sql = "LOAD FROM '$path' DELIMITER '	' INSERT INTO $qualified_table";	
	print "\n$sql\n";
	system "head $path";

	# unload file is generated  in dump_store   line ~1800 
	# TEC-- generating a 201 syntax error but works fine in a shell???
	
    $dbh->do($sql) 
		or $self->throw($dbh->errstr);
    unlink $path;
  }
  delete $self->{bulk_update_in_progress};
  delete $self->{filehandles}; 
  unlink $self->dump_path('feature_blob');
# will need to delete $blob_fh too TEC
}


###
# Add a subparts to a feature. Both feature and all subparts must already be in database.
#
sub _add_SeqFeature {
  my $self     = shift;

  # special purpose method for case when we are doing a bulk update
  return $self->_dump_add_SeqFeature(@_) if $self->{bulk_update_in_progress};

  my $parent   = shift;
  my @children = @_;

  my $dbh = $self->dbh;
  local $dbh->{RaiseError} = 1;

  my $child_table = $self->_parent2child_table();
  my $count = 0;

  my $querydel = "DELETE FROM $child_table WHERE id = ? AND child = ?";
  my $query = "INSERT INTO $child_table (id,child) VALUES (?,?)";
  my $sthdel = $self->_prepare($querydel);
  my $sth = $self->_prepare($query);

  my $parent_id = (ref $parent ? $parent->primary_id : $parent)
    or $self->throw("$parent should have a primary_id");

  $dbh->begin_work or $self->throw($dbh->errstr);
  eval {
    for my $child (@children) {
      my $child_id = ref $child ? $child->primary_id : $child;
      defined $child_id or die "no primary ID known for $child";
      $sthdel->execute($parent_id, $child_id);
     $sth->execute($parent_id,$child_id);
      $count++;
    }
  };

  if ($@) {
    warn "Transaction aborted because $@";
    $dbh->rollback;
  }
  else {
    $dbh->commit;
  }
  $sth->finish;
  $count;
}

sub _fetch_SeqFeatures {
  my $self   = shift;
  my $parent = shift;
  my @types  = @_;

  my $parent_id = $parent->primary_id or $self->throw("$parent should have a primary_id");
  my $feature_table = $self->_feature_table;
  my $child_table   = $self->_parent2child_table();

  my @from  = ("$feature_table as f","$child_table as c");
  my @where = ('f.id=c.child','c.id=?');
  my @args  = $parent_id;

  if (@types) {
    my ($from,$where,undef,@a) = $self->_types_sql(\@types,'f');
    push @from,$from   if $from;
    push @where,$where if $where;
    push @args,@a;
  }

  my $from  = join ', ',@from;
  my $where = join ' AND ',@where;

  my $query = <<END;
SELECT f.id,f.object
  FROM $from
  WHERE $where
END

  $self->_print_query($query,@args) if DEBUG || $self->debug;

  my $sth = $self->_prepare($query) or $self->throw($self->dbh->errstr);

  $sth->execute(@args) or $self->throw($sth->errstr);
  return $self->_sth2objs($sth);
}

###
# get primary sequence between start and end
#
sub _fetch_sequence {
  my $self = shift;
  my ($seqid,$start,$end) = @_;

  # backward compatibility to the old days when I liked reverse complementing
  # dna by specifying $start > $end
  my $reversed;
  if (defined $start && defined $end && $start > $end) {
    $reversed++;
    ($start,$end) = ($end,$start);
  }
  $start-- if defined $start;
  $end--   if defined $end;

  my $offset1 = $self->_offset_boundary($seqid,$start || 'left');
  my $offset2 = $self->_offset_boundary($seqid,$end   || 'right');
  my $sequence_table = $self->_sequence_table;
  my $locationlist_table = $self->_locationlist_table;

  my $sth     = $self->_prepare(<<END);
SELECT sequence,offset
   FROM $sequence_table as s,$locationlist_table as ll
   WHERE s.id=ll.id
     AND ll.seqname= ?
     AND offset >= ?
     AND offset <= ?
   ORDER BY offset
END

  my $seq = '';
  $sth->execute($seqid,$offset1,$offset2) or $self->throw($sth->errstr);

  while (my($frag,$offset) = $sth->fetchrow_array) {
    substr($frag,0,$start-$offset) = '' if defined $start && $start > $offset;
    $seq .= $frag;
  }
  substr($seq,$end-$start+1) = '' if defined $end && $end-$start+1 < length($seq);
  if ($reversed) {
    $seq = reverse $seq;
    $seq =~ tr/gatcGATC/ctagCTAG/;
  }
  $sth->finish;
  $seq;
}

sub _offset_boundary {
  my $self = shift;
  my ($seqid,$position) = @_;

  my $sequence_table     = $self->_sequence_table;
  my $locationlist_table = $self->_locationlist_table;

  my $sql;
  $sql =  $position eq 'left'  ? "SELECT min(offset) FROM $sequence_table as s,$locationlist_table as ll WHERE s.id=ll.id AND ll.seqname=?"
         :$position eq 'right' ? "SELECT max(offset) FROM $sequence_table as s,$locationlist_table as ll WHERE s.id=ll.id AND ll.seqname=?"
	 :"SELECT max(offset) FROM $sequence_table as s,$locationlist_table as ll WHERE s.id=ll.id AND ll.seqname=? AND offset<=?";
  my $sth = $self->_prepare($sql);
  my @args = $position =~ /^-?\d+$/ ? ($seqid,$position) : ($seqid);
  $sth->execute(@args) or $self->throw($sth->errstr);
  my $boundary = $sth->fetchall_arrayref->[0][0];
  $sth->finish;
  return $boundary;
}


###
# add namespace to tablename
#
sub _qualify {
  my $self = shift;
  my $table_name = shift;
  my $namespace = $self->namespace;
  return $table_name unless defined $namespace;
  return "${namespace}_${table_name}";
}

###
# Fetch a Bio::SeqFeatureI from database using its primary_id
#
sub _fetch {
  my $self       = shift;
  @_ or $self->throw("usage: fetch(\$primary_id)");
  my $primary_id = shift;
  my $features = $self->_feature_table;
  my $sth = $self->_prepare(<<END);
SELECT id,object FROM $features WHERE id=?
END
  $sth->execute($primary_id) or $self->throw($sth->errstr);
  my $obj = $self->_sth2obj($sth);
  $sth->finish;
  $obj;
}

###
# Efficiently fetch a series of IDs from the database
# Can pass an array or an array ref
#
sub _fetch_many {
  my $self       = shift;
  @_ or $self->throw('usage: fetch_many($id1,$id2,$id3...)');
  my $ids = join ',',map {ref($_) ? @$_ : $_} @_ or return;
  my $features = $self->_feature_table;

  my $sth = $self->_prepare(<<END);
SELECT id,object FROM $features WHERE id IN ($ids)
END
  $sth->execute() or $self->throw($sth->errstr);
  return $self->_sth2objs($sth);
}

sub _features {
  my $self = shift;
  my ($seq_id,$start,$end,$strand,
      $name,$class,$allow_aliases,
      $types,
      $attributes,
      $range_type,
      $fromtable,
      $iterator,
      $sources
     ) = rearrange([['SEQID','SEQ_ID','REF'],'START',['STOP','END'],'STRAND',
		    'NAME','CLASS','ALIASES',
		    ['TYPES','TYPE','PRIMARY_TAG'],
		    ['ATTRIBUTES','ATTRIBUTE'],
		    'RANGE_TYPE',
		    'FROM_TABLE',
		    'ITERATOR',
		    ['SOURCE','SOURCES']
		   ],@_);

  my (@from,@where,@args,@group);
  $range_type ||= 'overlaps';

  my $feature_table         = $self->_feature_table;
  @from = "$feature_table as f";

  if (defined $name) {
    # hacky backward compatibility workaround
    undef $class if $class && $class eq 'Sequence';
    $name = "$class:$name" if defined $class && length $class > 0;
    # last argument is the join field
    my ($from,$where,$group,@a) = $self->_name_sql($name,$allow_aliases,'f.id');
    push @from,$from   if $from;
    push @where,$where if $where;
    push @group,$group if $group;
    push @args,@a;
  }

  if (defined $seq_id) {
    # last argument is the name of the features table
    my ($from,$where,$group,@a) = $self->_location_sql($seq_id,$start,$end,$range_type,$strand,'f');
    push @from,$from   if $from;
    push @where,$where if $where;
    push @group,$group if $group;
    push @args,@a;
  }

  if (defined($sources)) {
    my @sources = ref($sources) eq 'ARRAY' ? @{$sources} : ($sources);
    if (defined($types)) {
        my @types = ref($types) eq 'ARRAY' ? @{$types} : ($types);
        my @final_types;
        foreach my $type (@types) {
            # *** not sure what to do if user supplies both -source and -type
            #     where the type includes a source!
            if ($type =~ /:/) {
                push(@final_types, $type);
            }
            else {
                foreach my $source (@sources) {
                    push(@final_types, $type.':'.$source);
                }
            }
        }
        $types = \@final_types;
    }
    else {
        $types = [map { ':'.$_ } @sources];
    }
  }
  if (defined($types)) {
    # last argument is the name of the features table
    my ($from,$where,$group,@a) = $self->_types_sql($types,'f');
    push @from,$from   if $from;
    push @where,$where if $where;
    push @group,$group if $group;
    push @args,@a;
  }

  if (defined $attributes) {
    # last argument is the join field
    my ($from,$where,$group,@a) = $self->_attributes_sql($attributes,'f.id');
    push @from,$from    if $from;
    push @where,$where  if $where;
    push @group,$group  if $group;
    push @args,@a;
  }

  if (defined $fromtable) {
    # last argument is the join field
    my ($from,$where,$group,@a) = $self->_from_table_sql($fromtable,'f.id');
    push @from,$from    if $from;
    push @where,$where  if $where;
    push @group,$group  if $group;
    push @args,@a;
  }

  # if no other criteria are specified, then
  # only fetch indexed (i.e. top level objects)
  @where = 'indexed=1' unless @where;

  my $from  = join ', ',@from;
  my $where = join ' AND ',map {"($_)"} @where;
  my $group = join ', ',@group;
  $group    = "GROUP BY $group" if @group;

  my $query = <<END;
SELECT f.id,f.object,f.typeid,f.seqid,f.start,f.end,f.strand
  FROM $from
  WHERE $where
  $group
END

  $self->_print_query($query,@args) if DEBUG || $self->debug;

  my $sth = $self->_prepare($query) or $self->throw($self->dbh->errstr);
  $sth->execute(@args) or $self->throw($sth->errstr);
  return $iterator ? Bio::DB::SeqFeature::Store::DBI::Iterator->new($sth,$self) : $self->_sth2objs($sth);
}

sub _name_sql {
  my $self = shift;
  my ($name,$allow_aliases,$join) = @_;
  my $name_table   = $self->_name_table;

  my $from  = "$name_table as n";
  my ($match,$string) = $self->_match_sql($name);

  my $where = "n.id=$join AND lower(n.name) $match";
  $where   .= " AND n.display_name>0" unless $allow_aliases;
  return ($from,$where,'',$string);
}

sub _search_attributes {
  my $self = shift;
  my ($search_string,$attribute_names,$limit) = @_;
  my @words               = map {quotemeta($_)} split /\s+/,$search_string;
  my $name_table          = $self->_name_table;
  my $attribute_table     = $self->_attribute_table;
  my $attributelist_table = $self->_attributelist_table;
  my $type_table          = $self->_type_table;
  my $typelist_table      = $self->_typelist_table;

  my @tags    = @$attribute_names;
  my $tag_sql = join ' OR ',("al.tag=?") x @tags;

  my $perl_regexp = join '|',@words;

# This section describes each SQL routine that the Informix regexp DataBlade module creates.
# regexp_extract	Return a list of strings that match a regular expression from the source string.
# regexp_match	Return TRUE if a source string matches the regular expression.
# regexp_replace	Match a regular expression in a string and replace it with something else.
# regexp_split	Splits a string into substrings, using the regular expression as the delimiter.
# TraceSet_regexp	Enable tracing for regexp routines.

  #my $sql_regexp = join ' OR ', "regexp_match (a.attribute_value,?)"  x @words;
  my $sql_regexp = join ' OR ',("a.attribute_value == ? ")  x @words;
  #print STDERR "$sql_regexp\n";

  my $sql = "SELECT ";
  $sql .= "FIRST $limit " if defined $limit;
  $sql .= <<END;
  name,attribute_value,tl.tag,n.id
  FROM $name_table as n,$attribute_table as a,$attributelist_table as al,$type_table as t,$typelist_table as tl
  WHERE n.id=a.id
    AND al.id=a.attribute_id
    AND n.id=t.id
    AND t.typeid=tl.id
    AND n.display_name=1
    AND ($tag_sql)
    AND ($sql_regexp)
END
  $self->_print_query($sql,@tags,@words) if DEBUG || $self->debug;
  my $sth = $self->_prepare($sql);
  $sth->execute(@tags,@words) or $self->throw($sth->errstr);

  my @results;
  while (my($name,$value,$type,$id) = $sth->fetchrow_array) {
    my (@hits) = $value =~ /$perl_regexp/ig;
    my @words_in_row = split /\b/,$value;
    my $score   = int(@hits * 10);
    push @results,[$name,$value,$score,$type,$id];
  }
  $sth->finish;
  @results = sort {$b->[2]<=>$a->[2]} @results;
  return @results;
}

sub _match_sql {
  my $self = shift;
  my $name = shift;

  my ($match,$string);
  if ($name =~ /(?:^|[^\\])[*?]/) {
    $name =~ s/(^|[^\\])([%_])/$1\\$2/g;
    $name =~ s/(^|[^\\])\*/$1%/g;
    $name =~ s/(^|[^\\])\?/$1_/g;
    $match = "LIKE ?";
    $string  = $name;
  } else {
    $match = "= lower(?)";
    $string  = lc($name);
  }
  return ($match,$string);
}

sub _from_table_sql {
  my $self = shift;
  my ($from_table,$join) = @_;
  my $from  = "$from_table as ft";
  my $where = "ft.id=$join";
  return ($from,$where,'');
}

sub _attributes_sql {
  my $self = shift;
  my ($attributes,$join) = @_;

  my ($wf,@bind_args)       = $self->_make_attribute_where('a','al',$attributes);
  my ($group_by,@group_args)= $self->_make_attribute_group('a',$attributes);

  my $attribute_table       = $self->_attribute_table;
  my $attributelist_table   = $self->_attributelist_table;

  my $from = "$attribute_table as a , $attributelist_table as al";
 
 my $where = <<END;
  a.id=$join
  AND   a.attribute_id=al.id
  AND ($wf)
END

  my $group = $group_by;

  my @args  = (@bind_args,@group_args);
  return ($from,$where,$group,@args);
}

sub subfeature_types_are_indexed     { 1 }
sub subfeature_locations_are_indexed { 1 }

sub _types_sql {
  my $self  = shift;
  my ($types,$type_table) = @_;
  my ($primary_tag,$source_tag);

  my @types = ref $types eq 'ARRAY' ?  @$types : $types;

  my $typelist      = $self->_typelist_table;
  my $from = "$typelist AS tl";

  my (@matches,@args);

  for my $type (@types) {

    if (ref $type && $type->isa('Bio::DB::GFF::Typename')) {
      $primary_tag = $type->method;
      $source_tag  = $type->source;
    } else {
      ($primary_tag,$source_tag) = split ':',$type,2;
    }

    if (defined $source_tag) {
      if (length($primary_tag)) {
        push @matches,"lower(tl.tag)=lower(?)";
        push @args,"$primary_tag:$source_tag";
      }
      else {
        push @matches,"tl.tag LIKE ?";
        push @args,"%:$source_tag";
      }
    } else {
      push @matches,"tl.tag LIKE ?";
      push @args,"$primary_tag:%";
    }
  }
  my $matches = join ' OR ',@matches;

  my $where = <<END;
   tl.id=$type_table.typeid
   AND   ($matches)
END

  return ($from,$where,'',@args);
}

sub _location_sql {
  my $self = shift;
  my ($seq_id,$start,$end,$range_type,$strand,$location) = @_;

  # the additional join on the location_list table badly impacts performance
  # so we build a copy of the table in memory
  my $seqid = $self->_locationid($seq_id) || 0; # zero is an invalid primary ID, so will return empty
#TEC zero is what we insert to initiate a new serial id. does this use conflict?
	
  $start = MIN_INT unless defined $start;
  $end   = MAX_INT unless defined $end;

  my ($bin_where,@bin_args) = $self->bin_where($start,$end,$location);

  my ($range,@range_args);
  if ($range_type eq 'overlaps') {
    $range = "$location.end>=? AND $location.start<=? AND ($bin_where)";
    @range_args = ($start,$end,@bin_args);
  } elsif ($range_type eq 'contains') {
    $range = "$location.start>=? AND $location.end<=? AND ($bin_where)";
    @range_args = ($start,$end,@bin_args);
  } elsif ($range_type eq 'contained_in') {
    $range = "$location.start<=? AND $location.end>=?";
    @range_args = ($start,$end);
  } else {
    $self->throw("range_type must be one of 'overlaps', 'contains' or 'contained_in'");
  }

  if (defined $strand) {
    $range .= " AND strand=?";
    push @range_args,$strand;
  }

  my $where = <<END;
   $location.seqid=?
   AND   $range
END

  my $from  = '';
  my $group = '';

  my @args  = ($seqid,@range_args);
  return ($from,$where,$group,@args);
}

###
# force reindexing
#
sub reindex {
  my $self = shift;
  my $from_update_table = shift;  # if present, will take ids from "update_table"

  my $dbh  = $self->dbh;
  my $count = 0;
  my $now;

  # try to bring in highres time() function
  eval "require Time::HiRes";

  my $last_time = $self->time();

  # tell _delete_index() not to bother removing the index rows corresponding
  # to each individual feature
  local $self->{reindexing} = 1;

  $dbh->begin_work;
  eval {
    my $update = $from_update_table;
    for my $table ($self->index_tables) {
      my $query = $from_update_table ? "DELETE $table FROM $table,$update WHERE $table.id=$update.id"
	                             : "DELETE FROM $table";
      $dbh->do($query);
      $dbh->do("ALTER TABLE $table DISABLE KEYS");
    }
    my $iterator = $self->get_seq_stream(-from_table=>$from_update_table ? $update : undef);
    while (my $f = $iterator->next_seq) {
      if (++$count %1000 == 0) {
	$now = $self->time();
	my $elapsed = sprintf(" in %5.2fs",$now - $last_time);
	$last_time = $now;
	print STDERR "$count features indexed$elapsed...",' 'x60;
	print STDERR -t STDOUT && !$ENV{EMACS} ? "\r" : "\n";
      }
      $self->_update_indexes($f);
    }
  };
  for my $table ($self->index_tables) {
    $dbh->do("ALTER TABLE $table ENABLE KEYS");
  }
  if (@_) {
    warn "Couldn't complete transaction: $@";
    $dbh->rollback;
    return;
  } else {
    $dbh->commit;
    return 1;
  }
}

sub optimize {
  my $self = shift;
  $self->dbh->do("UPDATE STATISTICS HIGH FOR TABLE $_") foreach $self->index_tables;
}

sub all_tables {
  my $self = shift;
  my @index_tables = $self->index_tables;
  my $feature_table = $self->_feature_table;
  return ($feature_table,@index_tables);
}

sub index_tables {
  my $self = shift;
  return map {$self->_qualify($_)} qw(name attribute parent2child)
}

sub _firstid {
  my $self = shift;
  my $features = $self->_feature_table;
  my $query = <<END;
SELECT min(id) FROM $features
END
  my $sth=$self->_prepare($query);
  $sth->execute();
  my ($first) = $sth->fetchrow_array;
  $sth->finish;
  $first;
}

sub _nextid {
  my $self = shift;
  my $lastkey = shift;
  my $features = $self->_feature_table;
  my $query = <<END;
SELECT min(id) FROM $features WHERE id>?
END
  my $sth=$self->_prepare($query);
  $sth->execute($lastkey);
  my ($next) = $sth->fetchrow_array;
  $sth->finish;
  $next;
}

sub _existsid {
  my $self = shift;
  my $key  = shift;
  my $features = $self->_feature_table;
  my $query = <<END;
SELECT count(*) FROM $features WHERE id=?
END
  my $sth=$self->_prepare($query);
  $sth->execute($key);
  my ($count) = $sth->fetchrow_array;
  $sth->finish;
  $count > 0;
}

sub _deleteid {
  my $self = shift;
  my $key  = shift;
  my $dbh = $self->dbh;
  my $child_table = $self->_parent2child_table;
  my $query = "SELECT child FROM $child_table WHERE id=?";
  my $sth=$self->_prepare($query);
  $sth->execute($key);
  my $success = 0;
  while (my ($cid) = $sth->fetchrow_array) {
    # Backcheck looking for multiple parents, delete only if one is present. I'm
    # sure there is a nice way to left join the parent2child table onto itself
    # to get this in one query above, just haven't worked it out yet...
    my $sth2 = $self->_prepare("SELECT count(id) FROM $child_table WHERE child=?");
    $sth2->execute($cid);
    my ($count) = $sth2->fetchrow_array;
    if ($count == 1) {
        $self->_deleteid($cid) || warn "An error occurred while removing subfeature id=$cid. Perhaps it was previously deleted?\n";
    }
  }
  for my $table ($self->all_tables) {
    $success += $dbh->do("DELETE FROM $table WHERE id=$key") || 0;
  }
  return $success;
}

sub _clearall {
  my $self = shift;
  my $dbh = $self->dbh;
  for my $table ($self->all_tables) {
    $dbh->do("DELETE FROM $table");
  }
}

sub _featurecount {
  my $self = shift;
  my $dbh = $self->dbh;
  my $features = $self->_feature_table;
  my $query = <<END;
SELECT count(*) FROM $features
END
  my $sth=$self->_prepare($query);
  $sth->execute();
  my ($count) = $sth->fetchrow_array;
  $sth->finish;
  $count;
}

sub _seq_ids {
  my $self = shift;
  my $dbh = $self->dbh;
  my $location = $self->_locationlist_table;
  my $sth = $self->_prepare("SELECT DISTINCT seqname FROM $location");
  $sth->execute() or $self->throw($sth->errstr);
  my @result;
  while (my ($id) = $sth->fetchrow_array) {
    push @result,$id;
  }
  return @result;
}

sub setting {
  my $self = shift;
  my ($variable_name,$value) = @_;
  my $meta  = $self->_meta_table;

  if (defined $value && $self->writeable) { 
    eval {
		local $self->dbh->{PrintError} = 0;
		local $self->dbh->{RaiseError} = 0;	
	    $self->dbh->do("delete from $meta where name = '$variable_name' and value = '$value'");
     };
    my $query = "INSERT INTO $meta (name,value) VALUES (?,?)";
		my $sth = $self->_prepare($query);
		$sth->execute($variable_name,$value) or $self->throw($sth->errstr);
		$sth->finish;
		$self->{settings_cache}{$variable_name} = $value;
  }
  else {
    return $self->{settings_cache}{$variable_name} if exists $self->{settings_cache}{$variable_name};
    my $query = <<END;
SELECT value FROM $meta as m WHERE m.name=?
END
    my $sth = $self->_prepare($query);
    $sth->execute($variable_name) or $self->throw($sth->errstr);
    my ($value) = $sth->fetchrow_array;
    $sth->finish;
    return $self->{settings_cache}{$variable_name} = $value;
  }
}

###
# Replace Bio::SeqFeatureI into database.
#
sub replace {
  my $self       = shift;
  my $object     = shift;
  my $index_flag = shift || undef;

  # ?? shouldn't need to do this
  # $self->_load_class($object);
  my $id = $object->primary_id;  
  my $features = $self->_feature_table;
  $id = "SCALAR" eq ref $id ? $id : 0; #TEC  


  my $sth = $self->_prepare(<<END);
INSERT INTO $features (id,object,indexed,seqid,start,end,strand,tier,bin,typeid) 
	VALUES (?,filetoblob(?,'SERVER'),?,?,?,?,?,?,?,?)
END

  my @location = $index_flag ? $self->_get_location_and_bin($object) : (undef)x6;

  my $primary_tag = $object->primary_tag;
  my $source_tag  = $object->source_tag || '';
  $primary_tag    .= ":$source_tag";
  my $typeid   = $self->_typeid($primary_tag,1);

  my $frozen =  $self->freeze($object); 
  #my $frozen = $self->no_blobs() ? 0 : $self->freeze($object);
  ### TEC:  "no_blobs" only applies to mysql according to 
  ### http://search.cpan.org/~cjfields/BioPerl-1.6.0/Bio/DB/SeqFeature/Store.pm#no_blobs
  ###
  ### informix only loads blobs from the file system so we have to write it out 
  open BLOB, '>/tmp/gbblob' or die "Cant open /tmp/gbblob to load frozen features\n";
  binmode(BLOB);
  syswrite BLOB, $frozen, length $frozen;
  close BLOB;
  $sth->execute($id, '/tmp/gbblob', $index_flag||0, @location, $typeid) or $self->throw($sth->errstr);

  my $dbh = $self->dbh;
  $object->primary_id($dbh->{ix_serial}) unless  $id > 0;

  $self->flag_for_indexing($dbh->{ix_serial}) if $self->{bulk_update_in_progress};
  system 'rm /tmp/gbblob';
}

###
# Insert one Bio::SeqFeatureI into database. primary_id must be undef
#
sub insert {
  my $self = shift;
  my $object = shift;
  my $index_flag = shift || 0;

  $self->_load_class($object);
  defined $object->primary_id and $self->throw("$object already has a primary id");

  my $features = $self->_feature_table;
  my $frozen =  $self->freeze($object); 

  ### informix only loads blobs from the file system so we have to write it out 
  open BLOB, '>/tmp/gbblob' or die "Cant open /tmp/gbblob to load frozen features\n";
  binmode(BLOB);
  syswrite BLOB, $frozen, length $frozen;
  close BLOB;
  
  my $sth = $self->_prepare(<<END);
INSERT INTO $features (id,object,indexed) VALUES (?,filetoblob(?,'SERVER'),?)
END
  $sth->execute(0,$frozen,$index_flag) or $self->throw($sth->errstr);
  my $dbh = $self->dbh;
  $object->primary_id($dbh->{ix_serial});
  $self->flag_for_indexing($dbh->{ix_serial}) if $self->{bulk_update_in_progress};
}

=head2 types

 Title   : types
 Usage   : @type_list = $db->types
 Function: Get all the types in the database
 Returns : array of Bio::DB::GFF::Typename objects
 Args    : none
 Status  : public

=cut

sub types {
    my $self = shift;
    eval "require Bio::DB::GFF::Typename"
	unless Bio::DB::GFF::Typename->can('new');
    my $typelist_table      = $self->_typelist_table;
    my $sql = <<END;
SELECT tag from $typelist_table
END
;
    $self->_print_query($sql) if DEBUG || $self->debug;
    my $sth = $self->_prepare($sql);
    $sth->execute() or $self->throw($sth->errstr);

    my @results;
    while (my($tag) = $sth->fetchrow_array) {
	push @results,Bio::DB::GFF::Typename->new($tag);
    }
    $sth->finish;
    return @results;
}

###
# Insert a bit of DNA or protein into the database
#
sub _insert_sequence {
  my $self = shift;
  my ($seqid,$seq,$offset) = @_;
  my $id = $self->_locationid($seqid);
  open BLOB, '>/tmp/gbseq' or die "Cant open /tmp/gbseq to load sequence\n";
  binmode(BLOB);
  syswrite BLOB, $seq, length $seq;
  close BLOB;
  my $seqtable = $self->_sequence_table;
  my $sth = $self->_prepare(<<END);
INSERT INTO $seqtable (id,offset,sequence) VALUES (?,?,filetoclob(?,'SERVER'))
END
  $sth->execute($id,$offset,'/tmp/gbseq') or $self->throw($sth->errstr);
  system 'rm /tmp/gbseq';
}

###
# This subroutine flags the given primary ID for later reindexing
#
sub flag_for_indexing {
  my $self = shift;
  my $id   = shift;
  my $needs_updating = $self->_update_table;
  my $sth = $self->_prepare("INSERT INTO $needs_updating VALUES (?)");
  $sth->execute($id) or $self->throw($self->dbh->errstr);
}

###
# Update indexes for given object
#
sub _update_indexes {
  my $self = shift;
  my $obj  = shift;
  defined (my $id   = $obj->primary_id) or return;

  if ($self->{bulk_update_in_progress}) {
    $self->_dump_update_name_index($obj,$id);
    $self->_dump_update_attribute_index($obj,$id);
  } else {
    $self->_update_name_index($obj,$id);
    $self->_update_attribute_index($obj,$id);
  }
}

sub _update_name_index {
  my $self = shift;
  my ($obj,$id) = @_;
  my $name = $self->_name_table;
  my $primary_id = $obj->primary_id;

  $self->_delete_index($name,$id);
  my ($names,$aliases) = $self->feature_names($obj);

  my $sth = $self->_prepare("INSERT INTO $name (id,name,display_name) VALUES (?,?,?)");

  $sth->execute($id,$_,1) or $self->throw($sth->errstr)   foreach @$names;
  $sth->execute($id,$_,0) or $self->throw($sth->errstr) foreach @$aliases;
  $sth->finish;
}

sub _update_attribute_index {
  my $self = shift;
  my ($obj,$id) = @_;
  my $attribute = $self->_attribute_table;
  $self->_delete_index($attribute,$id);

  my $sth = $self->_prepare("INSERT INTO $attribute (id,attribute_id,attribute_value) VALUES (?,?,?)");
  for my $tag ($obj->get_all_tags) {
    my $tagid = $self->_attributeid($tag);
    for my $value ($obj->get_tag_values($tag)) {
      $sth->execute($id,$tagid,$value) or $self->throw($sth->errstr);
    }
  }
  $sth->finish;
}

sub _genericid {
  my $self = shift;
  my ($table,$namefield,$name,$add_if_missing) = @_;
  my $qualified_table = $self->_qualify($table);
  my $sth = $self->_prepare(<<END);
SELECT id FROM $qualified_table WHERE $namefield=?
END
  $sth->execute($name) or die $sth->errstr;
  my ($id) = $sth->fetchrow_array;
  $sth->finish;
  return $id if defined $id;
  return     unless $add_if_missing;

  $sth = $self->_prepare(<<END);
INSERT INTO $qualified_table ($namefield) VALUES (?)
END
  $sth->execute($name) or die $sth->errstr;
  my $dbh = $self->dbh;
  return $dbh->{ix_serial};
}

sub _typeid {
  shift->_genericid('typelist','tag',shift,1);
}
sub _locationid {
  shift->_genericid('locationlist','seqname',shift,1);
}
sub _attributeid {
  shift->_genericid('attributelist','tag',shift,1);
}

sub _get_location_and_bin {
  my $self = shift;
  my $feature = shift;
  my $seqid   = $self->_locationid($feature->seq_id);
  my $start   = $feature->start;
  my $end     = $feature->end;
  my $strand  = $feature->strand || 0;
  my ($tier,$bin) = $self->get_bin($start,$end);
  return ($seqid,$start,$end,$strand,$tier,$bin);
}

sub get_bin {
  my $self = shift;
  my ($start,$end) = @_;
  my $binsize = MIN_BIN;
  my ($bin_start,$bin_end,$tier);
  $tier = 0;
  while (1) {
    $bin_start = int $start/$binsize;
    $bin_end   = int $end/$binsize;
    last if $bin_start == $bin_end;
    $binsize *= 10;
    $tier++;
  }
  return ($tier,$bin_start);
}

sub bin_where {
  my $self = shift;
  my ($start,$end,$f) = @_;
  my (@bins,@args);

  my $tier         = 0;
  my $binsize      = MIN_BIN;
  while ($binsize <= MAX_BIN) {
    my $bin_start = int($start/$binsize);
    my $bin_end   = int($end/$binsize);
    push @bins,"($f.tier=? AND $f.bin >= ? AND $f.bin <= ?)";
    push @args,($tier,$bin_start,$bin_end);
    $binsize *= 10;
    $tier++;
  }
  my $query = join ("\n\t OR ",@bins);
  return wantarray ? ($query,@args) : substitute($query,@args);
}


sub _delete_index {
  my $self = shift;
  my ($table_name,$id) = @_;
  return if $self->{reindexing};
  my $sth = $self->_prepare("DELETE FROM $table_name WHERE id=?") or $self->throw($self->dbh->errstr);
  $sth->execute($id);
}

# given a statement handler that is expected to return rows of (id,object)
# unthaw each object and return a list of 'em
sub _sth2objs {
  my $self = shift;
  my $sth  = shift;
  my @result;
  while (my ($id,$o,$typeid,$seqid,$start,$end,$strand) = $sth->fetchrow_array) {
    my $obj;
    if ($o eq '0') {
        # rebuild a new feat object from the data stored in the db
        $obj = $self->_rebuild_obj($id,$typeid,$seqid,$start,$end,$strand);
    }
    else {
        $obj = $self->thaw($o,$id);
    }
    
    push @result,$obj;
  }
  $sth->finish;
  return @result;
}

# given a statement handler that is expected to return rows of (id,object)
# unthaw each object and return a list of 'em
sub _sth2obj {
  my $self = shift;
  my $sth  = shift;
  my ($id,$o,$typeid,$seqid,$start,$end,$strand) = $sth->fetchrow_array;
  return unless defined $o;
  my $obj;
  if ($o eq '0') {  # I don't understand why an object ever needs to be rebuilt!
    # rebuild a new feat object from the data stored in the db
    $obj = $self->_rebuild_obj($id,$typeid,$seqid,$start,$end,$strand);
  }
  else {
    $obj = $self->thaw($o,$id);
  }
  
  $obj;
}

sub _rebuild_obj {
    my ($self, $id, $typeid, $db_seqid, $start, $end, $strand) = @_;
    my ($type, $source, $seqid);
    
    # convert typeid to type and source
    if (exists $self->{_type_cache}->{$typeid}) {
        ($type, $source) = @{$self->{_type_cache}->{$typeid}};
    }
    else {
        my $sql = qq{ SELECT `tag` FROM typelist WHERE `id` = ? };
        my $sth = $self->_prepare($sql) or $self->throw($self->dbh->errstr);
        $sth->execute($typeid);
        my $result;
        $sth->bind_columns(\$result);
        while ($sth->fetch()) {
            # there should be only one row returned, but we ensure to get all rows
        }
        
        ($type, $source) = split(':', $result);
        $self->{_type_cache}->{$typeid} = [$type, $source];
    }
    
    # convert the db seqid to the sequence name
    if (exists $self->{_seqid_cache}->{$db_seqid}) {
        $seqid = $self->{_seqid_cache}->{$db_seqid};
    }
    else {
        my $sql = qq{ SELECT `seqname` FROM locationlist WHERE `id` = ? };
        my $sth = $self->_prepare($sql) or $self->throw($self->dbh->errstr);
        $sth->execute($db_seqid);
        $sth->bind_columns(\$seqid);
        while ($sth->fetch()) {
            # there should be only one row returned, but we ensure to get all rows
        }
        
        $self->{_seqid_cache}->{$db_seqid} = $seqid;
    }
    
    # get the names from name table?
    
    # get the attributes and store those in obj
    my $sql = qq{ SELECT attribute_id,attribute_value FROM attribute WHERE `id` = ? };
    my $sth = $self->_prepare($sql) or $self->throw($self->dbh->errstr);
    $sth->execute($id);
    my ($attribute_id, $attribute_value);
    $sth->bind_columns(\($attribute_id, $attribute_value));
    my %attribs;
    while ($sth->fetch()) {
        # convert the attribute_id to its real name
        my $attribute;
        if (exists $self->{_attribute_cache}->{$attribute_id}) {
            $attribute = $self->{_attribute_cache}->{$attribute_id};
        }
        else {
            my $sql = qq{ SELECT `tag` FROM attributelist WHERE `id` = ? };
            my $sth2 = $self->_prepare($sql) or $self->throw($self->dbh->errstr);
            $sth2->execute($attribute_id);
            $sth2->bind_columns(\$attribute);
            while ($sth2->fetch()) {
                # there should be only one row returned, but we ensure to get all rows
            }
            
            $self->{_attribute_cache}->{$attribute_id} = $attribute;
        }
        
        if ($source && $attribute eq 'source' && $attribute_value eq $source) {
            next;
        }
        
        $attribs{$attribute} = $attribute_value;
    }
    
    my $obj = Bio::SeqFeature::Lite->new(-primary_id => $id,
                                         $type ? (-type => $type) : (),
                                         $source ? (-source => $source) : (),
                                         $seqid ? (-seq_id => $seqid) : (),
                                         defined $start ? (-start => $start) : (),
                                         defined $end ? (-end => $end) : (),
                                         defined $strand ? (-strand => $strand) : (),
                                         keys %attribs ? (-attributes => \%attribs) : ());
    
    return $obj;
}

sub _prepare {
  my $self = shift;
  my $query = shift;
  my $dbh   = $self->dbh;
  #print "$query\n";
  my $sth   = $dbh->prepare_cached($query, {}, 3) or $self->throw($dbh->errstr);
  $sth;
}


####################################################################################################
# SQL Fragment generators
####################################################################################################

sub _feature_table       {  shift->_qualify('feature')  }
sub _location_table      {  shift->_qualify('location') }
sub _locationlist_table  {  shift->_qualify('locationlist') }
sub _type_table          {  shift->_qualify('feature')     }
sub _typelist_table      {  shift->_qualify('typelist') }
sub _name_table          {  shift->_qualify('name')     }
sub _attribute_table     {  shift->_qualify('attribute')}
sub _attributelist_table {  shift->_qualify('attributelist')}
sub _parent2child_table  {  shift->_qualify('parent2child')}
sub _meta_table          {  shift->_qualify('meta')}
sub _update_table        {  shift->_qualify('update_table')}
sub _sequence_table      {  shift->_qualify('sequence')}

sub _make_attribute_where {
  my $self                     = shift;
  my ($attributetable,$attributenametable,$attributes) = @_;
  my @args;
  my @sql;
  my $dbh = $self->dbh;
  foreach (keys %$attributes) {
    my @match_values;
    my @values = ref($attributes->{$_}) && ref($attributes->{$_}) eq 'ARRAY' ? @{$attributes->{$_}} : $attributes->{$_};
    foreach (@values) {  # convert * into % for wildcard matches
      s/\*/%/g;
    }
    my $match  = join ' OR ',map {
      /%/ ? "$attributetable.attribute_value LIKE ?"
	  : "$attributetable.attribute_value=?"
    } @values;
    push @sql,"($attributenametable.tag=? AND ($match))";
    push @args,($_,@values);
  }
  return (join(' OR ',@sql),@args);
}

sub _make_attribute_group {
  my $self                     = shift;
  my ($table_name,$attributes) = @_;
  my $key_count = keys %$attributes or return;
  return "f.id,f.object,f.typeid,f.seqid,f.start,f.end,f.strand HAVING count(f.id)>?",$key_count-1;
}

sub _print_query {
  my $self = shift;
  my ($query,@args) = @_;
  while ($query =~ /\?/) {
    my $arg = $self->dbh->quote(shift @args);
    $query =~ s/\?/$arg/;
  }
  warn $query,"\n";
}

###
# special-purpose store for bulk loading feature table 
# - write to a file rather than to the db
# is read into db @ line ~650
sub _dump_store {
  my $self    = shift;
  my $indexed = shift;

  my $count = 0;
  my $store_fh = $self->dump_filehandle('feature'); 
  my $blob_fh = $self->dump_filehandle('feature_blob'); # tell() broken
  my $blob_path =  "$self->{dumpdir}/feature_blob.$$";
     $blob_fh = FileHandle->new (">>$blob_path");	    # tell() works
  binmode $blob_fh;
  my ($frozen, $blobref); 
  my $dbh      = $self->dbh;
 
  my $autoindex = $self->autoindex;

  for my $obj (@_) {
    my $id       = $self->next_id;
    my ($seqid,$start,$end,$strand,$tier,$bin) = $indexed ? $self->_get_location_and_bin($obj) : (undef)x6;
    my $primary_tag = $obj->primary_tag;
    my $source_tag  = $obj->source_tag || '';
    $primary_tag    .= ":$source_tag";
    my $typeid   = $self->_typeid($primary_tag,1);

    #print $store_fh join("\t",$id,$typeid,$seqid,$start,$end,$strand,$tier,$bin,$indexed,$dbh->quote($self->freeze($obj))),"\n";

	# TEC need to split blob into its own file and keep offset & length (in hex)
	$frozen =$self->freeze($obj);
	$blobref = join(',',sprintf("%lx",$blob_fh->tell),sprintf("%lx",length($frozen)),$blob_path);
	print $store_fh join("\t",$id,$typeid,$seqid,$start,$end,$strand,$tier,$bin,$indexed,$blobref,""),"\n";
	my $result = syswrite $blob_fh, $frozen, length($frozen);
	#if ($result != length($frozen)) warn  "what is blob is being written?\n";
	
    $obj->primary_id($id);
    $self->_update_indexes($obj) if $indexed && $autoindex;
    $count++;
  }

  # remember whether we are have ever stored a non-indexed feature
  unless ($indexed or $self->{indexed_flag}++) {
    $self->subfeatures_are_indexed(0);
  }
  $blob_fh->close;
  $count;
}

sub _dump_add_SeqFeature {
  my $self     = shift;
  my $parent   = shift;
  my @children = @_;

  my $dbh = $self->dbh;
  my $fh = $self->dump_filehandle('parent2child');
  my $parent_id = (ref $parent ? $parent->primary_id : $parent)
    or $self->throw("$parent should have a primary_id");
  my $count = 0;

  for my $child_id (@children) {
    print $fh join("\t",$parent_id,$child_id),"\n";
    $count++;
  }
  $count;
}

sub _dump_update_name_index {
  my $self = shift;
  my ($obj,$id) = @_;
  my $fh      = $self->dump_filehandle('name');
  my $dbh     = $self->dbh;
  my ($names,$aliases) = $self->feature_names($obj);
  print $fh join("\t",$id,$dbh->quote($_),1),"\n" foreach @$names;
  print $fh join("\t",$id,$dbh->quote($_),0),"\n" foreach @$aliases;
}

sub _dump_update_attribute_index {
  my $self = shift;
  my ($obj,$id) = @_;
  my $fh        = $self->dump_filehandle('attribute');
  my $dbh       = $self->dbh;
  for my $tag ($obj->all_tags) {
    my $tagid = $self->_attributeid($tag);
    for my $value ($obj->each_tag_value($tag)) {
      print $fh join("\t",$id,$tagid,$dbh->quote($value)),"\n";
    }
  }
}

sub time {
  return Time::HiRes::time() if Time::HiRes->can('time');
  return time();
}

sub DESTROY {
  my $self = shift;
  if ($self->{bulk_update_in_progress}) {  # be sure to remove temp files
    for my $table ('feature',$self->index_tables) {
      my $path = $self->dump_path($table);
      unlink $path;
    }
  }
}

1;
