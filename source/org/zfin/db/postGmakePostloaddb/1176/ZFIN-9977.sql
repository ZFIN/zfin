--liquibase formatted sql
--changeset rtaylor:ZFIN-9977

-- Requirements:
-- ZFIN:ZDB-GENE-011205-3 mt-rnr1 -->should be type rRNA, keep name because it is mitochondrial 
-- ZFIN:ZDB-GENE-011205-4 mt-rnr2 -->should be type rRNA, keep name because it is mitochondrial
-- ZFIN:ZDB-GENE-150915-1 scarna1 → should be type snoRNA (we don’t have its child scaRNA), also update nomenclature to sno.scarna1
-- ZFIN:ZDB-GENE-150916-1 scarna8 -->scarna1 → should be type snoRNA (we don’t have its child scaRNA), also update nomenclature to sno.scarna1
-- ZFIN:ZDB-GENE-080410-2 terc → should by type ncRNA (we don’t have its child telomerase_RNA_gene)
-- ZFIN:ZDB-GENE-090929-312 mir733 -->should be type MIRNAG
-- ZFIN:ZDB-GENE-150109-6 vtrna2 -->should be type ncRNA
-- ZFIN:ZDB-GENE-150109-5 vtrna1 -->should be type ncRNA
-- ZFIN:ZDB-GENE-090929-315 rny2 -->should be type ncRNA
-- ZFIN:ZDB-GENE-111201-3  rny1 -->should be type ncRNA
-- ZFIN:ZDB-GENE-191022-2  linc.1200 -->should be type lincRNA
-- ZFIN:ZDB-GENE-250515-1  lnc.nlrp3l.itgb1b.2-->should be type lncRNA
-- ZFIN:ZDB-GENE-100922-247 linc.2154-->hould be type lincRNA

-- Marker types: ATB BAC BAC_END BINDSITE BR CDNA CNE CPGISLAND CRISPR DNAMO EBS EFG EMR ENHANCER EREGION EST ETCONSTRCT
-- FOSMID GENE GENEFAMILY GENEP GTCONSTRCT HISTBS HMR LCR LIGANDBS LINCRNAG LNCRNAG MDNAB MIRNAG MRPHLNO MUTANT NCBS
-- NCCR NCRNAG NUCMO PAC PAC_END PIRNAG PROMOTER PROTBS PTCONSTRCT RAPD RNAMO RR RRNAG SCRNAG SNORNAG SNP SRPRNAG SSLP
-- STS TALEN TFBS TGCONSTRCT TLNRR TRNAG TRR TSCRIPT


select 'Converted ZDB-GENE-011205-3 to ' || convert_marker_type('ZDB-GENE-011205-3', 'RRNAG'),
 'Converted ZDB-GENE-011205-4 to '   || convert_marker_type('ZDB-GENE-011205-4', 'RRNAG'),
 'Converted ZDB-GENE-150915-1 to '   || convert_marker_type('ZDB-GENE-150915-1', 'SNORNAG'),
 'Converted ZDB-GENE-150916-1 to '   || convert_marker_type('ZDB-GENE-150916-1', 'SNORNAG'),
 'Converted ZDB-GENE-080410-2 to '   || convert_marker_type('ZDB-GENE-080410-2', 'NCRNAG'),
 'Converted ZDB-GENE-090929-312 to ' || convert_marker_type('ZDB-GENE-090929-312', 'MIRNAG'),
 'Converted ZDB-GENE-150109-6 to '   || convert_marker_type('ZDB-GENE-150109-6', 'NCRNAG'),
 'Converted ZDB-GENE-150109-5 to '   || convert_marker_type('ZDB-GENE-150109-5', 'NCRNAG'),
 'Converted ZDB-GENE-090929-315 to ' || convert_marker_type('ZDB-GENE-090929-315', 'NCRNAG'),
 'Converted ZDB-GENE-111201-3 to '   || convert_marker_type('ZDB-GENE-111201-3', 'NCRNAG'),
 'Converted ZDB-GENE-191022-2 to '   || convert_marker_type('ZDB-GENE-191022-2', 'LINCRNAG'),
 'Converted ZDB-GENE-250515-1 to '   || convert_marker_type('ZDB-GENE-250515-1', 'LNCRNAG'),
 'Converted ZDB-GENE-100922-247 to ' || convert_marker_type('ZDB-GENE-100922-247', 'LINCRNAG');

