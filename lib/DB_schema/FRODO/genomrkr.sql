begin work ;

create table genotype_marker (genomrkr_zdb_id varchar(50) not null constraint
					genomrkr_zdb_id_not_null,
				genomrkr_geno_zdb_id varchar(50) not null constraint
					genomrkr_geno_zdb_id_not_null, 
				genomrkr_mrkr_zdb_id varchar(50) not null constraint
					genomrkr_mrkr_zdb_id_not_null,	
				genomrkr_chromosome varchar(2),
				genomrkr_dad_zygocity varchar(50) not null 
				  constraint genomrkr_dad_zygocity_not_null,
				genomrkr_mom_zygocity varchar(50) not null
				  constraint genomrkr_mom_zygocity_not_null,
				genomrkr_zygocity varchar(50) not null constraint
					genomrkr_zygocity_not_null
)

fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 2048 next size 2048 lock mode row ;	

create unique index genotype_marker_primary_key_index
  on genotype_marker (genomrkr_zdb_id)
  using btree in idxdbs3;

create unique index genotype_marker_alternate_key_index
  on genotype_marker (genomrkr_geno_zdb_id, genomrkr_mrkr_zdb_id) 
  using btree in idxdbs3;

create index genomrkr_zygocity_index
  on genotype_marker (genomrkr_zygocity)
  using btree in idxdbs3 ;

create index genomrkr_mom_zygocity_index
  on genotype_marker (genomrkr_mom_zygocity)
  using btree in idxdbs3 ;

create index genomrkr_dad_zygocity_index
  on genotype_marker (genomrkr_dad_zygocity)
  using btree in idxdbs3 ;

alter table genotype_marker
  add constraint (foreign key (genomrkr_zygocity)
  references zygocity constraint 
	genomrkr_zygocity_foreign_key);

alter table genotype_marker
  add constraint (foreign key (genomrkr_dad_zygocity)
  references zygocity constraint 
	genomrkr_dad_zygocity_foreign_key);

alter table genotype_marker
  add constraint (foreign key (genomrkr_mom_zygocity)
  references zygocity constraint 
	genomrkr_mom_zygocity_foreign_key);

alter table genotype_marker
  add constraint primary key (genomrkr_zdb_id)
  constraint genotype_marker_primary_key ;

alter table genotype_marker
  add constraint unique (genomrkr_geno_zdb_id, genomrkr_mrkr_zdb_id)
  constraint genotype_marker_alternate_key ;

--took out ODC from genotype according to 6/7 frodo meeting

--alter table genotype_marker
--  add constraint (foreign key (genomrkr_geno_zdb_id) 
--    references genotype constraint
--    genomrkr_geno_foreign_key);

--! took out ODC from active data to genotype_marker.

--alter table genotype_marker
 --  add constraint (foreign key (genomrkr_zdb_id)
 --  references zdb_active_data on delete cascade constraint
 --  genomrkr_zdb_active_data_foreign_key);

alter table genotype_marker
   add constraint (foreign key (genomrkr_mrkr_zdb_id)
   references marker constraint
   genomrkr_mrkr_foreign_key_odc);

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('GENOMRKR', '11/15/2005','1','','genotype_marker', 
	  'genomrkr_zdb_id', 't','f', '2');

commit work ;