begin work ;

delete from ensdar_mapping;

\copy from sangerMutantData3.unl
 insert into ensdar_mapping;

commit work;
