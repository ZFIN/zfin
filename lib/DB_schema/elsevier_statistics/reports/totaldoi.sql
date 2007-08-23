
!echo 'total external doi accesses from pubs with figures'
select count(distinct es_pk_id) as num_doi_allpub
    from 
    elsevier_statistics ,figure ,publication 
    where 
    fig_source_zdb_id = zdb_id
    and es_figure_zdb_id = zdb_id
    and es_external_link is not null 
    and jtype not in ("Unpublished", "Curation")
    ;

!echo 'total external doi accesses from pubs with images only'
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

!echo 'total external doi accesses from pubs with images only'
select count(distinct es_pk_id) as num_doi_imgonly
    from 
    elsevier_statistics ,figure ,publication , image
    where fig_source_zdb_id = zdb_id
    and pub_doi is not null 
    and es_figure_zdb_id = zdb_id
    and es_external_link is not null
    and jtype not in ("Unpublished", "Curation")
    and fig_zdb_id = img_fig_zdb_id
    ;


!echo 'total external doi accesses from pub'
select count(distinct es_pk_id) 
    from elsevier_statistics , publication
    where es_external_link is not null
    and pub_doi is not  null 
    and es_figure_zdb_id = zdb_id
    and jtype not in ("Unpublished", "Curation")
    ;

!echo 'total external doi access from pub with no images'
select count(distinct es_pk_id )
    from 
    elsevier_statistics ,publication 
    where jtype not in ('Unpublished', 'Curation')
    and pub_doi is not null 
    and es_figure_zdb_id = zdb_id
    and es_external_link is not null
    and not exists(
        select * from figure, image 
        where fig_zdb_id = img_fig_zdb_id
        and fig_source_zdb_id = zdb_id 
    )  
    ; 

