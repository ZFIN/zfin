!echo 'total pubs'
select count(distinct zdb_id) , DATE( avg(pub_date) ) 
    from publication 
    where jtype not in ("Unpublished","Curation") ;

!echo 'total pubs with dois'
select count(distinct zdb_id) , DATE( avg(pub_date) ) 
    from publication 
    where 
    jtype not in ("Unpublished","Curation") 
    and pub_doi is not null ;


!echo 'total pubs with dois post-June 2004'
select count(distinct zdb_id) , DATE( avg(pub_date) ) 
    from publication 
    where 
    jtype not in ("Unpublished","Curation") 
    and pub_doi is not null 
    and pub_date > DATE( '6/1/04')  
    ;


!echo 'total pubs with dois and images with average date'
select count(distinct zdb_id), DATE( avg(pub_date) ) 
    from publication , figure 
    where jtype not in("Unpublished","Curation")
    and fig_source_zdb_id = zdb_id 
    and exists ( select 't' from image where fig_zdb_id = img_fig_zdb_id)
    and pub_doi is not null ;



