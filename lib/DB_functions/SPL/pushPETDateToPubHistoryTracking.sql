create procedure pushPETDateToPubHistoryTracking (vPubZdbId varchar(50))

       insert into pub_tracking_history (pth_pub_zdb_id,
						pth_status_id,
						pth_status_set_by,
						pth_status_is_current)
          values (vPubZdbId, (select pts_pk_id from pub_tracking_status
 			where pts_status= 'NEW'),'ZDB-PERS-030520-1','t')
			;
			

end procedure;
