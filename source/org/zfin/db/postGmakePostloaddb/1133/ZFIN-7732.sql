--liquibase formatted sql
--changeset cmpich:ZFIN-7732


-- cleanup data: set expression column to null if 'No_Data' from the original file
update fish_mirna_expression_data_temp set brain = null where brain = 'No_Data';
update fish_mirna_expression_data_temp set gills = null where gills = 'No_Data';
update fish_mirna_expression_data_temp set heart = null where heart = 'No_Data';
update fish_mirna_expression_data_temp set muscle = null where muscle = 'No_Data';
update fish_mirna_expression_data_temp set intestine = null where intestine = 'No_Data';
update fish_mirna_expression_data_temp set liver = null where liver = 'No_Data';
update fish_mirna_expression_data_temp set ovary = null where ovary = 'No_Data';
update fish_mirna_expression_data_temp set testis = null where testis = 'No_Data';
update fish_mirna_expression_data_temp set head_kidney = null where head_kidney = 'No_Data';
update fish_mirna_expression_data_temp set spleen = null where spleen = 'No_Data';

-- turn '0' or '0.00' into null
update fish_mirna_expression_data_temp set brain = null where brain = '0' or brain = '0.00' or brain = '0.0';
update fish_mirna_expression_data_temp set heart = null where heart = '0' or heart = '0.00' or heart = '0.0';
update fish_mirna_expression_data_temp set ovary = null where ovary = '0' or ovary = '0.00' or ovary = '0.0';
update fish_mirna_expression_data_temp set testis = null where testis = '0' or testis = '0.00' or testis = '0.0';


-- table to hold gene_zdb_id and fish_mirna_id
create table expressed_gene_fish_mirna
(
    gene_zdb_id VARCHAR(100) NOT NULL,
    fish_mirna_id VARCHAR(100) NOT NULL
);

insert into expressed_gene_fish_mirna
select distinct dblink_linked_recid, dblink_acc_num from db_link, fish_mirna_expression_data_temp where exists(
    select * from genes_with_expression_temp where dblink_linked_recid = gene_id
                                                  ) and
    dblink_acc_num like '%'||fish_mirna_id||'%' and
    dblink_info = 'imported from Fish miRNA Expression' ;


-- select * from fish_experiment where genox_fish_zdb_id = 'ZDB-FISH-150901-27842' and genox_exp_zdb_id = 'ZDB-EXP-041102-1';

-- generate all expression_experiment2 XPAT ids.
create table pre_expat_temp
(
    pre_gene_zdb_id    varchar(50),
    pre_xpat_zdb_id varchar(50) not null
);

insert into pre_expat_temp (pre_gene_zdb_id, pre_xpat_zdb_id)
select gene_zdb_id,get_id('XPAT')
from expressed_gene_fish_mirna;

insert into zdb_active_data select pre_xpat_zdb_id from pre_expat_temp;

insert into expression_experiment2 (xpatex_zdb_id, xpatex_assay_name, xpatex_gene_zdb_id,
                                    xpatex_genox_zdb_id, xpatex_atb_zdb_id, xpatex_source_zdb_id)
select pre_xpat_zdb_id, 'RNA Seq', pre_gene_zdb_id,
        (select genox_zdb_id from fish_experiment where genox_fish_zdb_id = 'ZDB-FISH-150901-27842' and genox_exp_zdb_id = 'ZDB-EXP-041102-1'),
        null, 'ZDB-PUB-220126-55'
    from pre_expat_temp;


-- create expression_figure_stage
-- Fig. 3:  ZDB-FIG-220603-11

insert into expression_figure_stage (efs_xpatex_zdb_id, efs_fig_zdb_id, efs_start_stg_zdb_id, efs_end_stg_zdb_id)
select  pre_xpat_zdb_id, 'ZDB-FIG-220603-11','ZDB-STAGE-010723-39','ZDB-STAGE-010723-39'
from pre_expat_temp;

-- generate expression_results
-- brain ()
insert into expression_result2 (xpatres_efs_id, xpatres_expression_found, xpatres_superterm_zdb_id)
select efs_pk_id, 't', 'ZDB-TERM-100331-8'
from expression_figure_stage, pre_expat_temp, expressed_gene_fish_mirna as exp
where efs_xpatex_zdb_id = pre_xpat_zdb_id
AND pre_gene_zdb_id = exp.gene_zdb_id
AND exists (select * from fish_mirna_expression_data_temp where
    brain is not null AND
    exp.fish_mirna_id ~ fish_mirna_id                                                            )
;

-- heart ()
insert into expression_result2 (xpatres_efs_id, xpatres_expression_found, xpatres_superterm_zdb_id)
select efs_pk_id, 't', 'ZDB-TERM-100331-107'
from expression_figure_stage, pre_expat_temp, expressed_gene_fish_mirna as exp
where efs_xpatex_zdb_id = pre_xpat_zdb_id
AND pre_gene_zdb_id = exp.gene_zdb_id
AND exists (select * from fish_mirna_expression_data_temp where
    heart is not null AND
    exp.fish_mirna_id ~ fish_mirna_id                                                            )
;

-- ovary ()
insert into expression_result2 (xpatres_efs_id, xpatres_expression_found, xpatres_superterm_zdb_id)
select efs_pk_id, 't', 'ZDB-TERM-100331-384'
from expression_figure_stage, pre_expat_temp, expressed_gene_fish_mirna as exp
where efs_xpatex_zdb_id = pre_xpat_zdb_id
AND pre_gene_zdb_id = exp.gene_zdb_id
AND exists (select * from fish_mirna_expression_data_temp where
    ovary is not null AND
    exp.fish_mirna_id ~ fish_mirna_id                                                            )
;

-- testis ()
insert into expression_result2 (xpatres_efs_id, xpatres_expression_found, xpatres_superterm_zdb_id)
select efs_pk_id, 't', 'ZDB-TERM-100331-570'
from expression_figure_stage, pre_expat_temp, expressed_gene_fish_mirna as exp
where efs_xpatex_zdb_id = pre_xpat_zdb_id
AND pre_gene_zdb_id = exp.gene_zdb_id
AND exists (select * from fish_mirna_expression_data_temp where
    testis is not null AND
    exp.fish_mirna_id ~ fish_mirna_id                                                            )
;

drop table genes_with_expression_temp;

drop table fish_mirna_expression_data_temp;

drop table pre_expat_temp;

