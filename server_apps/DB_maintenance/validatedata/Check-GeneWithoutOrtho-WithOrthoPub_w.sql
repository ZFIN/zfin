SELECT recattrib_data_zdb_id,mrkr_abbrev,mrkr_name
FROM   record_attribution,marker
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
       AND recattrib_data_zdb_id NOT IN (select ortho_zebrafish_gene_zdb_id from ortholog)
       AND recattrib_data_zdb_id=mrkr_zdb_id;

