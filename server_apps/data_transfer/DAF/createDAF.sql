begin work ;


select '7955' as taxon_id,
       'FISH' as object_type,
       'ZFIN' as db,
       genox_fish_zdb_id as db_object_id,
       'RO:0003301' as association_type,
       term_ont_id as doid,
       dat_source_zdb_id as reference,
       trim(get_date_from_id(dat_zdb_id,"YYYYMMDD")) as dateannotation,
       'ZFIN' as assignedBy,
        genox_zdb_id as genox_id
   from fish_experiment, disease_annotation_model, term, disease_Annotation
  where genox_zdb_id = damo_genox_zdb_id
  and term_zdb_id = dat_term_Zdb_id
  and dat_zdb_id = damo_dat_zdb_id
into temp  tmp_da;

create temp table tmp_da_full (taxon_id varchar(10),
                                object_type varchar(20),
                                db varchar(10),
                                genox_fish_id varchar(50),
                                symbol varchar(255),
                                inferredGeneAssociation lvarchar(500),
                                geneProductForm varchar(30),
                                conditions lvarchar(500),
                                association_type varchar(50),
                                do_id varchar(50),
                                withField varchar(100),
                                evidenceCodes lvarchar(500),
                                sex varchar(50),
                                reference varchar(50),
                                dateAnnotated varchar(30),
                                assignedBy varchar(20),
                                genox_id varchar(50)
                                )
with no log;

create temp table tmp_conds (genox_id varchar(50),
                                      expIdMush varchar(255))
with no log;

insert into tmp_conds (genox_id, expIdMush)
  select genox_zdb_id, z.term_ont_id||"+"||nvl(a.term_ont_id,"None")||"+"||nvl(g.term_ont_id,"None")||"+"||nvl(c.term_ont_id,"None")||"+"||nvl(t.term_ont_id,"None")
   from fish_experiment, experiment_condition, disease_annotation_model, term z, outer term a , outer term g, outer term c, outer term t
   where genox_exp_zdb_id = expcond_exp_zdb_id
   and expcond_zeco_term_zdb_id = z.term_zdb_id
   and expcond_ao_term_zdb_id = a.term_zdb_id
   and expcond_chebi_term_zdb_id = c.term_zdb_id
   and expcond_go_cc_term_zdb_id = g.term_zdb_id
   and expcond_taxon_Term_zdb_id = t.term_zdb_id
   and damo_genox_zdb_id = genox_zdb_id;


update tmp_da_full
  set inferredGeneAssociation = replace(inferredGeneAssociation, "\", "");

update tmp_conds
  set expIdMush = replace(expIdMush, "++++","+");

update tmp_conds

  set expIdMush = replace(expIdMush, "+++","+");
--select distinct expIdMush  from tmp_conds;

insert into tmp_da_full (taxon_id, object_type, db, genox_Fish_id, association_type, do_id, reference, dateAnnotated, assignedBy, genox_id)
  select taxon_id, object_type, db,db_object_id,association_type,doid, reference,dateannotation,assignedBy,genox_id   
    from tmp_da;

update tmp_da_full 
  set conditions = replace(replace(replace(substr(multiset (select distinct expIdMush from tmp_conds where tmp_conds.genox_id = tmp_da_full.genox_id
                                                          )::lvarchar(4000),11),""),"'}",""),"'","");

update tmp_da_full
 set conditions = replace(conditions, "OW(","");

update tmp_da_full
 set conditions = replace(conditions, ")}","");

update tmp_da_full
 set conditions = replace(conditions, "}","");

update tmp_da_full
 set conditions = replace(conditions, ")","");

update tmp_da_full
 set conditions = replace(conditions, "RZECO","ZECO");

update tmp_da_full
 set conditions = replace(conditions, ",","|");

--select expIdMush from tmp_conds where tmp_conds.genox_id = tmp_da_full.genox_id

--select distinct conditions from tmp_da_full where conditions is not null;

update tmp_da_full 
  set evidenceCodes = replace(replace(replace(substr(multiset (select distinct dat_evidence_code from disease_annotation, disease_annotation_model where damo_genox_zdb_id = tmp_da_full.genox_id and dat_source_zdb_id = tmp_da_full.reference
                                                          )::lvarchar(4000),11),""),"'}",""),"'","");

update tmp_da_full
  set evidenceCodes = replace (evidenceCodes, "OW(","");

update tmp_da_full
  set evidenceCodes = replace (evidenceCodes, ")","");

update tmp_da_full
  set evidenceCodes = replace (evidenceCodes, ")}","");

update tmp_da_full
  set evidenceCodes = replace (evidenceCodes, "}","");

update tmp_da_full
  set evidenceCodes = replace (evidenceCodes, "R","");

update tmp_da_full
  set evidenceCodes = replace (evidenceCodes, ",","|");

--select distinct evidenceCodes from tmp_da_full;

update tmp_da_full
  set inferredGeneAssociation  =  replace(replace(replace(substr(multiset (select distinct mfs_mrkr_zdb_id from mutant_fast_search, fish_experiment
                                                                                  where mfs_genox_zdb_id = genox_id
										  and (mfs_mrkr_zdb_id like 'ZDB-GENE%' or mfs_mrkr_zdb_id like '%RNAG%')
										  and genox_id = genox_zdb_id
										  and mfs_genox_zdb_id = genox_id
										  and genox_is_std_or_generic_control = 't'
                                                          )::lvarchar(4000),11),""),"'}",""),"'","");

update tmp_da_full
  set inferredGeneAssociation = replace(inferredGeneAssociation, "OW(","");

update tmp_da_full
  set inferredGeneAssociation = replace(inferredGeneAssociation, "ROW(","");

update tmp_da_full
  set inferredGeneAssociation = replace(inferredGeneAssociation, ")","");

update tmp_da_full
  set inferredGeneAssociation = replace(inferredGeneAssociation, "}","");

update tmp_da_full
  set inferredGeneAssociation = replace(inferredGeneAssociation, "RZ","Z");

update tmp_da_full
  set inferredGeneAssociation = replace(inferredGeneAssociation, ",","|");

--select genox_id, inferredGeneAssociation from tmp_da_full;

insert into tmp_da_full (taxon_id,
				object_type,
				db,
				genox_fish_id,
				association_type,
				do_id,
				evidenceCodes,
				reference,
				dateAnnotated,
				assignedBy,
				inferredGeneAssociation)
 select distinct '7955',
	'GENE',
	'ZFIN',
	ortho_zebrafish_gene_zdb_id,
	'RO:0003302',
	term_ont_id,
	'ISS',
	'ZDB-PUB-170210-12',
	current year to second,
	'ZFIN',
	ortho_zebrafish_gene_zdb_id
   from ortholog, omim_phenotype, omimp_termxref_mapping, term_xref, term
   	where tx_term_zdb_id = term_zdb_id
	and otm_omimp_id = omimp_pk_id
	and omimp_ortho_zdb_id = ortho_zdb_id
	and otm_tx_id = tx_pk_id;
				


unload to "daf.unl"
       DELIMITER "	" 
 select taxon_id, object_type, db, genox_fish_id, symbol, inferredGeneAssociation, geneProductForm, conditions, association_type, do_id, withField, evidenceCodes, sex, reference, dateAnnotated, assignedBy from tmp_da_full;  


commit work;

--rollback work ;
