--secondary terms.

create temp table sec_dups
  (
    prim_id varchar(50),
    sec_id varchar(250)
  );

load from term_secondary.unl
  insert into sec_dups;

create temp table sec_oks
  (
    prim_id varchar(50),
    sec_id varchar(250),
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

delete from sec_oks where prim_zdb_id = '' or sec_zdb_id = '' or prim_zdb_id is null or sec_zdb_id is null;

unload to 'debug'
  select * from sec_oks;

unload to 'debug'
  select * from sec_oks where sec_zdb_id is not null;

create temp table sec_unload
  (
    prim_id varchar(50),
    sec_id varchar(250)
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

unload to 'sec_unload.unl'
select term_ont_id from term
where exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id
		  and term_is_secondary = 'f');

update term
  set term_is_secondary = 't'
  where exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id) ;

select sec_zdb_id, prim_zdb_id from sec_oks  where not exists
    (select 'x' from zdb_replaced_data as d where d.zrepld_new_zdb_id =prim_zdb_id AND d.zrepld_old_zdb_id = sec_zdb_id);

-- create zdb_replaced_data entries for secondary terms
insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
select sec_zdb_id, prim_zdb_id from sec_oks where not exists
    (select 'x' from zdb_replaced_data as d where d.zrepld_new_zdb_id =prim_zdb_id AND d.zrepld_old_zdb_id = sec_zdb_id);

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

