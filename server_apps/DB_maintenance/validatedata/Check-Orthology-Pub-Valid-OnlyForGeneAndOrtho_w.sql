SELECT recattrib_data_zdb_id
FROM   record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
       AND recattrib_data_zdb_id NOT IN (select ortho_zdb_id from ortholog)
       AND recattrib_data_zdb_id NOT IN (select ortho_zebrafish_gene_zdb_id from ortholog);

