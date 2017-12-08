begin work;

alter table publication alter column pub_abstract type text using pub_abstract::text;

alter table person alter column nonzf_pubs type text using nonzf_pubs::text;

commit work;


