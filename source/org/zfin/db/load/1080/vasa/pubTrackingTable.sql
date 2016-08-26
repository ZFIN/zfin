--liquibase formatted sql
--changeset sierra:pubTracking


create table pub_tracking_history (pth_pk_id serial8 not null constraint pth_pk_id_not_null,
       	     			  	     pth_pub_Zdb_id varchar(50) not null constraint pth_pub_zdb_id_not_null,
					     pth_status_id int8 not null constraint pth_status_id_not_null,
					     pth_location_id int8,
					     pth_status_set_by varchar(50) not null constraint pth_status_set_by_not_null,
					     pth_claimed_by varchar(50),
					     pth_status_insert_date datetime year to second default current year to second not null constraint pth_status_insert_date_not_null,
					     pth_status_is_current boolean default 'f' not null constraint pth_status_is_current_not_null)
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

create index pth_status_fk_index
       on pub_tracking_history (pth_status_id)
 using btree in idxdbs2;

create index pth_status_set_by_fk_index
       on pub_tracking_history (pth_status_set_by)
 using btree in		       idxdbs1;


alter table pub_tracking_history
  add constraint (foreign key (pth_claimed_by)
  references person constraint pth_claimed_by_foreign_key);

alter table pub_tracking_history 
 add constraint primary key (pth_pk_id)
 constraint pth_primary_key;

alter table pub_tracking_history
  add constraint (foreign key (pth_pub_zdb_id)
 references publication constraint pth_pub_fk);

alter table pub_tracking_history
  add constraint (foreign key (pth_status_set_by)
 references person	  constraint pth_status_set_by_fk);

--bins, numbers, desks
create table pub_tracking_location (ptl_pk_id serial8 not null constraint ptl_pk_id_not_null,
       	     			   	     ptl_location varchar(30) not null constraint ptl_location_not_null,
					     ptl_location_display varchar(60) not null constraint ptl_location_display_not_null,
					     ptl_role varchar(50) not null constraint ptl_role_not_null)
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


--indexed, closed, assigned to bin, waiting for x,y,z, claimed
create table pub_tracking_status (pts_pk_id serial8 not null constraint pts_pk_id_not_null,
       	     				   pts_status varchar(100),
					   pts_terminal_status boolean default 't' not null constraint pts_terminal_status_not_null,
					   pts_status_display varchar(150) not null constraint pts_status_display_not_null,
					   pts_status_qualifier varchar(50))
in tbldbs3 
extent size 16 next size 16
lock mode page;

			
create unique index pts_pk_index
 on pub_tracking_status (pts_pk_id)
 using btree in		  idxdbs2;

create unique index pts_status_alternate_key_index
 on pub_tracking_status (pts_status, pts_status_qualifier)
 using btree in		  idxdbs1;

alter table pub_Tracking_status
 add constraint	primary	key (pts_pk_id)
 constraint pts_primary_key;

alter table pub_tracking_status
 add constraint	unique (pts_status, pts_status_qualifier)
 constraint pts_alternate_key;	

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status)
 values ('NEW','New','f');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status)
 values ('READY_FOR_INDEXING','Ready for Indexing','f');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status)
 values ('INDEXING','Indexing','f');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status)
 values ('READY_FOR_CURATION','Ready for Curation','f');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status)
 values ('CURATING','Curating','f');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status, pts_status_qualifier)
 values ('WAIT','Waiting for Curator Review','f','curator review');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status, pts_status_qualifier)
 values ('WAIT','Waiting for Software Fix','f','software');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status, pts_status_qualifier)
 values ('WAIT','Waiting for Author','f','author');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status, pts_status_qualifier)
 values ('WAIT','Waiting for Ontology','f','ontology');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status, pts_status_qualifier)
 values ('WAIT','Waiting for Nomenclature','f','nomenclature');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status)
 values ('CLOSED','Closed','t');

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status)
 values ('ARCHIVED','Archived','t');

alter table pub_tracking_history
  add constraint (foreign key (pth_location_id)
 references pub_tracking_location  constraint pth_location_fk);

alter table pub_tracking_history
  add constraint (foreign key (pth_status_id)
 references pub_tracking_status    constraint pth_status_fk);

insert into pub_tracking_location (ptl_location, ptl_location_display, ptl_role)
 values ('BIN_1','Bin 1','curator');

insert into pub_tracking_location (ptl_location, ptl_location_display, ptl_role)
 values ('BIN_2','Bin 2','curator');

insert into pub_tracking_location (ptl_location, ptl_location_display, ptl_role)
 values ('NEW_PHENO','New Phenotype','curator');

insert into pub_tracking_location (ptl_location, ptl_location_display, ptl_role)
 values ('NEW_EXPR','New Expression','curator');

insert into pub_tracking_location (ptl_location, ptl_location_display, ptl_role)
 values ('ORTHO','Orthology','curator');

insert into pub_tracking_location (ptl_location, ptl_location_display, ptl_role)
 values ('BIN_3','Bin 3','curator');

insert into pub_tracking_location (ptl_location, ptl_location_display, ptl_role)
 values ('pub_indexer_1','1','indexer');

insert into pub_tracking_location (ptl_location, ptl_location_display, ptl_role)
 values ('pub_indexer_2','2','indexer');

insert into pub_tracking_location (ptl_location, ptl_location_display, ptl_role)
 values ('pub_indexer_3','3','indexer');

