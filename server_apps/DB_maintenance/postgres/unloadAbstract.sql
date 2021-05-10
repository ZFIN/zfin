begin work;

select lotofile(pub_abstract, '/tmp/abstracts/'||zdb_id, 'server')
 from publication
 where pub_abstract is not null;

select lotofile(nonzf_pubs, '/tmp/nonzf_pubs/'||zdb_id, 'server')
 from person
 where nonzf_pubs is not null;

commit work;
