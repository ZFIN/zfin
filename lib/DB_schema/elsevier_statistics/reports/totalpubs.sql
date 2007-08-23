

!echo 'total pubs with figures, dois, and images'
select count(distinct zdb_id), DATE( avg(pub_date) ) 
    from publication , figure , image
    where fig_zdb_id = img_fig_zdb_id 
    and fig_source_zdb_id = zdb_id 
    and jtype not in("Unpublished","Curation")
    and pub_doi is not null 
    ;


!echo 'total pubs with dois, but no image'
select count(distinct zdb_id), DATE( avg(pub_date) ) 
    from publication 
    where jtype not in("Unpublished","Curation")
    and pub_doi is not null 
    and not exists(
select *
    from  figure , image
    where fig_zdb_id = img_fig_zdb_id 
    and fig_source_zdb_id = zdb_id 
    )
    ;

!echo 'total pubs with dois, but no image, normal date'
select count(distinct zdb_id), DATE( avg(pub_date) ) 
    from publication 
    where jtype not in("Unpublished","Curation")
    and pub_doi is not null 
    and pub_date > DATE( '6/15/04')  
    and not exists(
select *
    from  figure , image
    where fig_zdb_id = img_fig_zdb_id 
    and fig_source_zdb_id = zdb_id 
    )
    ;

!echo 'total pubs with dois'
    select count(*) 
    from publication  
    where jtype not in("Unpublished","Curation")
    and pub_doi is not null 
    ; 




