begin work;
delete from htp_dataset_category_tag;
delete from htp_category_tag;
delete from htp_dataset_alternate_identifier;
delete from htp_dataset_publication;
delete from htp_dataset;

create temp table tmp_dataset (datasetId text,
				secondaryId text,
				pub_list text,
				title text,
				summary text,
				subseries text,
				category_tag_list text);

\copy tmp_dataset from '/opt/zfin/source_roots/swirl/ZFIN_WWW/server_apps/data_transfer/RNASeqCuration/data_set.txt' with delimiter E'\t' null as '';

create temp table tmp_category_Tags (datasetId text, tag text);
insert into tmp_category_tags(datasetId, tag)
select datasetId, trim(replace(regexp_split_to_table(category_tag_list, E','),'"','')) as foo from tmp_dataset;

create temp table tmp_unique_tag(tag text);
insert into tmp_unique_tag (tag)
  select distinct tag from tmp_category_tags;

insert into htp_category_tag(hct_zdb_id, hct_category_tag)
  select get_id('HTPTAG'), tag
    from tmp_unique_tag;
    

create temp table tmp_pubs(datasetId text, pub text);
insert into tmp_pubs (datasetId, pub)
select datasetId, trim(replace(regexp_split_to_table(pub_list, E','), '"','')) as foo from tmp_dataset;

create temp table tmp_pubs_pmid(datasetId text, pub int);
insert into tmp_pubs_pmid(datasetId,pub)
 select datasetId, cast(replace(pub, 'PMID:','') as integer)
   from tmp_pubs;

create temp table tmp_id_map (zdb_id text, oid text);
insert into tmp_id_map(zdb_id, oid)
  select get_id('HTPDSET'), datasetid from tmp_dataset;

insert into zdb_active_data (zactvd_zdb_id)
  select zdb_id from tmp_id_map;

insert into htp_dataset(hd_zdb_id, hd_original_dataset_id, hd_title, hd_summary, hd_date_curated)
  select zdb_id, datasetid, replace(title,'"','' ), replace(summary,'"','' ), now()
    from tmp_dataset, tmp_id_map
    where datasetid = oid
  ;

create temp table tmp_ids(hd_zdb_id text, hd_acc_num text);

insert into tmp_ids (hd_zdb_id, hd_acc_num)
 select hd_zdb_id, hd_original_dataset_id 
   from htp_dataset;

insert into htp_dataset_alternate_identifier (hdai_hd_zdb_id, hdai_accession_number) 
  select distinct hd_zdb_id, hd_acc_num from tmp_ids;

insert into htp_dataset_publication (hdp_dataset_zdb_id, hdp_pub_zdb_id)
   select hd_zdb_id, zdb_id
     from publication, htp_dataset, tmp_pubs_pmid
    where accession_no = pub
and hd_original_dataset_id = datasetId;

--select * from htp_category_tag;

insert into htp_dataset_category_tag (hdct_dataset_zdb_id, hdct_category_tag)
   select distinct hd_zdb_id, (select hct_zdb_id from htp_category_tag where hct_category_tag = tag)
     from htp_dataset, tmp_category_tags
     where hd_original_dataset_id = datasetId;

--rollback work;
commit work;
