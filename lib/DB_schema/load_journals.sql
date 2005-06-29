begin work ;

execute procedure set_session_params();

alter table journal
 modify (jrnl_name varchar(255) not null constraint jrnl_name_not_null); 

set constraints all deferred ;

create temp table tmp_jrnl_list (title varchar(255),
	NLM_abbrev varchar(255),
	ISSN varchar(255),
	eISSN varchar(255),
	Publisher varchar(255),
	UniqueID  varchar(50),
	LatestIssue varchar(50),
	Earliest	varchar(50),
	free_access     varchar(50),
	delayed_release varchar(50),
	open_access	varchar(50),
	publink_access	varchar(50),
	journal_url 	varchar(255)
	)
with no log ;

load from pubmed_journal_free_access.txt
  insert into tmp_jrnl_list ;

alter table journal 
  add jrnl_publisher varchar(100) ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'PLoS Biology',
	'PLoS Biol.',
	't', 'PLoS'
     from single ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'PLoS Medicine',
	'PLoS Medicine',
	't', 'PLoS'
     from single ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'PLoS Computational Biology',
	'PLoS Computational Biology',
	't', 'PLoS'
     from single ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'PLoS Genetics',
	'PLoS Genetics',
	't', 'PLoS'
     from single ;


insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'PLoS Pathogens',
	'PLoS Pathogens',
	't', 'PLoS'
     from single ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Development',
	'Development',
	't','Company Of Biologists Limited'
     from single ;


insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Developmental dynamics',
	'Dev. Dyn.',
	't','Wiley-Liss'
     from single ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Developmental biology',
	'Dev. Biol.',
	'f','Academic Press'
     from single ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Genes and Development',
	'Genes Dev.',
	'f','Cold Spring Harbor Laboratory Press'
     from single ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Gene Expression Patterns : GEP.',
	'Gene Expr. Patterns',
	'f','Elsevier'
     from single ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Mechanisms of Development',
	'Mech. Dev.',
	'f','Elsevier'
     from single ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Nature',
	'Nature',
	'f','Nature Publishing Group'
     from single ;


insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Neuron',
	'Neuron',
	'f','Cell Press'
     from single ;


insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Proceedings of the National Academy of Sciences of the United States of America.',
	'Proc. Natl. Acad. Sci. U.S.A.',
	'f',''
     from single ;


insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice, 
			jrnl_publisher
	)
  select get_id('JRNL'),
	'Science',
	'Science',
	'f','American Assn. for the Advancement of Science'
     from single ;

insert into zdb_active_source
  select jrnl_zdb_id 
    from journal ;
 
update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'Development')
  where source like 'Development %' ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'PLoS Biol.')
  where source like 'PLoS Biol.%' ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'PLoS Biol.')
  where source like 'PLoS Biol.%' ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'Gene Expr. Patterns')
  where source like 'Gene Expr. Patterns %' ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'Genes Dev.')
  where source like 'Genes Dev. %' ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'Dev. Dyn.')
  where source like 'Dev. Dyn. %' ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'Dev. Biol.')
  where source like 'Dev. Biol. %' ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'Mech. Dev.')
  where source like 'Mech. Dev. %' ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'Nature')
  where source like 'Nature %' 
  and source not like 'Nature Gen%';

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'Neuron')
  where source like 'Neuron %' ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
                           where jrnl_abbrev = 'Proc. Natl. Acad. Sci. U.S.A.')
  where source like 'Proc. Natl. Acad. Sci. U.S.A. %' ;

insert into journal (jrnl_zdb_id, 
	jrnl_name,
	jrnl_abbrev,
	jrnl_is_nice,
	jrnl_publisher)
select get_id('JRNL'), 
	title, 
	trim(trailing ' ' from NLM_abbrev),
	'f',
	Publisher
  from tmp_jrnl_list ;

update journal
  set jrnl_is_nice = 't'
  where jrnl_abbrev like 'BMC %' ;

insert into zdb_active_source
  select jrnl_zdb_id 
    from journal 
    where jrnl_zdb_id not in (select zactvs_zdb_id 
				from zdb_active_source);

set constraints all immediate ;

!echo DELETE journal abbrevs

delete from journal
 where jrnl_abbrev = "PLoS Med";

delete from journal
  where jrnl_abbrev = "PLoS Biol" ;

update publication
  set pub_can_show_images = (Select jrnl_is_nice
				from journal
				where jrnl_zdb_id = pub_jrnl_zdb_id) 
  where pub_jrnl_zdb_id is not null ;

delete from journal where jrnl_name = 'Journal title' ;

select count(*), jrnl_name, jrnl_abbrev
  from journal
  group by jrnl_name, jrnl_abbrev
  having count(*) > 1 ;

create temp table tmp_updated_jrnl (jrnl_old_abbrev varchar(50), 
					jrnl_new_abbrev varchar(50))
with no log ;

load from new_abbrev
  insert into tmp_updated_jrnl ;

!echo NEW count

select first 10 jrnl_abbrev, jrnl_name from journal ;

select * from journal where jrnl_name like "%PLoS Biology%" ;

select * from journal where jrnl_abbrev like 'NLM%' ;

select count(*) from journal;

set constraints all immediate ;

alter table publication
  add (pub_volume varchar(15)) ;

alter table publication
  add (pub_pages varchar(15));

alter table publication
  add (pub_jrnl_abbrev varchar(255));

alter table journal
  drop jrnl_acknowledgment ;


commit work ;
--rollback work ;