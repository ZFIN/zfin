begin work;

delete from ensdar_mapping;

\copy ensdar_mapping from 'mart_exportName1.txt';

commit work;
