--liquibase formatted sql
--changeset sierra:migrateClosedStatus

update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status_display = 'Closed, No data')
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
			     where pts_status_display = 'Closed, No data')
 where exists (Select 'x' from publication_file
       	      	      where pth_pub_zdb_id = pf_pub_zdb_id
		      and pf_file_type_id = '1')
and exists (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon review, this publication contains no information currently curated by ZFIN.')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');


update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status_display = 'Closed, No PDF')
 where not exists (Select 'x' from publication_file
       	      	      where pth_pub_zdb_id = pf_pub_zdb_id
		      and pf_file_type_id = '1')
and not exists  (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon Review by L. Bayraktaroglu, this publication contains no information currently curated by ZFIN.')
and not exists (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon review, this publication contains no information currently curated by ZFIN.')
and not exists (Select 'x' from publication_note
    	   	   where pnote_text = 'Upon Review, this publication contains no information currently curated by ZFIN.')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated')
 and not exists (Select 'x' from record_attribution
     	 		where recattrib_source_zdb_id = pth_pub_zdb_id);


update pub_tracking_history
 set pth_status_id = (Select pts_pk_id
     		     	     from pub_tracking_status
			     where pts_status_display = 'Closed, No data')
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
			     where pts_status_display = 'Closed, Not a zebrafish paper')
 where exists  (Select 'x' from publication_note
    	   	   where pnote_text = 'This paper closed unread')
 and pth_status_id = (select pts_pk_id from pub_tracking_status
     		     	     where pts_status_display = 'Closed, Curated');


delete from publication_note 
where pnote_text = 'Upon Review, this publication contains no information currently curated by ZFIN.';

delete from publication_note 
where pnote_text = 'Upon review, this publication contains no information currently curated by ZFIN.';

delete from publication_note 
where pnote_text = 'This paper closed unread';

delete from publication_note 
where pnote_text = 'Upon Review by L. Bayraktaroglu, this publication contains no information currently curated by ZFIN.';

delete from publication_note
 where pnote_text = 'Closed Paper';

set triggers for pub_tracking_history disabled;

insert into pub_tracking_history (pth_pub_zdb_id, pth_status_set_by, pth_status_id)
 select zdb_id, 'ZDB-PERS-100329-1', pts_pk_id
  from publication, publication_note, pub_tracking_status
where zdb_id = pnote_pub_zdb_id
 and pnote_text = 'Indexed Paper'
 and pts_status = 'INDEXED'
and not exists (Select 'x' from pub_tracking_history b
    	       	       where b.pth_pub_zdb_id = zdb_id
		       and b.pth_status_id = pts_pk_id);

delete from publication_note
 where pnote_text = 'Indexed Paper';

insert into pub_tracking_history (pth_pub_zdb_id, pth_status_set_by, pth_status_id)
 select zdb_id, 'ZDB-PERS-100329-1', pts_pk_id
  from publication, publication_note, pub_tracking_status
where zdb_id = pnote_pub_zdb_id
 and pnote_text = 'Indexed Paper'
 and pts_status = 'INDEXED'
and not exists (Select 'x' from pub_tracking_history b
    	       	       where b.pth_pub_zdb_id = zdb_id
		       and b.pth_status_id = pts_pk_id);

set triggers for pub_tracking_history enabled;

update publication
 set pub_indexed_date = (select max(pth_status_insert_date)
     		      		from pub_tracking_history, pub_tracking_status
				where pth_pub_zdb_id = zdb_id
				and pth_status_id = pts_pk_id
				and pts_status = 'INDEXED')
 where pub_indexed_date is null
 and exists (Select 'x' from pub_tracking_history
     	    	   where pth_pub_zdb_id = zdb_id);
