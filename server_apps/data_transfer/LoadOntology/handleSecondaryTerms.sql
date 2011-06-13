--secondary terms.

create temp table sec_dups
  (
    prim_id varchar(50),
    sec_id varchar(50)
  );

load from term_secondary.unl
  insert into sec_dups;

create temp table sec_oks
  (
    prim_id varchar(50),
    sec_id varchar(50),
    prim_zdb_id varchar(50),
    sec_zdb_id varchar(50)
  );

--insert only the distinct secondary terms

insert into sec_oks (sec_id, prim_id)
  select distinct sec_id, prim_id
    from sec_dups ;

update sec_oks
  set prim_zdb_id = (select term_zdb_id
      		       from term
		       where term_ont_id = prim_id);

update sec_oks
  set sec_zdb_id = (select term_zdb_id
      		       from term
		       where term_ont_id = sec_id);

unload to 'debug'
  select * from sec_oks;

create temp table sec_unload
  (
    prim_id varchar(50),
    sec_id varchar(50)
  );

--update the secondary terms in ZFIN

!echo "here is the secondary update for  terms" ;

!echo "Number of secondary (merged) terms in obo file";

unload to debug
    select count(*) from sec_oks;

!echo "Secondary (merged) terms in obo file";

unload to debug
    select * from sec_oks;

-- delete existing secondary from temp table as they need not be dealt with any more.

unload to debug
 select *
		from term
		      where term_is_secondary = 't' AND term_ontology= 'quality';

!echo "Terms that were merged (became secondary): ";

update term
  set term_is_secondary = 't'
  where exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id) ;

--unload to term_no_longer_secondary.txt
--  select term_name, term_ont_id, term_zdb_id
--    from term
--    where term_is_secondary = 't'
--    and term_is_obsolete = 'f'
--    and not exists (Select 'x'
--		  from sec_oks
--		  where term_ont_id = sec_id);

--set these back to primary for now

--update term
--  set term_is_secondary = 'f'
--  where not exists (Select 'x'
--		  from sec_oks
--		  where term_ont_id = sec_id)
--  and term_is_secondary = 't'

