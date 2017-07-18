begin work;

alter table publication alter column pub_abstract type bytea using pub_abstract::bytea;

alter table person alter column nonzf_pubs type bytea using nonzf_pubs:bytea;

commit work;


