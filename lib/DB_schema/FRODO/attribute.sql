begin work ;

update statistics for procedure ;


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
					new_id varchar(50),
					old_id varchar(50))
with no log ;

insert into tmp_dup_attribs (data_id, source_id, new_id, old_id )
 select a.recattrib_data_zdb_id, 
	 a.recattrib_source_zdb_id, 
	 zrepld_new_zdb_id, zrepld_old_zdb_id
            from record_attribution a, zdb_replaced_data
             where (a.recattrib_datA_zdb_id like 'ZDB-LOCUS-%'
                    or a.recattrib_data_zdb_id like 'ZDB-ALT-%')
             and zrepld_old_zdb_id = recattrib_Data_zdb_id 
             and exists (select 'x' 
		          from record_attribution b
	 	         where zrepld_new_zdb_id = b.recattrib_Data_zdb_id
 		          and a.recattrib_source_zdb_id = 
				b.recattrib_source_zdb_id);

--delete the records that will become dups with addition of locus ids.

delete from record_attribution 
  where exists (select 'x'
		from tmp_dup_attribs
		where source_id = recattrib_source_zdb_id
		and data_id = recattrib_data_zdb_id);


select count(*) as counter, zrepld_old_zdb_id as old_id
  from zdb_replaced_data
  where zrepld_old_zdb_id like 'ZDB-LOCUS-%'
  group by zrepld_old_zdb_id
  having count(*) > 1 
  into temp tmp_need_two;

select zrepld_new_zdb_id as data_id, recattrib_source_zdb_id as source_id
 from record_Attribution, zdb_replaced_data, tmp_need_two
    where zrepld_old_zdb_id = recattrib_data_zdb_id
    and old_id = zrepld_old_zdb_id
 into temp tmp_need_two_recs;

delete from record_attribution
  where exists (select 'x' 
		  from tmp_need_two
		  where recattrib_data_zdb_id = old_id);

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_sourcE_zdb_id)
  select data_id, source_id
   from tmp_need_two_recs ;


insert into record_attribution (recattrib_data_zdb_id,
		recattrib_source_zdb_id)
  select distinct locus.zdb_id, b.recattrib_source_zdb_id
	from locus, fish, record_Attribution b
        where b.recattrib_data_zdb_id = fish.zdb_id
	and fish.locus = locus.zdb_id
	and not exists (select 'x'
			  from locus, record_attribution c
			  where c.recattrib_data_zdb_id = locus.zdb_id
			  and c.recattrib_source_zdb_id = 
				b.recattrib_source_zdb_id) 
        and not exists (select 'x'
			  from zdb_replaced_data
			  where locus.zdb_id = zrepld_old_zdb_id);


insert into record_attribution (recattrib_data_zdb_id,
		recattrib_source_zdb_id)
  select distinct alteration.zdb_id, b.recattrib_source_zdb_id
	from fish, alteration, record_Attribution b
        where b.recattrib_data_zdb_id = fish.zdb_id
	and fish.allele = alteration.allele
	and not exists (select 'x'
			  from alteration, record_attribution c
			  where c.recattrib_data_zdb_id = alteration.zdb_id
			  and c.recattrib_source_zdb_id = 
				b.recattrib_source_zdb_id) 
        and not exists (select 'x'
			  from zdb_replaced_data
			  where alteration.zdb_id = zrepld_old_zdb_id);


update record_attribution
  set recattrib_data_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = 
					recattrib_data_zdb_id)
 where exists (select 'x'
		from zdb_replaced_data
		where zrepld_old_zdb_id = recattrib_data_zdb_id);


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

insert into tmp_pato_attrib  (pato_id, source_id)
  select pato_zdb_id, colattrib_source_zdb_id
			from column_attribution, 
				genotype_experiment, 
				experiment, phenotype_anatomy
			where colattrib_data_zdb_id = genox_geno_zdb_id
			and genox_zdb_id = pato_genox_zdb_id
			and colattrib_column_name = 'phenotype'
			and exp_zdb_id = genox_exp_zdb_id
			and exp_name = '_Standard';

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select pato_id, source_id
   from tmp_pato_attrib;

delete from tmp_pato_attrib ;

insert into tmp_pato_attrib (pato_id, source_id)
  select patog_zdb_id, colattrib_source_zdb_id
			from column_attribution, 
				genotype_experiment,
				experiment, phenotype_go
			where colattrib_data_zdb_id = genox_geno_zdb_id
			and genox_zdb_id = patog_genox_zdb_id
			and colattrib_column_name = 'phenotype'
			and genox_exp_Zdb_id = exp_zdb_id
			and exp_name = '_Standard' ;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select pato_id, source_id
    from tmp_pato_attrib;

--add feature_marker_relationship attributions b/c the interface does

!echo "Need to fix column attribution and fmrel_zdb_id attributions before production" ;
  
insert into record_attribution (recattrib_data_zdb_id,
				recattrib_sourcE_zdb_id)
  select distinct fmrel_zdb_id,colattrib_source_zdb_id
    from feature_marker_relationship,
	column_attribution, zdb_replaced_data
    where zrepld_old_zdb_id = colattrib_data_zdb_id
    and zrepld_new_zdb_id = fmrel_mrkr_zdb_id
    and colattrib_column_name = 'cloned_gene'
    and not exists (select 'x' 
			from record_attribution
			where recattrib_data_zdb_id = fmrel_zdb_id
			and colattrib_source_zdb_id = recattrib_source_zdb_id);

--add any not attributed: mainly from loci w/o cloned genes.

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_sourcE_zdb_id)
  select fmrel_zdb_id, 'ZDB-PUB-020723-5'
    from feature_marker_relationship
    where not exists (select 'x' 
			from record_attribution
			where recattrib_data_zdb_id = fmrel_zdb_id);
	
--add genofeat attributions

!echo "Need to fix column attribution and genofeat_zdb_id attributions before production" ;

insert into record_attribution (recattrib_data_zdb_id,
		recattrib_source_zdb_id)
  select genofeat_zdb_id, colattrib_source_zdb_id
    from genotype_feature,
	column_attribution, feature
    where feature_zdb_id = colattrib_data_zdb_id
    and feature_zdb_id = genofeat_feature_zdb_id
    and colattrib_column_name = 'protocol'
    and not exists (select 'x' 
			from record_attribution
			where recattrib_data_zdb_id = genofeat_zdb_id
			and colattrib_source_zdb_id = recattrib_source_zdb_id);

--add any from genotype_feature that aren't in already...attribute
--to curation pub.

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_sourcE_zdb_id)
  select genofeat_zdb_id, 'ZDB-PUB-020723-5'
    from genotype_feature
    where not exists (select 'x' 
			from record_attribution
			where recattrib_data_zdb_id = genofeat_zdb_id);
	

commit work ;
--rollback work ;