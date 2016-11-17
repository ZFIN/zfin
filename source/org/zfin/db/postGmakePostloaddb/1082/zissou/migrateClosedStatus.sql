--liquibase formatted sql
--changeset sierra:migrateClosedStatus

update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status_disply = 'Closed, No data')
 where exists (Select 'x' from publication_file
       	      	      where pth_pub_zdb_id = pf_pub_zdb_id
		      and pf_file_type_id = '1')
and exists (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon Review, this publication contains no information currently curated by ZFIN.')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');

update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status_disply = 'Closed, No data')
 where exists (Select 'x' from publication_file
       	      	      where pth_pub_zdb_id = pf_pub_zdb_id
		      and pf_file_type_id = '1')
and exists (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon Review, this publication contains no information currently curated by ZFIN.')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');


update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status = 'Closed, No PDF')
 where not exists (Select 'x' from publication_file
       	      	      where pth_pub_zdb_id = pf_pub_zdb_id
		      and pf_file_type_id = '1')
and not exists  (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon Review by L. Bayraktaroglu, this publication contains no information currently curated by ZFIN.')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');


update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status_disply = 'Closed, No data')
 where exists (Select 'x' from publication_file
       	      	      where pth_pub_zdb_id = pf_pub_zdb_id
		      and pf_file_type_id = '1')
and exists (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon Review by L. Bayraktaroglu, this publication contains no information currently curated by ZFIN.')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');


update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status = 'Closed, No data')
 where not exists  (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon review, this publication contains no information currently curated by ZFIN.')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');

update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status = 'Closed, No data')
 where not exists  (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon Review, this publication contains no information currently curated by ZFIN.')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');


update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status = 'Closed, Archived')
 where not exists  (Select 'x' from publication_note
    	   	   where pnote_text = 'This paper closed unread')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');

update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status = 'Closed, No PDF')
 where not exists (Select 'x' from publication_file
       	      	      where pth_pub_zdb_id = pf_pub_zdb_id
		      and pf_file_type_id = '1')
and not exists  (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon Review, this publication contains no information currently curated by ZFIN.')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');
