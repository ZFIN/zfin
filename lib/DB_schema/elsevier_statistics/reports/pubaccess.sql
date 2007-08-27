


!echo 'accesses of pubview2 page for pubs with valid doi'
select count(distinct es_pk_id) as num_doi_allpub, date ( avg (pub_date) )
    from 
    elsevier_statistics ,publication 
    where jtype not in ("Unpublished", "Curation")
    and es_figure_zdb_id = zdb_id
    and es_external_link is null 
    and pub_doi is not null 
    ;

!echo 'accesses of pubview2 page with doi and image'
select count(distinct es_pk_id), date ( avg (pub_date) )
    from 
    elsevier_statistics ,figure ,publication , image
    where jtype not in ("Unpublished", "Curation")
    and es_figure_zdb_id = zdb_id
    and es_external_link is null 
    and pub_doi is not null 
    and fig_source_zdb_id = zdb_id
    and fig_zdb_id = img_fig_zdb_id
    ;


!echo 'accesses of pubview2 page with doi but w/o image'
select count(distinct es_pk_id) , date ( avg (pub_date) )
    from 
    elsevier_statistics ,publication 
    where jtype not in ("Unpublished", "Curation")
    and es_figure_zdb_id = zdb_id
    and es_external_link is null 
    and pub_doi is not null 
    and not exists(
        select *
        from figure , image
        where fig_zdb_id = img_fig_zdb_id
        and fig_source_zdb_id = zdb_id 
        )
    ;

!echo 'accesses of pubview2 page with doi but w/o image after 6/15/04'
select count(distinct es_pk_id) , date ( avg (pub_date) )
    from 
    elsevier_statistics ,publication 
    where jtype not in ("Unpublished", "Curation")
    and es_figure_zdb_id = zdb_id
    and es_external_link is null 
    and pub_doi is not null 
    and pub_date > date('6/15/04')
    and not exists(
        select *
        from figure , image
        where fig_zdb_id = img_fig_zdb_id
        and fig_source_zdb_id = zdb_id 
        )
    ;
