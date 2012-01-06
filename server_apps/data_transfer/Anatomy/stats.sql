
begin work;


create temp table anat_temp 
  (
    at_anatitem_zdb_id varchar(50),
    at_anatitem_obo_id varchar(20),
    at_anatitem_name varchar(80),
    at_anatitem_start_stg_zdb_id varchar(50) ,
    at_anatitem_end_stg_zdb_id varchar(50) ,
    at_anatitem_name_order varchar(100),
    at_anatitem_definition lvarchar,
    at_anatitem_description lvarchar(28000),
    at_anatitem_name_lower varchar(80) ,
    at_anatitem_is_cell boolean ,
    at_anatitem_is_obsolete boolean 
  );


load from "anat_temp.unl" insert into anat_temp;



create index at_anatitem_zdb_id_index on 
    anat_temp (at_anatitem_zdb_id) using btree ;

       
update statistics medium for table anat_temp;

!echo "Definitions updated"
select count(*) from anat_temp,anatomy_item
where anatitem_zdb_id = at_anatitem_zdb_id
  and anatitem_definition != at_anatitem_definition
  and anatitem_zdb_id not like "%-111025-%"
;

!echo "Definitions updated"
select count(*) from anat_temp,anatomy_item
where anatitem_zdb_id = at_anatitem_zdb_id
  and anatitem_definition is not null 
  and at_anatitem_definition is null;


select count(*) from anatomy_item
where anatitem_zdb_id like "%-111025-%"
  and anatitem_definition != ""
  and anatitem_definition is not null;

!echo "Stages updated"
select count(*) from anat_temp,anatomy_item
where anatitem_zdb_id = at_anatitem_zdb_id
  and anatitem_start_stg_zdb_id != at_anatitem_start_stg_zdb_id;

select count(*) from anat_temp,anatomy_item
where anatitem_zdb_id = at_anatitem_zdb_id
 and anatitem_end_stg_zdb_id != at_anatitem_end_stg_zdb_id;


select count(*) from anat_temp
where at_anatitem_definition is  null
  or  at_anatitem_definition = "";

select count(*) from anatomy_item
where anatitem_definition is not null
  and anatitem_zdb_id like "%-111025-%"
  or  anatitem_definition = "";
  
  
--commit work;