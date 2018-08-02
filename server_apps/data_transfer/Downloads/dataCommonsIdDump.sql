begin work;

create temp table tmp_id (id text, id2 text, uri text, destination_url text, outgoing_uri text, entity_type text, biolink_type text);

insert into tmp_id (id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||feature_zdb_id, 'ZFIN:'||feature_zdb_id, 'http://zfin.org/'||'ZFIN:'||feature_zdb_id,'http://zfin.org/'||'ZFIN:'||feature_zdb_id, 'http://zfin.org/'||'ZFIN:'||feature_zdb_id, 'feature', szm_term_ont_id from feature, so_zfin_mapping
where feature_type = szm_object_type;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||mrkr_zdb_id,'ZFIN:'||mrkr_zdb_id, 'http://zfin.org/'||'ZFIN:'||mrkr_zdb_id,'http://zfin.org/'||'ZFIN:'||mrkr_zdb_id,'http://zfin.org/'||'ZFIN:'||mrkr_zdb_id, mrkr_type, szm_term_ont_id from marker, so_zfin_mapping
where mrkr_type = szm_object_type;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||geno_zdb_id,'ZFIN:'||geno_zdb_id,'http://zfin.org/'||'ZFIN:'||geno_zdb_id, 'http://zfin.org/'||'ZFIN:'||geno_zdb_id,'http://zfin.org/'||'ZFIN:'||geno_zdb_id, 'genotype','' from genotype;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||fish_zdb_id, 'ZFIN:'||fish_zdb_id, 'http://zfin.org/'||'ZFIN:'||fish_zdb_id,'http://zfin.org/'||'ZFIN:'||fish_zdb_id,'http://zfin.org/'||'ZFIN:'||fish_zdb_id,'fish','' from fish;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select term_ont_id,term_ont_id, 'http://zfin.org/'||term_ont_id,'http://zfin.org/'||term_ont_id,'http://zfin.org/'||term_ont_id,'ontology term', '' from term;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select term_zdb_id, term_zdb_id, 'http://zfin.org/'||'ZFIN:'||term_zdb_id, 'http://zfin.org/'||'ZFIN:'||term_zdb_id, 'http://zfin.org/'||'ZFIN:'||term_zdb_id, 'ontology term', '' from term;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select tx_full_accession,tx_full_accession,'','','','cross reference','' from term_xref;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||dblink_zdb_id, 'ZFIN:'||dblink_zdb_id,'','','','cross reference','' from db_link;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select tx_full_accession,tx_full_accession, '','','','cross reference',''  from term_xref;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select fdb_db_display_name||':'||dblink_acc_num,fdb_db_display_name||':'||dblink_acc_num, fdb_db_query||dblink_acc_num||fdb_url_suffix, fdb_db_query||dblink_acc_num||fdb_url_suffix, fdb_db_query||dblink_acc_num||fdb_url_suffix, 'cross reference',''
   from db_link, foreign_db_contains, foreign_db
    where dblink_fdbcont_zdb_id = fdbcont_zdb_id
    and fdbcont_fdb_db_id = fdb_db_pk_id;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:XPATRES-'||xpatres_pk_id,'ZFIN:XPATRES-'||xpatres_pk_id,'','','','expression result','' from expression_result2;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||exp_zdb_id, 'ZFIN:'||exp_zdb_id, 'https://http://zfin.org/action/expression/experiment?id='||exp_zdb_id,'https://http://zfin.org/action/expression/experiment?id='||exp_zdb_id,'https://http://zfin.org/action/expression/experiment?id='||exp_zdb_id,'experiment','' from experiment;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||genox_zdb_id,'ZFIN:'||genox_zdb_id, '','','','genotype experiment',''from fish_experiment;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||zdb_id,'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'person','' from person;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||zdb_id,'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'company','' from company;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||zdb_id,'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'publication',''  from publication;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||zdb_id,'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'http://zfin.org/'||'ZFIN:'||zdb_id, 'lab','' from lab;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||zrepld_old_zdb_id,'ZFIN:'||zrepld_old_zdb_id,'http://zfin.org/'||'ZFIN:'||zrepld_old_zdb_id,'http://zfin.org/'||'ZFIN:'||zrepld_old_zdb_id,'http://zfin.org/'||'ZFIN:'||zrepld_old_zdb_id,'various','' from zdb_replaced_data;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||fig_zdb_id, 'ZFIN:'||fig_zdb_id, 'http://zfin.org/'||'ZFIN:'||fig_zdb_id,'http://zfin.org/'||'ZFIN:'||fig_zdb_id, 'http://zfin.org/'||'ZFIN:'||fig_zdb_id, 'figure',''from figure;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||img_zdb_id,'ZFIN:'||img_zdb_id,'http://zfin.org/'||'ZFIN:'||img_zdb_id,'http://zfin.org/'||'ZFIN:'||img_zdb_id,'http://zfin.org/'||'ZFIN:'||img_zdb_id, 'image','' from image;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||jrnl_zdb_id, 'ZFIN:'||jrnl_zdb_id, 'http://zfin.org/'||'ZFIN:'||jrnl_zdb_id,'http://zfin.org/'||'ZFIN:'||jrnl_zdb_id, 'http://zfin.org/'||'ZFIN:'||jrnl_zdb_id,'journal','' from journal;

insert into tmp_id(id, id2, uri, destination_url, outgoing_uri, entity_type, biolink_type)
 select 'ZFIN:'||xpatex_zdb_id, 'ZFIN:'||xpatex_zdb_id, '','','','expression experiment','' from expression_experiment2;

select count(*) from tmp_id;


\copy (select * from tmp_id) to 'zfin_ids.txt';

--rollback work;

commit work;
