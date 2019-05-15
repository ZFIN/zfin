begin work;

create temp table names (
    pubmedid    text not null,
    zdbid       text  not null,
    lastname    text not null,
    middlename    text,
    firstname    text    
) ;

create index names_pid_idx on names(pubmedid);
create index names_zdbid_idx on names(zdbid);

copy names from '<!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/authors' (delimiter '|');

insert into pubmed_publication_author(ppa_pubmed_id, ppa_publication_zdb_id, ppa_author_last_name, ppa_author_middle_name, ppa_author_first_name)
  select pubmedid, zdbid, lastname, middlename, firstname
    from names;

--commit work;
rollback work;

