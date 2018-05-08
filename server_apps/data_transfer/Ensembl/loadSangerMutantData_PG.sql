begin work ;

delete from ensdar_mapping;

\copy ensdar_mapping from 'sangerMutantData3.unl';

commit work;
