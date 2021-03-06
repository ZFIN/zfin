--liquibase formatted sql
--changeset sierra:migratePaperless

alter table publication
 drop entry_time;


update pub_tracking_status 
 set pts_status_qualifier = 'curated'
 where pts_Status = 'CLOSED';

update pub_tracking_status
 set pts_status_display = 'Closed, Curated'
 where pts_Status = 'CLOSED';

update pub_tracking_status 
 set pts_status_qualifier = 'archived'
 where pts_Status = 'ARCHIVED';

update pub_tracking_status
 set pts_status_display = 'Closed, Archived'
 where pts_Status = 'ARCHIVED';

update pub_tracking_status
 set pts_status = 'CLOSED'
 where pts_Status = 'ARCHIVED';


insert into pub_tracking_status (pts_status,
					pts_terminal_status,
					pts_status_display,
					pts_status_qualifier, pts_pipeline_pull_down_order)
  values ('CLOSED','t','Closed, No data', 'no data',13);

insert into pub_tracking_status (pts_status,
					pts_terminal_status,
					pts_status_display,
					pts_status_qualifier, pts_pipeline_pull_down_order)
  values ('CLOSED','t','Closed, Not a zebrafish paper', 'not a zebrafish paper',13);

insert into pub_tracking_status (pts_status,
					pts_terminal_status,
					pts_status_display,
					pts_status_qualifier, pts_pipeline_pull_down_order)
  values ('CLOSED','t','Closed, No PDF', 'no PDF',14);


insert into pub_tracking_status (pts_status, 
       	    		         pts_terminal_status, 
				 pts_status_display, pts_pipeline_pull_down_order)
 values ('INDEXED','f','Indexed',4);


set triggers for pub_tracking_history disabled;

insert into pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_insert_date, pth_status_set_by)
 select distinct zdb_id, (select pts_pk_id from pub_tracking_status
 			where pts_status= 'INDEXED'),pub_indexed_date, 'ZDB-PERS-030520-1'
  from publication
  where pub_indexed_date is not null
 and pub_is_indexed = 't';


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

update pub_tracking_Status
 set pts_pipeline_pull_down_order = 5
 where pts_status_display = 'Ready for Curating';

update pub_tracking_Status
 set pts_pipeline_pull_down_order = 6
 where pts_status_display = 'Curating';

update pub_tracking_Status
 set pts_pipeline_pull_down_order = 8
 where pts_status_display = 'Waiting for Curator Review';

update pub_tracking_Status
 set pts_pipeline_pull_down_order = 9
 where pts_status_display = 'Waiting for Software Fix';

update pub_tracking_Status
 set pts_pipeline_pull_down_order = 10
 where pts_status_display = 'Waiting for Author';

update pub_tracking_Status
 set pts_pipeline_pull_down_order = 11
 where pts_status_display = 'Waiting for Ontology';

update pub_tracking_Status
 set pts_pipeline_pull_down_order = 12
 where pts_status_display = 'Waiting for Nomenclature';

--alter table publication
-- drop pub_is_indexed;

--alter table publication
-- drop pub_indexed_date;

alter table publication
 drop pub_geli_removed;

--drop table curation;
--drop table curation_topic;

--create table pub_data_recorded_topics (pdnc_pk_id serial8 not null constraint dnc_pk_id_not_null,
--       	     		             pdnc_pub_zdb_id varchar(50) not null constraint pdnc_pub_zdb_id_not_null,
--				     pdnc_topic varchar(40) not null constraint pdnc_topic_zdb_id_not_null,
--				     pdnc_curator_zdb_id varchar(50) not null constraint pdnc_curator_zdb_id_not_null,
--				     pdnc_topic_found boolean default 'f' not null constraint pdnc_topic_found_not_null)

--fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3 
--  extent size 4096 next size 4096 lock mode row;
	
--create unique index pdnc_pk_index on pub_data_not_curatable
-- (pdnc_pk_id) using btree in idxdbs1;

--create unique index pdnc_ak_index on pub_data_not_curatable
-- (pdnc_pub_zdb_id, pdnc_topic)
-- using btree in idxdbs3;

--create index pdnc_curator_zdb_id_index on
--     pub_data_not_curatable (pdnc_curator_zdb_id) using btree in idxdbs3 ;
--create index pdnc_pub_zdb_id_index on  pub_data_not_curatable 
--    (pdnc_pub_zdb_id) using btree in idxdbs1;
--create index pdnc_topic_index on  pub_data_not_curatable 
--    (pdnc_topic) using btree in idxdbs2;
			    
--alter table pub_data_not_curatable add constraint (foreign key (pdnc_topic) 
--    references pub_data_not_curatable_topic  constraint
--    pdnc_topic_foreign_key);
--alter table pub_data_not_curatable add constraint (foreign key (pdnc_pub_zdb_id) 
--    references publication  on delete cascade constraint 
 --   pdnc_pub_zdb_id_foreign_key);
--alter table pub_data_not_curatable add constraint (foreign key (pdnc_curator_zdb_id) 
--    references person  constraint pdnc_curator_zdb_id_foreign_key);

--alter table pub_data_not_curatable 
-- add constraint primary key (pdnc_pk_id) 
-- constraint pub_data_not_curatable_primary_key;

--alter table pub_data_not_curatable 
-- add constraint unique (pdnc_pub_zdb_id, pdnc_topic) 
-- constraint pub_data_not_curatable_alternate_key;

--insert into pub_data_not_curatable (pdnc_pub_zdb_id, pdnc_topic, pdnc_curator_zdb_id,pdnc_topic_found)
-- select cur_pub_zdb_id, cur_topic, cur_curator_zdb_id, 'f'
--   from curation
-- where cur_data_found = 'f'
-- and cur_closed_date is not null;

--insert into pub_data_not_curatable (pdnc_pub_zdb_id, pdnc_topic, pdnc_curator_zdb_id,pdnc_topic_found)
-- select cur_pub_zdb_id, cur_topic, cur_curator_zdb_id, 't'
--   from curation
-- where cur_data_found = 't';
