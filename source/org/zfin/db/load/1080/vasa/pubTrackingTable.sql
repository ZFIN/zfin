--liquibase formatted sql
--changeset sierra:pubTracking

create table pub_tracking_history (pth_pk_id serial8 not null constraint pth_pk_id_not_null,
       	     			  	     pth_pub_Zdb_id varchar(50) not null constraint pth_pub_zdb_id_not_null,
					     pth_event_id int8 not null constraint pth_event_id_not_null,
					     pth_location_id int8 not null constraint pth_location_id_not_null,
					     pth_claimed_by varchar(50),
					     pth_last_updated_by varchar(50) not null constraint pth_last_updated_by_not_null,
					     pth_event_set_by varchar(50) not null constraint pth_event_set_by_not_null,
					     pth_last_updated_date datetime year to second default current year to second not null constraint pth_last_updated_date_not_null,
					     pth_event_insert_date datetime year to second default current year to second not null constraint pth_event_insert_date_not_null)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 4096 next size 4096;
					     					     
create unique index pub_tracking_history_primary_key_index on 
       pub_tracking_history (pth_pk_id)
       using btree in idxdbs2;

create index pth_pub_zdb_id_fk_index
 on pub_tracking_history (pth_pub_zdb_id)
 using btree in idxdbs3;

create index pth_location_fk_index
       on pub_tracking_history (pth_location_id)
       using btree in idxdbs1;

create index pth_event_fk_index
       on pub_tracking_history (pth_event_id)
 using btree in idxdbs2;

create index pth_event_set_by_fk_index
       on pub_tracking_history (pth_event_set_by)
 using btree in		       idxdbs1;

create index pth_claimed_by_fk_index
       on pub_tracking_history (pth_claimed_by)
 using btree in                idxdbs2;

create unique index pub_tracking_history_alternative on
       pub_tracking_history (pth_pub_zdb_id, pth_event_id, pth_location_id)
using btree in idxdbs3;

alter table pub_tracking_history 
 add constraint primary key (pth_pk_id)
 constraint pth_primary_key;

alter table pub_tracking_history
  add constraint (foreign key (pth_pub_zdb_id)
 references publication constraint pth_pub_fk);

alter table pub_tracking_history
  add constraint (foreign key (pth_claimed_by)
 references person	  constraint pth_claimed_by_fk);

alter table pub_tracking_history
  add constraint (foreign key (pth_event_set_by)
 references person	  constraint pth_event_set_by_fk);

alter table pub_tracking_history
 add constraint unique (pth_pub_zdb_id, pth_event_id, pth_location_id)
 constraint pth_alternate_key;


--bins, numbers, desks
create table pub_tracking_location (ptl_pk_id serial8 not null constraint ptl_pk_id_not_null,
       	     			   	     ptl_location varchar(10) not null constraint ptl_location_not_null,
					     ptl_location_process_order int not null constraint ptl_location_process_order)
in tbldbs2
extent size 16 next size 16
lock mode page;

create unique index ptl_pk_index
 on pub_tracking_location (ptl_pk_id)
 using btree in idxdbs2;

create unique index ptl_location_alternate_key_index
 on pub_tracking_location (ptl_location)
 using btree in idxdbs1;

alter table pub_Tracking_location
 add constraint primary key (ptl_pk_id)
 constraint ptl_primary_key;

alter table pub_tracking_location
 add constraint unique (ptl_location)
 constraint ptl_alternate_key;

--indexed, closed, assigned to bin?, reassigned?, waiting for x,y,z
create table pub_tracking_event (pte_pk_id serial8 not null constraint pte_pk_id_not_null,
       	     				   pte_event varchar(100))
in tbldbs3 
extent size 16 next size 16
lock mode page;

			
create unique index pte_pk_index
 on pub_tracking_event (pte_pk_id)
 using btree in		  idxdbs2;

create unique index pte_event_alternate_key_index
 on pub_tracking_event (pte_event)
 using btree in		  idxdbs1;

alter table pub_Tracking_event
 add constraint	primary	key (pte_pk_id)
 constraint pte_primary_key;

alter table pub_tracking_event
 add constraint	unique (pte_event)
 constraint pte_alternate_key;		     

alter table pub_tracking_history
  add constraint (foreign key (pth_location_id)
 references pub_tracking_location  constraint pth_location_fk);

alter table pub_tracking_history
  add constraint (foreign key (pth_event_id)
 references pub_tracking_event    constraint pth_event_fk);


