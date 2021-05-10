--liquibase formated sql
--changeset kschaper:INF-1527

delete from webPages 
where id in ('aa-markerselect.apg',
             'aa-msegselect.apg',
             'aa-newmrkrselect.apg',
             'aa-searchform.apg');

