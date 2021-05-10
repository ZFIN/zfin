begin work ;

create temp table tmp_syns (human_gene_id text,
                        synonym text);

\copy tmp_syns from '<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/human_gene_synonyms.txt' delimiter E'\t';

delete from tmp_syns
 where not exists (select 'x' from ncbi_ortholog
                          where noi_ncbi_gene_id = human_gene_id);

insert into ncbi_ortholog_alias (noa_ncbi_gene_id, noa_alias)
  select distinct human_gene_id, synonym 
    from tmp_syns
   where not exists (select 'x' from ncbi_ortholog_alias
                            where noa_ncbi_gene_id = human_gene_id
                            and noa_alias = synonym);

--rollback work;

commit work ;
