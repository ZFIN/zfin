--liquibase formatted sql
--changestat sierra:14874

alter table pub_tracking_location
 add ptl_location_definition varchar(255);

update pub_tracking_location
 set ptl_location_definition = 'Bin 1 is highest priority papers - new genes/markers/clones/features/STRs, etc...'
where ptl_pk_id = '1';

update pub_tracking_location
 set ptl_location_definition = 'Lower priority papers without new primary data objects, new phenotype, orthology nor expression and not toxicology.'
where ptl_pk_id = '2';

update pub_tracking_location
 set ptl_location_definition = 'Toxicology papers'
where ptl_pk_id = '6';

update pub_tracking_location
 set ptl_location_definition = 'Papers with new phenotype data.'
where ptl_pk_id = '3';

update pub_tracking_location
 set ptl_location_definition = 'Papers with new expression data.'
where ptl_pk_id = '4';

update pub_tracking_location
 set ptl_location_definition = 'Papers with new orthology data.'
where ptl_pk_id = '5';

update pub_tracking_location
 set ptl_location_definition = 'Papers likely destined for Bin 1.'
where ptl_pk_id = '7';

update pub_tracking_location
 set ptl_location_definition = 'Papers likely destined for Bin 2, new orthology, phenotype or expression bins.'
where ptl_pk_id = '8';

update pub_tracking_location
 set ptl_location_definition = 'Papers likely destined for Bin 3, likely toxicology papers.'
where ptl_pk_id = '9';

alter table pub_tracking_location
 modify (ptl_location_definition varchar(255) not null constraint ptl_location_definition_not_null);
