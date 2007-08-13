begin work;

create table elsevier_statistics (
       	     			 es_pk_id serial,
	    			 es_incoming_ip varchar(30) 
       	     			 	not null constraint 
					es_incoming_ip_not_null,
       	     			  es_zfin_url varchar(255) 
				  	not null constraint 
					es_zfin_url_not_null,
				  es_figure_zdb_id varchar(50)
				        not null constraint 
					es_figure_zdb_id_not_null,
				  es_external_link varchar(255),
				  es_date datetime year to second)

fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096
lock mode row;

create unique index elsevier_statistics_primary_key_index
  on elsevier_statistics (es_pk_id)
  using btree in idxdbs4 ;

alter table elsevier_statistics
  add constraint primary key (es_pk_id)
  constraint elsevier_statistics_primary_key ;

--  Exposes necessary variables for doing insertions from web pages.
--  Needs to be added significantly in the past.
insert into webconfigs (config_name,variable_name,overwrite,value,disable,time_stamp) values ('zfin','QUERY_STRING','N','+','N',DATE('12/06/1998') ) ; 
insert into webconfigs (config_name,variable_name,overwrite,value,disable,time_stamp) values ('zfin','REMOTE_ADDR','N','+','N',DATE('12/06/1998') ) ; 
insert into webconfigs (config_name,variable_name,overwrite,value,disable,time_stamp) values ('zfin','HTTP_REFERER','N','+','N',DATE('12/06/1998') ) ; 
insert into webconfigs (config_name,variable_name,overwrite,value,disable,time_stamp) values ('zfin','HTTP_URI','N','+','N',DATE('12/06/1998') ) ; 
insert into webconfigs (config_name,variable_name,overwrite,value,disable,time_stamp) values ('zfin','HTTP_HOST','N','+','N',DATE('12/06/1998') ) ; 
insert into webconfigs (config_name,variable_name,overwrite,value,disable,time_stamp) values ('zfin','HTTP_USER_AGENT','N','+','N',DATE('12/06/1998') ) ; 

commit work ;

--rollback work;
