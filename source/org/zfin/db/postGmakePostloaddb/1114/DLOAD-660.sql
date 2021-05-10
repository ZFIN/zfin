--liquibase formatted sql
--changeset pm:DLOAD-660


drop table if exists cnechr1;
update cnechr set cne='nccr.'||cne ;
create table  cnechr1 (
 cneid text not null,
        assembly text not null,
           chromosome text not null,
            stlocation integer,endlocation integer,evidence text,pub text) ;

 insert into cnechr1
 select distinct  mrkr_zdb_id,'Zv9',chr,startchr,chrend,'ZDB-TERM-170419-250','ZDB-PUB-170214-158' from cnechr , marker where  trim(cne)=trim(mrkr_Abbrev) ;





update sequence_feature_chromosome_location
set sfcl_start_position=(select stlocation from cnechr1 where cneid=sfcl_feature_zdb_id and sfcl_feature_zdb_id like '%NCCR%' and sfcl_start_position is null)
 from cnechr1 where cneid=sfcl_feature_zdb_id;

 update sequence_feature_chromosome_location
set sfcl_end_position=(select endlocation from cnechr1 where cneid=sfcl_feature_zdb_id and sfcl_feature_zdb_id like '%NCCR%' and sfcl_end_position is null)
 from cnechr1 where cneid=sfcl_feature_zdb_id;
 update sequence_feature_chromosome_location
set sfcl_assembly=(select assembly from cnechr1 where cneid=sfcl_feature_zdb_id and sfcl_feature_zdb_id like '%NCCR%' and assembly is null)
 from cnechr1 where cneid=sfcl_feature_zdb_id;




