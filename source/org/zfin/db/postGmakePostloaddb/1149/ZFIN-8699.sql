--liquibase formatted sql
--changeset rtaylor:ZFIN-8699.sql

-- use the same significance for 'mutation involves' as 'is allele of'
INSERT INTO genotype_component_significance (gcs_mrkr_type, gcs_ftr_type, gcs_fmrel_type, gcs_significance) (
    SELECT
        gcs_mrkr_type,
        gcs_ftr_type,
        'mutation involves' AS gcs_fmrel_type,
        gcs_significance
    FROM
        genotype_component_significance
    WHERE
        gcs_fmrel_type = 'is allele of');

-- fixes for genotypes where we don't want to use superscript for deficiencies
UPDATE genotype SET geno_display_name = 'Df(Chr12:ndr2)st5/st5' where geno_zdb_id = 'ZDB-GENO-010426-4';
UPDATE genotype SET geno_display_name = 'Df(Chr14:gpc4)st3/st3' where geno_zdb_id = 'ZDB-GENO-010426-6';
UPDATE genotype SET geno_display_name = 'Df(Chr11:bmp7a)p15/p15' where geno_zdb_id = 'ZDB-GENO-050216-2';
UPDATE genotype SET geno_display_name = 'Df(Chr11:bmp7a)p11/p11' where geno_zdb_id = 'ZDB-GENO-050216-3';
UPDATE genotype SET geno_display_name = 'Df(Chr16:fbxl4,coq3,pnrc2,pou3f1,rspo1,gnl2,dnali1,snip1)b644' where geno_zdb_id = 'ZDB-GENO-060125-1';
UPDATE genotype SET geno_display_name = 'ndr2<sup>tf219/tf219</sup>' where geno_zdb_id = 'ZDB-GENO-070712-5';
UPDATE genotype SET geno_display_name = 'sb15Tg' where geno_zdb_id = 'ZDB-GENO-080418-2';
UPDATE genotype SET geno_display_name = 'sb15Tg' where geno_zdb_id = 'ZDB-GENO-080713-1';
UPDATE genotype SET geno_display_name = 'shha<sup>tbx392/+</sup>' where geno_zdb_id = 'ZDB-GENO-080804-4';
UPDATE genotype SET geno_display_name = 'noto<sup>n1/n1</sup>' where geno_zdb_id = 'ZDB-GENO-100325-4';
UPDATE genotype SET geno_display_name = 'smo<sup>b641/b641</sup>' where geno_zdb_id = 'ZDB-GENO-100325-6';
UPDATE genotype SET geno_display_name = 'sqet332Et' where geno_zdb_id = 'ZDB-GENO-100601-2';
UPDATE genotype SET geno_display_name = 'Df(Chr11:bmp7a)p11' where geno_zdb_id = 'ZDB-GENO-110105-3';
UPDATE genotype SET geno_display_name = 'Df(Chr11:bmp7a)p15' where geno_zdb_id = 'ZDB-GENO-110105-4';
UPDATE genotype SET geno_display_name = 'sqet33Et/+' where geno_zdb_id = 'ZDB-GENO-130402-3';
UPDATE genotype SET geno_display_name = 'sqet33Et/+' where geno_zdb_id = 'ZDB-GENO-130402-5';
UPDATE genotype SET geno_display_name = 'cy13Tg' where geno_zdb_id = 'ZDB-GENO-131018-4';
UPDATE genotype SET geno_display_name = 'tdgf1<sup>m134/+</sup>' where geno_zdb_id = 'ZDB-GENO-140825-1';
UPDATE genotype SET geno_display_name = 'Df(Chr16:fbxl4,coq3,pnrc2,pou3f1,rspo1,gnl2,dnali1,snip1)b644/b644' where geno_zdb_id = 'ZDB-GENO-180208-11';
UPDATE genotype SET geno_display_name = 'pnrc2<sup>oz22/+</sup>; Df(Chr16:fbxl4,coq3,pnrc2,pou3f1,rspo1,gnl2,dnali1,snip1)b644/+' where geno_zdb_id = 'ZDB-GENO-180208-15';
UPDATE genotype SET geno_display_name = 'Df(Chr9:hoxd13a,hoxd12a,hoxd11a,hoxd10a,hoxd9a,hoxd4a,hoxd3a)sud116/sud116' where geno_zdb_id = 'ZDB-GENO-230615-11';
UPDATE genotype SET geno_display_name = 'Df(Chr3:hoxb13a,hoxb10a,hoxb9a,hoxb8a,hoxb7a,hoxb6a,hoxb5a,hoxb4a,hoxb3a,hoxb2a,hoxb1a)sud113/sud113' where geno_zdb_id = 'ZDB-GENO-230615-8';
UPDATE genotype SET geno_display_name = 'Df(Chr19:hoxa13a,hoxa11a,hoxa9a,hoxa5a,hoxa4a,hoxa1a)sud111/sud111' where geno_zdb_id = 'ZDB-GENO-230615-6';
UPDATE genotype SET geno_display_name = 'Df(Chr12:hoxb8b,hoxb6b,hoxb5b,hoxb1b)sud114/sud114' where geno_zdb_id = 'ZDB-GENO-230615-9';
UPDATE genotype SET geno_display_name = 'Df(Chr16:hoxa13b,hoxa11b,hoxa10b,hoxa9b,hoxa2b)sud112/sud112' where geno_zdb_id = 'ZDB-GENO-230615-7';
UPDATE genotype SET geno_display_name = 'Df(Chr23:hoxc13a,hoxc12a,hoxc11a,hoxc10a,hoxc9a,hoxc8a,hoxc6a,hoxc5a,hoxc4a,hoxc3a,hoxc1a)sud115/sud115' where geno_zdb_id = 'ZDB-GENO-230615-10';



-- fixes for genotypes after adding the rules for genotype_component_significance
update genotype SET geno_display_name = 'Df(Chr10:tdgf1)z1/z1' where geno_zdb_id = 'ZDB-GENO-001214-1';
update genotype SET geno_display_name = 'Df(Chr07)t4/t4' where geno_zdb_id = 'ZDB-GENO-010426-2';
update genotype SET geno_display_name = 'Df(Chr07)t4/+' where geno_zdb_id = 'ZDB-GENO-070209-80';
update genotype SET geno_display_name = 'Df(Chr07)t4/t4' where geno_zdb_id = 'ZDB-GENO-070406-1';
update genotype SET geno_display_name = 'ndr2<sup>tf219/tf219</sup>; Df(Chr07)t4/t4' where geno_zdb_id = 'ZDB-GENO-070712-5';
update genotype SET geno_display_name = 'Df(Chr07)t4/t4; sb15Tg' where geno_zdb_id = 'ZDB-GENO-080418-2';
update genotype SET geno_display_name = 'Df(Chr07)t4/t4' where geno_zdb_id = 'ZDB-GENO-080701-2';
update genotype SET geno_display_name = 'Df(Chr07)t4/t4; sb15Tg' where geno_zdb_id = 'ZDB-GENO-080713-1';
update genotype SET geno_display_name = 'shha<sup>tbx392/+</sup>; Df(Chr07)t4/+' where geno_zdb_id = 'ZDB-GENO-080804-4';
update genotype SET geno_display_name = 'Df(Chr07)t4/+' where geno_zdb_id = 'ZDB-GENO-080825-3';
update genotype SET geno_display_name = 'Df(Chr07)t4/+' where geno_zdb_id = 'ZDB-GENO-091027-1';
update genotype SET geno_display_name = 'Df(Chr07)t4/t4' where geno_zdb_id = 'ZDB-GENO-091109-1';
update genotype SET geno_display_name = 'Df(Chr12:hhex,ndr2)b16/b16; Df(Chr07)t4/t4' where geno_zdb_id = 'ZDB-GENO-100325-3';
update genotype SET geno_display_name = 'noto<sup>n1/n1</sup>; Df(Chr07)t4/t4' where geno_zdb_id = 'ZDB-GENO-100325-4';
update genotype SET geno_display_name = 'noto<sup>n1/n1</sup>; Df(Chr12:hhex,ndr2)b16/b16; Df(Chr07)t4/t4' where geno_zdb_id = 'ZDB-GENO-100325-5';
update genotype SET geno_display_name = 'smo<sup>b641/b641</sup>; Df(Chr07)t4/t4' where geno_zdb_id = 'ZDB-GENO-100325-6';
update genotype SET geno_display_name = 'Df(Chr10:tdgf1)z1' where geno_zdb_id = 'ZDB-GENO-100506-7';
update genotype SET geno_display_name = 'Df(Chr07)t4' where geno_zdb_id = 'ZDB-GENO-100524-2';
update genotype SET geno_display_name = 'Df(Chr07)t4/t4; sqet332Et' where geno_zdb_id = 'ZDB-GENO-100601-2';
update genotype SET geno_display_name = 'Df(Chr10:tdgf1)z1/z1' where geno_zdb_id = 'ZDB-GENO-101028-17';
update genotype SET geno_display_name = 'Df(Chr10:tdgf1)z1/z1; sqet33Et/+' where geno_zdb_id = 'ZDB-GENO-130402-3';
update genotype SET geno_display_name = 'Df(Chr07)t4/t4; sqet33Et/+' where geno_zdb_id = 'ZDB-GENO-130402-5';
update genotype SET geno_display_name = 'tdgf1<sup>m134/+</sup>; Df(Chr10:tdgf1)z1/+' where geno_zdb_id = 'ZDB-GENO-140825-1';
update genotype SET geno_display_name = 'Df(Chr10:tdgf1)z1/z1' where geno_zdb_id = 'ZDB-GENO-230117-2';

