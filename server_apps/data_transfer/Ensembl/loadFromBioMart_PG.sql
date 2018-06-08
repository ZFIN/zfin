begin work;

delete from ensdar_mapping;

\copy from mart_exportName1.txt
 insert into ensdar_mapping;

commit work;
