--liquibase formatted sql
--changeset sierra:notNullAdditions

--alter table marker_type_Group
-- modify (mtgrp_searchable boolean default 'f' not null constraint mtgrp_searchable_not_null);

--alter table marker_type_Group
-- modify (mtgrp_display_name varchar(80) not null constraint mtgrp_display_name_not_null);
