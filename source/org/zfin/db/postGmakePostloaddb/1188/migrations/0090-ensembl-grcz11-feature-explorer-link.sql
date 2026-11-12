--liquibase formatted sql

--changeset rtaylor:ensembl-grcz11-feature-explorer-link
--comment Point Ensembl(GRCz11) gene links at the Ensembl feature explorer instead of the retired geneview page

UPDATE foreign_db
   SET fdb_db_query = 'https://www.ensembl.org/feature-explorer/GCA_000002035.4/gene:',
       fdb_url_suffix = '?view=transcripts'
 WHERE fdb_db_name = 'Ensembl(GRCz11)';

--rollback UPDATE foreign_db SET fdb_db_query = 'https://www.ensembl.org/Danio_rerio/geneview?gene=', fdb_url_suffix = NULL WHERE fdb_db_name = 'Ensembl(GRCz11)';
