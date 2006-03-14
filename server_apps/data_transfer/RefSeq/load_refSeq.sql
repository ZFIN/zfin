--RefSeq files are downloaded via ftp then parsed INTO informix loadable
--.unl files. RefSeq file formats are expected to be well formed per there
--documentation ON 05/31/02. Script will LOADnothing if RefSeq files are 
--not well formatted.
--
--
--2005/03 LocusLink is discontinued.
--Load files from Entrez Gene.
--ftp://ftp.ncbi.nih.gov/gene/DATA



begin work;    

lock table db_link in share mode;


  --=================================--
  -- Find Orthologue Name Mismatches --
  --=================================--
  
--HUMAN 
CREATE TEMP TABLE LL_HUMAN
  (
    llhs_ll_id		varchar (50) not null,
    llhs_hs_abbrev	varchar (20),
    llhs_hs_id		varchar (50) not null
  )
with no log;

!echo 'LOAD ll_hs_id.unl'
LOAD FROM 'll_hs_id.unl' INSERT INTO ll_human;

CREATE INDEX llhs_ll_id_index ON ll_human
    (llhs_ll_id) using btree;
CREATE INDEX llhs_abbrev_index ON ll_human
    (llhs_hs_abbrev) using btree;
    
UNLOAD to hs_ortho_abbrev_conflict.unl
SELECT mrkr_abbrev, ortho_abbrev, llhs_hs_abbrev
FROM ll_human, db_link, orthologue, marker, foreign_db_contains
WHERE llhs_ll_id = dblink_acc_num
  and llhs_hs_abbrev != ortho_abbrev

  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdbcont_organism_common_name = "Human"
  and fdbcont_fdbdt_data_type = "orthologue"
  and fdbcont_fdb_db_name = "Entrez Gene"
  and dblink_linked_recid = zdb_id
  and c_gene_id = mrkr_zdb_id
ORDER BY mrkr_abbrev;
  
--MOUSE
CREATE TEMP TABLE LL_MOUSE
  (
    llmm_ll_id		varchar (50) not null,
    llmm_mm_abbrev	varchar (50),
    llmm_mm_id		varchar (50) not null
  )
with no log;

!echo 'LOAD ll_mm_id.unl'
LOAD FROM 'll_mm_id.unl' INSERT INTO ll_mouse;

CREATE INDEX llmm_ll_id_index ON ll_mouse
    (llmm_ll_id) using btree;
CREATE INDEX llmm_abbrev_index ON ll_mouse
    (llmm_mm_abbrev) using btree;
    
UNLOAD to mm_ortho_abbrev_conflict.unl
SELECT mrkr_abbrev, ortho_abbrev, llmm_mm_abbrev
FROM ll_mouse, db_link, orthologue, marker, foreign_db_contains
WHERE llmm_ll_id = dblink_acc_num
--  and llmm_mm_abbrev != ortho_abbrev
  and llmm_mm_abbrev not like ortho_abbrev||'%'
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdbcont_organism_common_name = "Mouse"
  and fdbcont_fdbdt_data_type = "orthologue"
  and fdbcont_fdb_db_name = "Entrez Gene"
  and dblink_linked_recid = zdb_id
  and c_gene_id = mrkr_zdb_id
ORDER BY mrkr_abbrev;



  --=======================--
  -- LOAD INTO TEMP TABLES --
  --=======================--

--ZEBRAFISH 
CREATE TEMP TABLE LL_ZDB
  (
    llzdb_ll_id		varchar (50) not null,
    llzdb_zdb_id	varchar (50) not null
  )
with no log;

!echo 'LOAD LL_ID.UNL'
LOAD FROM 'll_id.unl' INSERT INTO ll_zdb;

CREATE INDEX llzdb_ll_id_index ON ll_zdb
    (llzdb_ll_id) using btree;
CREATE INDEX llzdb_zdb_id_index ON ll_zdb
    (llzdb_zdb_id) using btree;



--REFSEQ ACCESSION NUM--
CREATE TEMP TABLE REF_SEQ_ACC
  (
    refseq_ll		varchar (50) not null,
    refseq_nM_acc	varchar (50),
    refseq_nM_length	integer,
    refseq_nP_acc	varchar (50),
    refseq_nP_length	integer
  )
with no log;

!echo 'LOAD loc2ref.unl'
LOAD FROM 'loc2ref.unl' INSERT INTO ref_seq_acc;

CREATE INDEX refseq_ll_index ON ref_seq_acc
    (refseq_ll) using btree;
CREATE INDEX refseq_nM_acc_index ON ref_seq_acc
    (refseq_nM_acc) using btree;
CREATE INDEX refseq_nP_acc_index ON ref_seq_acc
    (refseq_nP_acc) using btree;
   

--GENBANK ACCESSION NUM--
CREATE TEMP TABLE GENBANK_ACC
  (
    gbacc_ll	varchar (50) not null,
    gbacc_acc	varchar (50) not null,
    gbacc_pept	varchar (50) not null
  )
with no log;

!echo 'LOAD loc2acc.unl'
LOAD FROM 'loc2acc.unl' INSERT INTO genbank_acc;

CREATE INDEX genbank_ll_index ON genbank_acc
    (gbacc_ll) using btree;
CREATE INDEX genbank_acc_index ON genbank_acc
    (gbacc_acc) using btree;
CREATE INDEX genbank_pept_index ON genbank_acc
    (gbacc_pept) using btree;
   

--ACCESSION LENGTH--
CREATE TEMP TABLE ACC_LENGTH
  (
    acclen_acc		varchar (20) not null,
    acclen_length	integer,
    acclen_max_len	boolean
  )
with no log;

CREATE INDEX acclen_acc_index ON acc_length
    (acclen_acc) using btree;
    
!echo 'LOAD loc2acclen.unl'
LOAD FROM 'loc2acclen.unl' INSERT INTO acc_length (acclen_acc, acclen_length);

CREATE INDEX acclen_length_index ON acc_length
    (acclen_length) using btree;

UPDATE STATISTICS HIGH FOR TABLE acc_length;


-- Delete duplicate records. Keep longer length    
SELECT acclen_acc AS mAcclen_acc, acclen_length AS mAcclen_length 
FROM acc_length 
GROUP BY 1,2 
HAVING COUNT(*) > 1
INTO TEMP tmp_multiple_acclength; 

DELETE FROM acc_length
WHERE acclen_acc IN (SELECT mAcclen_acc from tmp_multiple_acclength);

INSERT INTO acc_length ( acclen_acc, acclen_length )
SELECT mAcclen_acc, mAcclen_length
FROM tmp_multiple_acclength;

SELECT acclen_acc AS aAcclen_acc, max(acclen_length) AS aAcclen_length 
FROM acc_length 
GROUP BY 1
INTO TEMP tmp_acclength_longest;

CREATE INDEX tmp_acclen_longest_index ON tmp_acclength_longest
    (aAcclen_acc) using btree;

UPDATE STATISTICS HIGH FOR TABLE tmp_acclength_longest;

UPDATE acc_length
SET acclen_max_len = 't'
WHERE exists
  (
    SELECT *
    FROM tmp_acclength_longest
    WHERE acclen_acc = aAcclen_acc
      AND acclen_length = aAcclen_length
  );


DELETE FROM acc_length
WHERE acclen_max_len != 't';


-- (copied from ../GenPept/load_prot_len_acc.unl)
CREATE TEMP TABLE prot_len_acc 
  (
    pla_prot varchar (10), 
    pla_len integer,
    pla_gene varchar(100), 
    pla_acc varchar(10)
  )
with no log;
  
LOAD FROM '../GenPept/prot_len_acc.unl' INSERT INTO prot_len_acc;

CREATE INDEX pla_prot_idx ON prot_len_acc(pla_prot);
CREATE INDEX pla_acc_idx ON prot_len_acc(pla_acc);
CREATE INDEX pla_gene_idx ON prot_len_acc(pla_gene);
UPDATE STATISTICS FOR TABLE prot_len_acc;


--UNI_GENE
CREATE TEMP TABLE uni_gene
  (
    uni_ll_id		varchar (50) not null,
    uni_cluster_id	varchar (50) not null
  )
with no log;

!echo 'LOAD loc2UG.unl'
LOAD FROM 'loc2UG.unl' INSERT INTO uni_gene;

CREATE INDEX uni_ll_id_index ON uni_gene
    (uni_ll_id) using btree;
CREATE INDEX uni_cluster_id_index ON uni_gene
    (uni_cluster_id) using btree;


--TMP_DB_LINK
CREATE TEMP TABLE tmp_db_link
  (
    tmp_linked_recid 	varchar(50),
    tmp_db_name 	varchar(50),
    tmp_acc_num 	varchar(50),
    tmp_info 		varchar(80),
    tmp_dblink_zdb_id	varchar(50),
    tmp_fdbcont_zdb_id	varchar(50),
    tmp_length 		integer
  )
with no log;

CREATE INDEX tmp_linked_recid_index ON tmp_db_link
    (tmp_linked_recid) using btree;
CREATE INDEX tmp_acc_num_index ON tmp_db_link
    (tmp_acc_num) using btree;
CREATE INDEX tmp_db_name_index ON tmp_db_link
    (tmp_db_name) using btree;
    
!echo 'insert RefSeq INTO temp_db_link'
INSERT INTO tmp_db_link
  SELECT
    mrkr_zdb_id,
    'RefSeq',
    refseq_nM_acc,
    'Uncurated: RefSeq load ' || TODAY,
    'x',
    fdbcont_zdb_id,
    acclen_length
  FROM ref_seq_acc, ll_zdb, marker, foreign_db_contains, OUTER(acc_length)
  WHERE refseq_ll = llzdb_ll_id
    AND llzdb_zdb_id = mrkr_zdb_id
    AND fdbcont_fdb_db_name = 'RefSeq'
    AND fdbcont_fdbdt_data_type = 'cDNA'
    AND refseq_nM_acc[1,2] = 'NM'
    AND acclen_acc = refseq_nM_acc
;
INSERT INTO tmp_db_link
  SELECT
    mrkr_zdb_id,
    'RefSeq',
    refseq_nP_acc,
    'Uncurated: RefSeq load ' || TODAY,
    'x',
    fdbcont_zdb_id,
    acclen_length
  FROM ref_seq_acc, ll_zdb, marker, foreign_db_contains, OUTER(acc_length)
  WHERE refseq_ll = llzdb_ll_id
    AND llzdb_zdb_id = mrkr_zdb_id
    AND fdbcont_fdb_db_name = 'RefSeq'
    AND fdbcont_fdbdt_data_type = 'Polypeptide'
    AND refseq_nP_acc != '-'
    AND acclen_acc = refseq_nP_acc
;

INSERT INTO tmp_db_link
  SELECT
    mrkr_zdb_id,
    'RefSeq',
    refseq_nM_acc,
    'Uncurated: RefSeq load ' || TODAY,
    'x',
    fdbcont_zdb_id,
    acclen_length
  FROM ref_seq_acc, ll_zdb, marker, foreign_db_contains, OUTER(acc_length)
  WHERE refseq_ll = llzdb_ll_id
    AND llzdb_zdb_id = mrkr_zdb_id
    AND fdbcont_fdb_db_name = 'RefSeq'
    AND fdbcont_fdbdt_data_type = 'Genomic'
    AND refseq_nM_acc[1,2] = 'NC'
    AND acclen_acc = refseq_nM_acc
;

!echo 'insert GenBank INTO temp_db_link'
INSERT INTO tmp_db_link
  SELECT distinct
    mrkr_zdb_id,
    'GenBank',
    gbacc_acc,
    'Uncurated: RefSeq load ' || TODAY,
    'x',
    fdbcont_zdb_id,
    accbk_length
  FROM genbank_acc, ll_zdb, marker, foreign_db_contains, accession_bank
  WHERE gbacc_ll = llzdb_ll_id
    AND llzdb_zdb_id = mrkr_zdb_id
    AND fdbcont_fdb_db_name = 'GenBank'
    AND fdbcont_fdbdt_data_type = accbk_data_type
    AND gbacc_acc = accbk_acc_num
    AND accbk_db_name = 'GenBank'
    AND gbacc_acc != "-"
;

select distinct gbacc_pept as pept_acc, dblink_linked_recid as seg_zdb
    from db_link, foreign_db_contains, genbank_acc 
    where gbacc_acc = dblink_acc_num
      and dblink_fdbcont_zdb_id = fdbcont_zdb_id
      and fdbcont_fdb_db_name = "GenBank"
      and fdbcont_fdbdt_data_type = "cDNA"
      and gbacc_pept != "-"
      and dblink_linked_recid not like "ZDB-GENE%"
into temp tmp_put_genpept_on_segment;

select pept_acc  as mPept_acc
from tmp_put_genpept_on_segment
group by 1
having count(*) > 1
into temp tmp_non_unique_pept_acc;

delete from tmp_put_genpept_on_segment
where pept_acc in (select mPept_acc from tmp_non_unique_pept_acc);
      
!echo 'insert GenPept INTO temp_db_link'
INSERT INTO tmp_db_link
  SELECT distinct
    mrkr_zdb_id,
    'GenPept',
    gbacc_pept,
    'Uncurated: RefSeq load ' || TODAY,
    'x',
    fdbcont_zdb_id,
    pla_len
  FROM genbank_acc, ll_zdb, marker, foreign_db_contains, prot_len_acc
  WHERE gbacc_ll = llzdb_ll_id
    AND llzdb_zdb_id = mrkr_zdb_id
    AND fdbcont_fdb_db_name = 'GenPept'
    AND fdbcont_fdbdt_data_type = 'Polypeptide'
    AND gbacc_pept != '-'
    AND gbacc_pept = pla_prot
;

update tmp_db_link
set tmp_linked_recid = (select seg_zdb from tmp_put_genpept_on_segment where pept_acc = tmp_acc_num)
where exists 
  (select * 
   from tmp_put_genpept_on_segment
   where tmp_db_name = "GenPept" 
   and pept_acc = tmp_acc_num);

select seg_zdb from tmp_put_genpept_on_segment,tmp_db_link where pept_acc = tmp_acc_num group by 1 having count(*) > 1;


!echo 'insert ZF_LL INTO temp_db_link'
INSERT INTO tmp_db_link
  SELECT
    mrkr_zdb_id,
    'Entrez Gene',
    llzdb_ll_id,
    'uncurated ' || TODAY || ' Entrez load',
    'x',
    'x',
    ''
  FROM ll_zdb, marker
  WHERE llzdb_zdb_id = mrkr_zdb_id
;


   

----------------------------------
--| DROP TEMPORARY LOAD TABLES |--
----------------------------------

DROP TABLE REF_SEQ_ACC;
DROP TABLE GENBANK_ACC;


------------------------------------------------------
--| RECORD THE AUTOMATED and NON-AUTOMATED DBLINKS |--
------------------------------------------------------
-- Use non-automated links for consistency check after duplicates are deleted.

CREATE TEMP TABLE automated_dblink
  (
    link_id 	varchar(80)
  )
with no log;

!echo 'remove automated link'
INSERT INTO automated_dblink
SELECT dblink_zdb_id
  FROM db_link
  WHERE 1 = 
    (
      SELECT count (*) 
      FROM record_attribution r1
      WHERE r1.recattrib_data_zdb_id = dblink_zdb_id
        AND r1.recattrib_data_zdb_id in 
        (
          SELECT r2.recattrib_data_zdb_id
          FROM record_attribution r2
          WHERE r2.recattrib_source_zdb_id = "ZDB-PUB-020723-3"
        )
     )
;

CREATE INDEX link_id_index ON automated_dblink 
    (link_id) using btree in tempdbs1 ;


        --------------------------
        --|  Expression Links  |--
        --------------------------

-- Specially treated because the db_link is not an on-delete-cascade.
-- If the dblink is out of date, the link record must be updated
-- without deleting. Redundency checks later on will determine if 
-- the link needs updating. 

--TMP_XPAT_DBLINK
CREATE TEMP TABLE tmp_xpat_dblink
  (
    tmpfx_linked_recid 	varchar(50),
    tmpfx_db_name 	varchar(50),
    tmpfx_acc_num 	varchar(50),
    tmpfx_info 		varchar(80),
    tmpfx_dblink_zdb_id	varchar(50),
    tmpfx_fdbcont_zdb_id	varchar(50),
    tmpfx_length 		integer
  )
with no log;

INSERT INTO tmp_xpat_dblink
  (
    tmpfx_linked_recid,
    tmpfx_acc_num,
    tmpfx_info,
    tmpfx_dblink_zdb_id,
    tmpfx_fdbcont_zdb_id,
    tmpfx_length
  )
SELECT 
    dblink_linked_recid,
    dblink_acc_num,
    dblink_info,
    dblink_zdb_id,
    dblink_fdbcont_zdb_id,
    dblink_length
FROM db_link, automated_dblink, expression_experiment
WHERE dblink_zdb_id = link_id
  AND xpatex_dblink_zdb_id = link_id;

DELETE FROM automated_dblink 
WHERE EXISTS (SELECT * FROM tmp_xpat_dblink WHERE link_id = tmpfx_dblink_zdb_id);

Unload to "xpat_dblink.unl" Select tmpfx_dblink_zdb_id from tmp_xpat_dblink;

DELETE FROM zdb_active_data WHERE zactvd_zdb_id in (SELECT link_id FROM automated_dblink);

      

-- ======================= --
--  MULTIPLE REFSEQ LINKS  --
-- ======================= --

-- --------------  DELETE REDUNDANT DB_LINKS  ----------------- --
UPDATE STATISTICS HIGH FOR TABLE tmp_db_link;
UPDATE STATISTICS HIGH FOR TABLE db_link;

-- 06/15/2004
-- GenPept links from the Entrez load have precedence.
-- Conflicting GenPepts should be deleted.

    delete from zdb_active_data
    where zactvd_zdb_id in
      (
        select dblink_zdb_id
        from db_link, tmp_put_genpept_on_segment, record_attribution
        where pept_acc = dblink_acc_num
          and dblink_zdb_id = recattrib_data_zdb_id
          and recattrib_source_zdb_id = "ZDB-PUB-030924-6"      
      );
    

--Redundant links. A record with Marker/Acc_num/DB exists in ZFIN. Delete the matching record in tmp_db_link.

    SELECT dblink_linked_recid, fdbcont_fdb_db_name, dblink_acc_num
    FROM db_link, tmp_db_link, foreign_db_contains
    WHERE dblink_linked_recid = tmp_linked_recid
      AND dblink_fdbcont_zdb_id = fdbcont_zdb_id
      AND fdbcont_fdb_db_name = tmp_db_name
      AND dblink_acc_num = tmp_acc_num
    into temp tmp_redundant_db_link;
    

    DELETE FROM tmp_db_link
    WHERE exists 
      (
        SELECT *
        FROM tmp_redundant_db_link
        WHERE fdbcont_fdb_db_name = tmp_db_name
          AND dblink_acc_num = tmp_acc_num
          AND dblink_linked_recid = tmp_linked_recid
      );


--Remove links that would be assigned to a gene and currently have a 'Contained In' relationship
--with a segment.
--Contained In MREL types 2004-02: [gene encodes small segment, gene contains small segment, gene hybridized by small segment]

    SELECT *
    FROM db_link
    WHERE dblink_linked_recid not like "ZDB-GENE%"
    into temp tmp_est_db_link;
    
    DELETE FROM tmp_db_link
    WHERE tmp_linked_recid like "ZDB-GENE%"
      and exists
      (
        SELECT *
        FROM tmp_est_db_link, marker_relationship, foreign_db_contains
        WHERE dblink_fdbcont_zdb_id = fdbcont_zdb_id
          and fdbcont_fdb_db_name = tmp_db_name
          and dblink_acc_num = tmp_acc_num
          and dblink_linked_recid = mrel_mrkr_1_zdb_id
          and tmp_linked_recid = mrel_mrkr_2_zdb_id
          and mrel_type IN ('gene encodes small segment',
                            'gene contains small segment',
                            'gene hybridized by small segment')
      );

--Remove links that would be assigned to a gene and currently have a 'Contains' relationship
--with a marker. [BAC,PAC]

    DELETE FROM tmp_db_link
    WHERE tmp_linked_recid like "ZDB-GENE%"
      and exists
      (
        SELECT *
        FROM tmp_est_db_link, marker_relationship, foreign_db_contains
        WHERE dblink_fdbcont_zdb_id = fdbcont_zdb_id
          and fdbcont_fdb_db_name = tmp_db_name
          and dblink_acc_num = tmp_acc_num
          and dblink_linked_recid = mrel_mrkr_2_zdb_id
          and tmp_linked_recid = mrel_mrkr_1_zdb_id
      );


--These records are not linked through marker relationship. However, they have the same
--accession record. They will be unloaded for curatorial investigation.

    SELECT distinct dblink_linked_recid conf, 
           tmp_linked_recid as conf_linked_recid, 
           dblink_acc_num, 
           fdbcont_fdb_db_name,
           fdbcont_fdbdt_data_type
    FROM db_link, tmp_db_link, foreign_db_contains
    WHERE dblink_linked_recid != tmp_linked_recid
      AND dblink_fdbcont_zdb_id = fdbcont_zdb_id
      AND fdbcont_zdb_id = tmp_fdbcont_zdb_id
      AND fdbcont_fdbdt_data_type != 'Genomic'
      AND dblink_acc_num = tmp_acc_num
    into temp tmp_conflict_db_link;
        

    UNLOAD to conflict_dblink.unl
    select * from tmp_conflict_db_link;

    DELETE FROM tmp_db_link
    WHERE exists 
      (
        SELECT *
        FROM tmp_conflict_db_link
        WHERE dblink_acc_num = tmp_acc_num
          AND conf_linked_recid = tmp_linked_recid
          --AND fdbcont_fdb_db_name = tmp_db_name
      );



    

-- --------------  DELETE MULTIPLE REFSEQ  ----------------- --
-- Find all genes that have multiple RefSeq acc_nums.
-- Unload the gene_abbrev/acc_num and delete the records.
-- Failing to do this will result in a unique constraint violation.

create temp table tmp_multiple_refseq
  (
    multref_linked_recid varchar(50),
    multref_acc_num varchar(20),  
    multref_count integer  
  )with no log;

INSERT INTO tmp_multiple_refseq  
SELECT tmp_linked_recid, tmp_acc_num, count(tmp_linked_recid)
FROM tmp_db_link
WHERE tmp_db_name = "RefSeq"
GROUP BY tmp_linked_recid, tmp_acc_num
HAVING count(tmp_linked_recid) > 1;


UNLOAD to gene_with_multiple_linked_recid.unl
SELECT mrkr_abbrev, multref_acc_num
FROM tmp_multiple_refseq, marker
WHERE multref_linked_recid = mrkr_zdb_id;

DELETE FROM tmp_db_link
WHERE exists 
    (SELECT * 
     FROM tmp_multiple_refseq 
     WHERE multref_linked_recid = tmp_linked_recid 
       AND multref_acc_num = tmp_acc_num)
  AND tmp_db_name = 'RefSeq';


-- ----------------------  DB_LINK  ------------------------ --

-- ---------  CREATE DB_LINK ZDB IDs  ----------- --
-- Don't add zdb_ids until all redundant data has been removed.

  UPDATE tmp_db_link
  SET tmp_dblink_zdb_id = get_id('DBLINK');

-- RefSeq Lengths
UPDATE db_link
SET dblink_length = (SELECT acclen_length FROM acc_length WHERE dblink_acc_num = acclen_acc)
WHERE dblink_acc_num IN (SELECT acclen_acc FROM acc_length);


-- GenBank Lengths
UPDATE db_link
SET dblink_length = 
  (
    SELECT accbk_length 
    FROM accession_bank 
    WHERE dblink_acc_num = accbk_acc_num 
      AND accbk_db_name = 'GenBank'
  )
WHERE dblink_acc_num IN 
  (
    SELECT tmp_acc_num 
    FROM tmp_db_link
    WHERE tmp_db_name = 'GenBank'
  );


-- ------------------  add new links  ---------------------- --
!echo 'add active data'
INSERT INTO zdb_active_data SELECT tmp_dblink_zdb_id FROM tmp_db_link WHERE tmp_db_name = "RefSeq";
INSERT INTO zdb_active_data SELECT tmp_dblink_zdb_id FROM tmp_db_link WHERE tmp_db_name = "Entrez Gene";
INSERT INTO zdb_active_data SELECT tmp_dblink_zdb_id FROM tmp_db_link WHERE tmp_db_name = "GenBank";
INSERT INTO zdb_active_data SELECT tmp_dblink_zdb_id FROM tmp_db_link WHERE tmp_db_name = "GenPept";
 

!echo 'insert new db_links'
INSERT INTO db_link
        (dblink_linked_recid,
        dblink_fdbcont_zdb_id,
        dblink_acc_num,
        dblink_info,
        dblink_zdb_id,
        dblink_acc_num_display,
        dblink_length) 
  SELECT tmp_linked_recid,
        tmp_fdbcont_zdb_id,
        tmp_acc_num,
        tmp_info,
        tmp_dblink_zdb_id,
        tmp_acc_num,
        tmp_length
  FROM tmp_db_link
  WHERE tmp_db_name = "RefSeq";


INSERT INTO db_link
        (dblink_linked_recid,
        dblink_fdbcont_zdb_id,
        dblink_acc_num,
        dblink_info,
        dblink_zdb_id,
        dblink_acc_num_display,
        dblink_length) 
  SELECT tmp_linked_recid,
        fdbcont_zdb_id,
        tmp_acc_num,
        tmp_info,
        tmp_dblink_zdb_id,
        tmp_acc_num,
        tmp_length
  FROM tmp_db_link, foreign_db_contains 
  WHERE tmp_db_name = "Entrez Gene"
    AND tmp_db_name = fdbcont_fdb_db_name
    AND fdbcont_fdbdt_super_type = 'summary page'
    AND fdbcont_organism_common_name = "Zebrafish";

INSERT INTO db_link
        (dblink_linked_recid,
        dblink_fdbcont_zdb_id,
        dblink_acc_num,
        dblink_info,
        dblink_zdb_id,
        dblink_acc_num_display,
        dblink_length) 
  SELECT tmp_linked_recid,
        tmp_fdbcont_zdb_id,
        UPPER(tmp_acc_num),
        tmp_info,
        tmp_dblink_zdb_id,
        UPPER(tmp_acc_num),
        tmp_length
  FROM tmp_db_link
  WHERE tmp_db_name = "GenBank";
    
INSERT INTO db_link
        (dblink_linked_recid,
        dblink_fdbcont_zdb_id,
        dblink_acc_num,
        dblink_info,
        dblink_zdb_id,
        dblink_acc_num_display,
        dblink_length) 
  SELECT tmp_linked_recid,
        tmp_fdbcont_zdb_id,
        UPPER(tmp_acc_num),
        tmp_info,
        tmp_dblink_zdb_id,
        UPPER(tmp_acc_num),
        tmp_length
  FROM tmp_db_link
  WHERE tmp_db_name = "GenPept";


!echo 'Attribute ZFIN_LL links to an artificial pub record.'
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
    SELECT dblink_zdb_id, 'ZDB-PUB-020723-3'
    FROM db_link, tmp_db_link
    WHERE dblink_zdb_id = tmp_dblink_zdb_id
;


      
----------------------------------------------
--| DELETE OVERLAPPING GENPEP/REFSEQ LINKS |--
----------------------------------------------

SELECT dblink_zdb_id 
FROM db_link AS genpept, foreign_db_contains AS fdbcont1
WHERE genpept.dblink_fdbcont_zdb_id = fdbcont1.fdbcont_zdb_id
  AND fdbcont1.fdbcont_fdb_db_name = "GenPept"
  AND exists (
      SELECT *
      FROM db_link AS refseq, foreign_db_contains AS fdbcont2
      WHERE refseq.dblink_fdbcont_zdb_id = fdbcont2.fdbcont_zdb_id
        AND fdbcont2.fdbcont_fdb_db_name = "RefSeq"
        AND refseq.dblink_acc_num = genpept.dblink_acc_num
      )
INTO TEMP overlapping_acc_num;
        
DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT * FROM overlapping_acc_num);


-- ------------------  UNI_GENE  ------------------- --
!echo 'remove existing temp_db_link records'
DELETE FROM tmp_db_link;

!echo 'INSERT INTO temp_db_link'
INSERT INTO tmp_db_link
  SELECT
    llzdb_zdb_id,
    'UniGene',
    uni_cluster_id,
    'Uncurated: RefSeq load ' || TODAY,
    'x',
    fdbcont_zdb_id,
    ''
  FROM uni_gene, ll_zdb, marker, foreign_db_contains
  WHERE uni_ll_id = llzdb_ll_id
    AND llzdb_zdb_id = mrkr_zdb_id
    AND fdbcont_fdb_db_name = 'UniGene'
    AND fdbcont_fdbdt_data_type = 'Sequence Clusters'
;

-- ------------------ add new records ------------------ --
!echo 'get all UniGene db_links that remain'
  SELECT * 
  FROM db_link
  WHERE dblink_fdbcont_zdb_id in 
    (
      SELECT fdbcont_zdb_id
      FROM foreign_db_contains
      WHERE fdbcont_fdb_db_name = "UniGene"
    )
  INTO temp unigene_link
  with no log;

--only keep new links
DELETE FROM tmp_db_link
WHERE tmp_acc_num IN (SELECT dblink_acc_num FROM unigene_link);

!echo 'add active source AND active data'
UPDATE tmp_db_link
SET tmp_dblink_zdb_id = get_id('DBLINK');

INSERT INTO zdb_active_data SELECT tmp_dblink_zdb_id FROM tmp_db_link;


!echo 'insert new db_links'
INSERT INTO db_link
        (dblink_linked_recid,
        dblink_fdbcont_zdb_id,
        dblink_acc_num,
        dblink_info,
        dblink_zdb_id,
        dblink_acc_num_display)
    SELECT tmp_linked_recid,
        tmp_fdbcont_zdb_id,
        tmp_acc_num,
        tmp_info,
        tmp_dblink_zdb_id,
        tmp_acc_num
    FROM tmp_db_link;


!echo 'Attribute RefSeq links to an artificial pub record.'
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
    SELECT tmp_dblink_zdb_id, 'ZDB-PUB-020723-3'
    FROM tmp_db_link
;


--rollback work;
commit work;

