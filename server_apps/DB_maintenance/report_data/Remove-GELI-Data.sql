SELECT fig_zdb_id,
  fig_comments,
  fig_source_zdb_id
FROM   figure
WHERE  fig_source_zdb_id = '$PUBID'
       AND fig_comments = 'GELI';

-- delete expression_experiment records via cascade for GELI records
DELETE FROM zdb_active_data
WHERE  zactvd_zdb_id IN (SELECT xpatex_zdb_id
                         FROM   expression_experiment2,
                                figure
                         WHERE  xpatex_source_zdb_id = '$PUBID'
                                AND fig_source_zdb_id = xpatex_source_zdb_id
                                AND fig_comments = 'GELI');

-- delete GELI figures via cascade
DELETE FROM zdb_active_data
WHERE zactvd_zdb_id IN (SELECT fig_zdb_id
                         FROM   figure
                         WHERE  fig_source_zdb_id = '$PUBID'
                                AND fig_comments = 'GELI');

create temp table tmp_id (id varchar(50))
 with no log;

insert into tmp_id (id)
 select get_id('CUR') from single;

insert into zdb_active_data
 select id from single;

insert into curation (cur_zdb_id, cur_pub_Zdb_id, cur_curator_zdb_id, cur_topic,
       	    	     cur_entry_date)

select id, '$PUBID','$PERSONID','Geli Removed',current year to second
 from tmp_id;
