begin work ;

delete from ensdar_mapping;

load from sangerMutantData3.unl
 insert into ensdar_mapping;

commit work;