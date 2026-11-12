--liquibase formatted sql

--changeset rtaylor:clear-dead-vega-urls
--comment Vega (vega.sanger.ac.uk) is retired; clear the URL prefix so these accessions display as plain text instead of dead links

UPDATE foreign_db
   SET fdb_db_query = ''
 WHERE fdb_db_query LIKE 'http://vega.sanger.ac.uk/%';

--rollback UPDATE foreign_db SET fdb_db_query = 'http://vega.sanger.ac.uk/Danio_rerio/geneview?gene=' WHERE fdb_db_name = 'VEGA';
--rollback UPDATE foreign_db SET fdb_db_query = 'http://vega.sanger.ac.uk/Danio_rerio/contigview?region=' WHERE fdb_db_name = 'VEGA_Clone';
--rollback UPDATE foreign_db SET fdb_db_query = 'http://vega.sanger.ac.uk/Danio_rerio/transview?db=core;transcript=' WHERE fdb_db_name = 'Vega_Trans';
--rollback UPDATE foreign_db SET fdb_db_query = 'http://vega.sanger.ac.uk/Danio_rerio/protview?db=core;peptide=' WHERE fdb_db_name = 'VEGAPROT';
