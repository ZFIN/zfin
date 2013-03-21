begin work;

delete from ensdar_mapping;

load from mart_exportName1.txt
 insert into ensdar_mapping;

commit work;