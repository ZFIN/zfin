!echo 'doi accesses from pub page'
select count(distinct es_pk_id) as num_doi_allpub
    from 
    elsevier_statistics ,figure ,publication 
    where 
    fig_source_zdb_id = zdb_id
    and es_figure_zdb_id = zdb_id
    and es_external_link is not null 
    and jtype not in ("Unpublished", "Curation")
    ;

!echo 'doi accesses from pub page with image'
select count(distinct es_pk_id) as num_doi_imgonly
    from 
    elsevier_statistics ,figure ,publication 
    where 
    fig_source_zdb_id = zdb_id
    and es_figure_zdb_id = zdb_id
    and es_external_link is not null
    and jtype not in ("Unpublished", "Curation")
    and exists(
        select 't' from image 
        where fig_zdb_id = img_fig_zdb_id)  
    ;
