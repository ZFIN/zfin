!echo 'total pubs'
select count(distinct zdb_id) 
    from publication 
    where jtype not in ("Unpublished","Curation") ;

!echo 'total pubs with dois'
select count(distinct zdb_id) 
    from publication 
    where 
    jtype not in ("Unpublished","Curation") 
    and pub_doi is not null ;

!echo 'total pubs with dois and images'
select count(distinct zdb_id) 
    from publication , figure 
    where jtype not in("Unpublished","Curation")
    and fig_source_zdb_id = zdb_id 
    and exists ( select 't' from image where fig_zdb_id = img_fig_zdb_id)
    and pub_doi is not null ;

