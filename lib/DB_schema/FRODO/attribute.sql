begin work ;

update statistics for procedure ;

set constraints all deferred ;

update attribution_type
  set attype_type = 'feature type'
  where attype_type = 'discovery' ;

update record_attribution
  set recattrib_source_type = 'feature type'
  where recattrib_source_type = 'discovery';

!echo "update cloned gene to standard attributions" ;

update record_Attribution
  set recattrib_sourcE_type = 'standard'
  where recattrib_source_type = 'cloned gene'
  and recattrib_data_zdb_id != 'ZDB-LOCUS-990215-497' ;

--don't update this loci, as it already has a gene attribution 
--to the paper that also did cloned_gene attribution.

--may need to add this back in when curators see, but for now 
--erik says these are pesky as many people clone the gene
--and its hard to determine who did it first, etc...

!echo "delete recattribs with cloned_gene -- should be zero" ;

delete from record_attribution
  where recattrib_source_type = 'cloned gene' ;

delete from linkage_member
  where lnkgmem_member_zdb_id = 'ZDB-LOCUS-030115-1' 
  and lnkgmem_linkage_zdb_id = 'ZDB-LINK-030521-39';

update linkage_member
  set lnkgmem_member_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = 
					lnkgmem_member_zdb_id)
  where exists (select 'x' from 
			zdb_replaced_data
			where lnkgmem_member_zdb_id = 	
			  zrepld_old_zdb_id)
  and lnkgmem_member_zdb_id like 'ZDB-LOCUS-%';
  
--In each case, the Df locus can be made into multiple loci. (I think  
--they each started out as a "normal" mutant locus with multiple  
--alleles, then they were characterized as deficiencies, but were never  
--separated into different loci in ZFIN.)

--Also check out the "valentino" locus ZDB-LOCUS-990215-631 and the  
--note in FB 856 on 1/6/2006 -- b361 and b475 should be associated with  
--one unique Df locus each, and b337 left alone in the valentino locus.

--Thanks,
--Erik


--primer set from fish to genotype.

alter table primer_set
 drop constraint primerset_strain_id_foreign_key ;


create temp table tmp_dup_Attribs (data_id varchar(50), 
					source_id varchar(50), 
					source_type varchar(50),
					new_id varchar(50),
					old_id varchar(50))
with no log ;

--give alterations their propper attributions

!echo " add attribution to alleles where fish are attributed" ;

insert into record_attribution (recattrib_data_zdb_id,
		recattrib_source_zdb_id,
		recattrib_source_type)
  select distinct alteration.zdb_id, b.recattrib_source_zdb_id,
		b.recattrib_source_type
	from fish, alteration, record_Attribution b
        where b.recattrib_data_zdb_id = fish.zdb_id
	and fish.allele = alteration.allele
	and not exists (select 'x'
			  from alteration, record_attribution c
			  where c.recattrib_data_zdb_id = alteration.zdb_id
			  and c.recattrib_source_zdb_id = 
				b.recattrib_source_zdb_id
			  and c.recattrib_source_type = 
				b.recattrib_source_type) ;
--        and not exists (select 'x'
--			  from zdb_replaced_data
--			  where alteration.zdb_id = zrepld_old_zdb_id);

!echo "add attribution to fish where alleles are attributed" ;

insert into record_attribution (recattrib_data_zdb_id,
		recattrib_source_zdb_id, recattrib_source_type)
  select distinct fish.zdb_id, b.recattrib_source_zdb_id, 
		b.recattrib_source_type
	from fish, alteration, record_Attribution b
        where b.recattrib_data_zdb_id = alteration.zdb_id
	and fish.allele = alteration.allele
	and not exists (select 'x'
			  from fish, record_attribution c
			  where c.recattrib_data_zdb_id = fish.zdb_id
			  and c.recattrib_source_zdb_id = 
				b.recattrib_source_zdb_id
			  and c.recattrib_source_type =
				b.recattrib_source_type) ;
--        and not exists (select 'x'
--			  from zdb_replaced_data
--			  where alteration.zdb_id = zrepld_old_zdb_id);

!echo "add attribution to loci where fish are attributed" ;

insert into record_attribution (recattrib_data_zdb_id,
		recattrib_source_zdb_id, recattrib_source_type)
  select distinct locus.zdb_id, b.recattrib_source_zdb_id, 
		b.recattrib_source_type
	from fish, locus, record_Attribution b
        where b.recattrib_data_zdb_id = fish.zdb_id
	and fish.locus = locus.zdb_id
	and not exists (select 'x'
			  from record_attribution c
			  where c.recattrib_data_zdb_id = locus.zdb_id
			  and c.recattrib_source_zdb_id = 
				b.recattrib_source_zdb_id
			  and c.recattrib_source_type = 
				b.recattrib_source_type) 
--        and not exists (select 'x'
--			  from zdb_replaced_data
--			  where alteration.zdb_id = zrepld_old_zdb_id)
        and locus_name like 'Tg%';

!echo "count dup attributes" ;

select count(*), recattrib_data_zdb_id, recattrib_source_zdb_id,
  recattrib_source_type
  from record_attribution
  group by recattrib_data_zdb_id, recattrib_source_zdb_id,
  recattrib_source_type
  having count(*)>1;

!echo "insert into dup attribs" ;

insert into tmp_dup_attribs (data_id, source_id, source_type, new_id, old_id )
 select a.recattrib_data_zdb_id, 
	 a.recattrib_source_zdb_id, a.recattrib_source_type,
	 zrepld_new_zdb_id, zrepld_old_zdb_id
            from record_attribution a, zdb_replaced_data
             where zrepld_old_zdb_id = recattrib_Data_zdb_id 
             and exists (select 'x' 
		          from record_attribution b
	 	         where zrepld_new_zdb_id = b.recattrib_Data_zdb_id
 		          and a.recattrib_source_zdb_id = 
				b.recattrib_source_zdb_id
			  and a.recattrib_source_type = 
				b.recattrib_source_type);

--delete the records that will become dups with addition of locus ids.
--delete zdb_repld_old_id, source with new_id source

!echo "delete the dups from recattrib" ;

delete from record_attribution 
  where exists (select 'x'
		from tmp_dup_attribs
		where source_id = recattrib_source_zdb_id
		and data_id = recattrib_data_zdb_id
		and source_type = recattrib_source_type);

--select count(*), recattrib_data_zdb_id, recattrib_source_zdb_id,
--  recattrib_source_type
--  from record_attribution
--  group by recattrib_data_zdb_id, recattrib_source_zdb_id,
--  recattrib_source_type
--  having count(*)>1;

--select count(*) as counter, zrepld_old_zdb_id as old_id
--  from zdb_replaced_data
--  where zrepld_old_zdb_id like 'ZDB-LOCUS-%'
--  group by zrepld_old_zdb_id
--  having count(*) > 1 
-- into temp tmp_need_two;

--select zrepld_new_zdb_id as data_id, recattrib_source_zdb_id as source_id,
--	recattrib_source_type as source_type
-- from record_Attribution, zdb_replaced_data, tmp_need_two
--    where zrepld_old_zdb_id = recattrib_data_zdb_id
--    and old_id = zrepld_old_zdb_id
-- into temp tmp_need_two_recs;

--delete from record_attribution
--  where exists (select 'x' 
--		  from tmp_need_two
--		  where recattrib_data_zdb_id = old_id);

!echo "insert replaced recattribs back into recattrib" ;

--insert into record_attribution (recattrib_data_zdb_id,
--				recattrib_sourcE_zdb_id,
--				recattrib_source_type)
--  select data_id, source_id, source_type
--   from tmp_need_two_recs 
--   where not exists (select 'x' 
--			from record_attribution b
--	 	         where data_id = b.recattrib_data_zdb_id
--			 and source_id = b.recattrib_source_zdb_id
--			 and source_type = b.recattrib_source_type);

!echo "count dup recattribs" ;

select count(*), recattrib_data_zdb_id, recattrib_source_zdb_id,
  recattrib_source_type
  from record_attribution
  group by recattrib_data_zdb_id, recattrib_source_zdb_id,
  recattrib_source_type
  having count(*)>1;

update record_attribution
  set recattrib_data_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = 
					recattrib_data_zdb_id)
 where exists (select 'x'
		from zdb_replaced_data
		where zrepld_old_zdb_id = recattrib_data_zdb_id) ;

select count(*), recattrib_data_zdb_id, recattrib_source_zdb_id,
  recattrib_source_type
  from record_attribution
  group by recattrib_data_zdb_id, recattrib_source_zdb_id,
  recattrib_source_type
  having count(*)>1;


!echo "mapped deletion" ;


update mapped_deletion
  set marker_id = (select zrepld_new_zdb_id
			from zdb_replaced_data
			where zrepld_old_zdb_id = marker_id)
  where exists (select 'x'
		  from zdb_replaced_data
		  where zrepld_old_zdb_id = marker_id)
  and (marker_id like 'ZDB-LOCUS-%' or marker_id like 'ZDB-FISH-%'
		or marker_id like 'ZDB-ALT-%');

update mapped_marker
  set marker_id = (select zrepld_new_zdb_id
			from zdb_replaced_data
			where zrepld_old_zdb_id = marker_id),
      marker_type = 'GENE'
  where exists (select 'x'
		  from zdb_replaced_data
		  where zrepld_old_zdb_id = marker_id)
  and (marker_id like 'ZDB-LOCUS-%' or marker_id like 'ZDB-FISH-%'
		or marker_id like 'ZDB-ALT-%')
  and marker_type = 'MUTANT';


create temp table tmp_pato_attrib (pato_id varchar(50), 
					source_id varchar(50))
with no log ;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select distinct apato_zdb_id, apato_pub_zdb_id 
    from atomic_phenotype 
    where not exists (Select 'x'
			from record_attribution b
			where b.recattrib_data_zdb_id = apato_zdb_id
			and b.recattrib_source_zdb_id= apato_pub_zdb_id
			and b.recattrib_source_type = 'standard');
	

select count(*), recattrib_data_zdb_id, recattrib_source_zdb_id,
  recattrib_source_type
  from record_attribution
  group by recattrib_data_zdb_id, recattrib_source_zdb_id,
  recattrib_source_type
  having count(*)>1;

set constraints all immediate;

commit work ;
--rollback work ;