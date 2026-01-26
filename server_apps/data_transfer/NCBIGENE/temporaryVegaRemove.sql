-- This file is temporary and will no longer need to be run after the first run of 1178
-- It removes db_links that were added based on Vega gene IDs where we have other evidence
-- (RNA seq or Ensembl gene ID match) to retain the NCBI gene ID

-- TODO: Remove this file and references to it after the January 2026 NCBI gene load (needs to run only once)
drop table if exists vega_based_ncbi_gene_dblinks_to_keep;
create temp table vega_based_ncbi_gene_dblinks_to_keep as
select * from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1' and dblink_zdb_id in
   (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id = 'ZDB-PUB-130725-2')
                        and (dblink_linked_recid, dblink_acc_num) in (
                          ('ZDB-GENE-030131-8582', '569464'),
                          ('ZDB-GENE-030131-8643', '795300'),
                          ('ZDB-GENE-030131-9307', '100149164'),
                          ('ZDB-GENE-030722-8', '368227'),
                          ('ZDB-GENE-030728-1', '562687'),
                          ('ZDB-GENE-040426-1655', '393671'),
                          ('ZDB-GENE-040724-174', '562460'),
                          ('ZDB-GENE-041111-14', '100001422'),
                          ('ZDB-GENE-041210-81', '559029'),
                          ('ZDB-GENE-050208-539', '100034562'),
                          ('ZDB-GENE-050419-164', '568211'),
                          ('ZDB-GENE-050420-404', '108183495'),
                          ('ZDB-GENE-050609-39', '108179154'),
                          ('ZDB-GENE-060413-21', '100150617'),
                          ('ZDB-GENE-060414-1', '100001562'),
                          ('ZDB-GENE-060414-8', '103911778'),
                          ('ZDB-GENE-060503-18', '108183944'),
                          ('ZDB-GENE-060503-234', '100034575'),
                          ('ZDB-GENE-060503-448', '100334522'),
                          ('ZDB-GENE-060503-861', '561253'),
                          ('ZDB-GENE-060526-316', '100317041'),
                          ('ZDB-GENE-060526-87', '108190102'),
                          ('ZDB-GENE-060526-96', '108190101'),
                          ('ZDB-GENE-060531-15', '100005375'),
                          ('ZDB-GENE-060531-80', '100125912'),
                          ('ZDB-GENE-061207-43', '557052'),
                          ('ZDB-GENE-070308-1', '793783'),
                          ('ZDB-GENE-070424-201', '100002902'),
                          ('ZDB-GENE-070705-202', '100006019'),
                          ('ZDB-GENE-070705-462', '562264'),
                          ('ZDB-GENE-070705-505', '561319'),
                          ('ZDB-GENE-070806-10', '100148683'),
                          ('ZDB-GENE-070806-5', '100150748'),
                          ('ZDB-GENE-070806-60', '100861451'),
                          ('ZDB-GENE-070806-75', '100148707'),
                          ('ZDB-GENE-070806-76', '100150812'),
                          ('ZDB-GENE-070806-97', '100151063'),
                          ('ZDB-GENE-070806-98', '100150394'),
                          ('ZDB-GENE-070912-333', '101886862'),
                          ('ZDB-GENE-070912-367', '569635'),
                          ('ZDB-GENE-070912-482', '566034'),
                          ('ZDB-GENE-070912-537', '100007494'),
                          ('ZDB-GENE-071003-2', '101886662'),
                          ('ZDB-GENE-080215-13', '563697'),
                          ('ZDB-GENE-081028-29', '560704'),
                          ('ZDB-GENE-081028-4', '108192014'),
                          ('ZDB-GENE-081031-102', '335412'),
                          ('ZDB-GENE-081031-21', '794765'),
                          ('ZDB-GENE-081031-31', '795109'),
                          ('ZDB-GENE-081031-55', '100149018'),
                          ('ZDB-GENE-081103-26', '100331576'),
                          ('ZDB-GENE-081104-193', '558403'),
                          ('ZDB-GENE-081104-194', '100000984'),
                          ('ZDB-GENE-081104-453', '798129'),
                          ('ZDB-GENE-081205-1', '100004011'),
                          ('ZDB-GENE-090311-33', '100149418'),
                          ('ZDB-GENE-090312-33', '795959'),
                          ('ZDB-GENE-090313-119', '101883039'),
                          ('ZDB-GENE-090313-122', '100148235'),
                          ('ZDB-GENE-090313-126', '565426'),
                          ('ZDB-GENE-090708-1', '797297'),
                          ('ZDB-GENE-091116-16', '108179083'),
                          ('ZDB-GENE-091116-75', '108191519'),
                          ('ZDB-GENE-091204-268', '101886051'),
                          ('ZDB-GENE-100203-1', '559568'),
                          ('ZDB-GENE-100922-138', '100329524'),
                          ('ZDB-GENE-110208-3', '561766'),
                          ('ZDB-GENE-110408-28', '335430'),
                          ('ZDB-GENE-110411-113', '571108'),
                          ('ZDB-GENE-110411-142', '101886753'),
                          ('ZDB-GENE-110411-199', '100002907'),
                          ('ZDB-GENE-110411-250', '108190651'),
                          ('ZDB-GENE-110411-90', '100149596'),
                          ('ZDB-GENE-110913-63', '108179204'),
                          ('ZDB-GENE-110914-122', '101883146'),
                          ('ZDB-GENE-110914-243', '100150046'),
                          ('ZDB-GENE-111109-3', '100002104'),
                          ('ZDB-GENE-120214-21', '100334961'),
                          ('ZDB-GENE-120215-128', '100330109'),
                          ('ZDB-GENE-120215-213', '101882887'),
                          ('ZDB-GENE-120215-238', '559232'),
                          ('ZDB-GENE-120703-37', '103910745'),
                          ('ZDB-GENE-120709-63', '108183853'),
                          ('ZDB-GENE-121214-148', '101882533'),
                          ('ZDB-GENE-121214-189', '100331025'),
                          ('ZDB-GENE-121214-197', '108181627'),
                          ('ZDB-GENE-121214-21', '108190743'),
                          ('ZDB-GENE-121214-273', '103911292'),
                          ('ZDB-GENE-130530-2', '569265'),
                          ('ZDB-GENE-130530-564', '793327'),
                          ('ZDB-GENE-130530-691', '568908'),
                          ('ZDB-GENE-130530-789', '100331898'),
                          ('ZDB-GENE-130531-13', '570632'),
                          ('ZDB-GENE-130603-60', '101884418'),
                          ('ZDB-GENE-131119-94', '100333005'),
                          ('ZDB-GENE-131120-4', '559090'),
                          ('ZDB-GENE-131121-138', '798606'),
                          ('ZDB-GENE-131121-178', '100334411'),
                          ('ZDB-GENE-131121-217', '100534835'),
                          ('ZDB-GENE-131121-329', '100535085'),
                          ('ZDB-GENE-131121-77', '559220'),
                          ('ZDB-GENE-131122-15', '100332150'),
                          ('ZDB-GENE-131122-86', '796869'),
                          ('ZDB-GENE-131126-22', '108179093'),
                          ('ZDB-GENE-131127-102', '563177'),
                          ('ZDB-GENE-131127-183', '799658'),
                          ('ZDB-GENE-131127-209', '108181225'),
                          ('ZDB-GENE-131127-395', '101884935'),
                          ('ZDB-GENE-131127-66', '562992'),
                          ('ZDB-GENE-131127-95', '100331162'),
                          ('ZDB-GENE-141210-7', '553517'),
                          ('ZDB-GENE-141211-48', '798805'),
                          ('ZDB-GENE-141212-278', '100334678'),
                          ('ZDB-GENE-141212-295', '570842'),
                          ('ZDB-GENE-141212-297', '792792'),
                          ('ZDB-GENE-141215-37', '799164'),
                          ('ZDB-GENE-141215-61', '100537725'),
                          ('ZDB-GENE-141216-26', '108191591'),
                          ('ZDB-GENE-141216-28', '108191774'),
                          ('ZDB-GENE-141216-31', '100005062'),
                          ('ZDB-GENE-141216-32', '573077'),
                          ('ZDB-GENE-141216-348', '108192171'),
                          ('ZDB-GENE-141216-349', '101885156'),
                          ('ZDB-GENE-141216-39', '100537403'),
                          ('ZDB-GENE-141216-395', '100334146'),
                          ('ZDB-GENE-141216-65', '799882'),
                          ('ZDB-GENE-141219-10', '100000241'),
                          ('ZDB-GENE-141219-43', '101886927'),
                          ('ZDB-GENE-141222-1', '101883793'),
                          ('ZDB-GENE-141222-70', '103910426'),
                          ('ZDB-GENE-151216-1', '100004988'),
                          ('ZDB-GENE-160113-145', '101882779'),
                          ('ZDB-GENE-160113-30', '100000574'),
                          ('ZDB-GENE-160113-62', '108192189'),
                          ('ZDB-GENE-160113-80', '101884661'),
                          ('ZDB-GENE-160728-32', '100137609'),
                          ('ZDB-GENE-160728-66', '100329858'),
                          ('ZDB-GENE-161017-131', '100535375'),
                          ('ZDB-GENE-161017-152', '103910258'),
                          ('ZDB-GENE-161017-64', '103908798'),
                          ('ZDB-GENE-990415-193', '30358'),
                          ('ZDB-GENE-990715-17', '30683')
        );

--  !!!!! RMOVE BEFORE COMMIT
-- Just for testing: let's add this one back in the keep list.
-- This way we can simulate the behavior of what happens when one of our
-- preserved vega-mapped db_links later gains RNA evidence in NCBI (or ensembl match).
insert into vega_based_ncbi_gene_dblinks_to_keep
select * from db_link where
    (dblink_linked_recid, dblink_acc_num) in (('ZDB-GENE-000329-17', '58062'));
--
-- END RMOVE BEFORE COMMIT

create temp table vega_based_ncbi_gene_dblinks_to_delete as
select * from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1' and dblink_zdb_id in
       (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id = 'ZDB-PUB-130725-2')
                        and (dblink_zdb_id) not in (
        select dblink_zdb_id from vega_based_ncbi_gene_dblinks_to_keep
    );

create temp table vega_based_rna_dblinks_to_delete as
select * from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-38' and dblink_zdb_id in
        (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id = 'ZDB-PUB-130725-2')
                        and dblink_linked_recid not in (
        select dblink_linked_recid from vega_based_ncbi_gene_dblinks_to_keep
    )
;


delete from zdb_active_data where zactvd_zdb_id in (
    select dblink_zdb_id from vega_based_ncbi_gene_dblinks_to_delete
    union
    select dblink_zdb_id from vega_based_rna_dblinks_to_delete
);