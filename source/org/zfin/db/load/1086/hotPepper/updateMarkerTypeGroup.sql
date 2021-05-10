--liquibase formated sql
--changeset kschaper:updateMarkerTypeGroup

alter table marker_type_group
add (mtgrp_searchable boolean default 'f', mtgrp_display_name varchar(80));


