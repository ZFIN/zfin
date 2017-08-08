create or replace function regen_panelcount()
returns text as $success$

-- Creates the panel_count table, a fast search table used to quickly
-- get counts for each marker type on each linkage group or panel.

-- DEBUGGING:  Uncomment the next two statements to turn on a debugging trace.
--             (and change the first one to point to your OWN dang directory!)
-- set debug file to '/tmp/debug-regen-panelcnt';
-- trace on;

-- Create all the new tables and views.
-- If an exception occurs here, drop all the newly-created tables
   
declare nrows integer;	
      
begin

  drop table if exists panel_count_new;

  create table panel_count_new
  	(
    	panelcnt_panel_zdb_id	text,
    	panelcnt_mrkr_type		varchar(10),
    	panelcnt_chromosome		varchar(2),
    	panelcnt_count		integer
      	not null
 	 );


     insert into panel_count_new
          select refcross_id, marker_type, mm_chromosome, count(*)
	    from mapped_marker
        group by refcross_id, mm_chromosome, marker_type;
	

      drop table panel_count;

      alter table panel_count_new rename to panel_count;

      create unique index panel_count_primary_key_index
  	on panel_count (panelcnt_panel_zdb_id, panelcnt_mrkr_type,
	panelcnt_chromosome);

      alter table panel_count
  	add constraint panel_count_primary_key
   	 primary key (panelcnt_panel_zdb_id, panelcnt_mrkr_type,
	panelcnt_chromosome);


     create index panelcnt_panel_zdb_id
  	on panel_count(panelcnt_panel_zdb_id);

     alter table panel_count
	  add constraint panelcnt_panel_zdb_id_foreign_key
    	foreign key (panelcnt_panel_zdb_id)
    	references panels
    	on delete cascade
;

     create index panelcnt_mrkr_type_index
  	on panel_count(panelcnt_mrkr_type);

    alter table panel_count
  	add constraint panelcnt_mrkr_type_foreign_key
    	foreign key (panelcnt_mrkr_type)
    	references marker_types
    	on delete cascade
;
    create index panelcnt_or_lg_index
  	on panel_count(panelcnt_chromosome)
  ;

    alter table panel_count
  	add constraint panelcnt_or_lg_foreign_key
   	 foreign key (panelcnt_chromosome)
    	references linkage_group
    	on delete cascade
;
    
 return 'success';
  return 'regen_panel_count() completed without error; success!';
  exception when raise_exception then
  	    return errorHint;
end;

$success$ LANGUAGE plpgsql
