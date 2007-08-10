begin work ;

create table excluded_ip (ei_ip varchar(30) not null constraint
       ei_ip_not_null, ei_ip_type varchar(30) not null constraint
       ei_ip_type_not_null,
       check(
		(ei_ip_type='external') or (ei_ip_type='internal')
	    ) 
       	constraint exclude_ip_type_is_either_external_or_internal 
 )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
  extent size 256 next size 256 ;

create unique index excluded_ip_primary_key_index
  on excluded_ip (ei_ip) using btree in idxdbs3 ;

alter table excluded_ip
  add constraint primary key (ei_ip)
   constraint excluded_ip_primary_key_constraint ;

alter table elsevier_statistics 
  add (es_http_user_agent varchar(255)) ;

--commit work ;

rollback work ;