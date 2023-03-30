--liquibase formatted sql
--changeset cmpich:ZFIN-8518.sql

insert into zdb_active_data
values ('ZDB-GENE-101020-1');

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_comments, mrkr_abbrev, mrkr_type, mrkr_owner, mrkr_name_order)
select 'ZDB-GENE-101020-1', mrkr_name || '_temp', mrkr_comments, mrkr_abbrev || '_temp', 'GENE', mrkr_owner, mrkr_name_order
from marker
where mrkr_zdb_id = 'ZDB-GENEP-101020-1';

update snp_download
set snpd_mrkr_zdb_id = 'ZDB-GENE-101020-1'
where snpd_mrkr_zdb_id = 'ZDB-GENEP-101020-1';

update data_alias
set dalias_data_zdb_id = 'ZDB-GENE-101020-1'
where dalias_data_zdb_id = 'ZDB-GENEP-101020-1';

update db_link
set dblink_linked_recid = 'ZDB-GENE-101020-1'
where dblink_linked_recid = 'ZDB-GENEP-101020-1';

update ui.publication_expression_display
set ped_gene_zdb_id = 'ZDB-GENE-101020-1'
where ped_gene_zdb_id = 'ZDB-GENEP-101020-1';

update expression_experiment2
set xpatex_gene_zdb_id = 'ZDB-GENE-101020-1'
where xpatex_gene_zdb_id = 'ZDB-GENEP-101020-1';

update expression_experiment
set xpatex_gene_zdb_id = 'ZDB-GENE-101020-1'
where xpatex_gene_zdb_id = 'ZDB-GENEP-101020-1';

delete
from zdb_active_data
where zactvd_zdb_id = 'ZDB-GENEP-101020-1';

update marker
set mrkr_name   = trim(trailing '_temp' from mrkr_name),
    mrkr_abbrev = trim(trailing '_temp' from mrkr_abbrev)
where mrkr_zdb_id = 'ZDB-GENE-101020-1';

