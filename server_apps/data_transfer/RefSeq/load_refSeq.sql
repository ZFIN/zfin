--RefSeq files are downloaded via ftp then parsed INTO informix loadable
--.unl files. RefSeq file formats are expected to be well formed per there
--documentation ON 05/31/02. Script will LOADnothing if RefSeq files are 
--not well formatted.
--
--Download
--ftp.ncbi.nih.gov/refseq/LocusLink/
--
--Static Variables
--db_name     - RefSeq



begin work;

  --=======================--
  -- LOAD INTO TEMP TABLES --
  --=======================--

--ZEBRAFISH locus_link
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

--HUMAN locus_link
CREATE TEMP TABLE LL_GDB
  (
    llgdb_ll_id		varchar (50) not null,
    llgdb_omim_id	varchar (50),
    llgdb_gdb_id	varchar (50) not null
  )
with no log;

!echo 'LOADll_hs_id.unl'
LOAD FROM 'll_hs_id.unl' INSERT INTO ll_gdb;

CREATE INDEX llgdb_ll_id_index ON ll_gdb
    (llgdb_ll_id) using btree;
CREATE INDEX llgdb_gdb_id_index ON ll_gdb
    (llgdb_gdb_id) using btree;

--MOUSE locus_link
CREATE TEMP TABLE LL_MGI
  (
    llmgi_ll_id		varchar (50) not null,
    llmgi_mgi_id	varchar (50) not null
  )
with no log;

!echo 'LOADll_mm_id.unl'
LOAD FROM 'll_mm_id.unl' INSERT INTO ll_mgi;

CREATE INDEX llmgi_ll_id_index ON ll_mgi
    (llmgi_ll_id) using btree;
CREATE INDEX llmgi_gdb_id_index ON ll_mgi
    (llmgi_mgi_id) using btree;
   

--REFSEQ ACCESSION NUM--
CREATE TEMP TABLE REF_SEQ_ACC
  (
    refseq_ll	varchar (50) not null,
    refseq_acc	varchar (50) not null
  )
with no log;

!echo 'LOADloc2ref.unl'
LOAD FROM 'loc2ref.unl' INSERT INTO ref_seq_acc;

CREATE INDEX refseq_ll_index ON ref_seq_acc
    (refseq_ll) using btree;
CREATE INDEX refseq_acc_index ON ref_seq_acc
    (refseq_acc) using btree;
   

--GENBANK ACCESSION NUM--
CREATE TEMP TABLE GENBANK_ACC
  (
    gbacc_ll	varchar (50) not null,
    gbacc_acc	varchar (50) not null
  )
with no log;

!echo 'LOADloc2acc.unl'
LOAD FROM 'loc2acc.unl' INSERT INTO genbank_acc;

CREATE INDEX genbank_ll_index ON genbank_acc
    (gbacc_ll) using btree;
CREATE INDEX genbank_acc_index ON genbank_acc
    (gbacc_acc) using btree;

--UNI_GENE
CREATE TEMP TABLE uni_gene
  (
    uni_ll_id		varchar (50) not null,
    uni_cluster_id	varchar (50) not null
  )
with no log;

!echo 'LOADloc2UG.unl'
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
    tmp_dblink_zdb_id	varchar(50)
  )
with no log;

!echo 'insert RefSeq INTO temp_db_link'
INSERT INTO tmp_db_link
  SELECT
    mrkr_zdb_id,
    'RefSeq',
    refseq_acc,
    'Uncurrated: RefSeq load ' || TODAY,
    'x'
  FROM ref_seq_acc, ll_zdb, marker
  WHERE refseq_ll = llzdb_ll_id
    AND llzdb_zdb_id = mrkr_zdb_id
;

!echo 'insert GenBank INTO temp_db_link'
INSERT INTO tmp_db_link
  SELECT
    mrkr_zdb_id,
    'Genbank',
    gbacc_acc,
    'Uncurrated: RefSeq load ' || TODAY,
    'x'
  FROM genbank_acc, ll_zdb, marker
  WHERE gbacc_ll = llzdb_ll_id
    AND llzdb_zdb_id = mrkr_zdb_id
;


!echo 'insert ZF_LL INTO temp_db_link'
INSERT INTO tmp_db_link
  SELECT
    mrkr_zdb_id,
    'LocusLink',
    llzdb_ll_id,
    'uncurrated ' || TODAY || ' LocusLink load',
    'x'
  FROM ll_zdb, marker
  WHERE llzdb_zdb_id = mrkr_zdb_id
;

CREATE INDEX tmp_linked_recid_index ON tmp_db_link
    (tmp_linked_recid) using btree;
CREATE INDEX tmp_acc_num_index ON tmp_db_link
    (tmp_acc_num) using btree;
CREATE INDEX tmp_db_name_index ON tmp_db_link
    (tmp_db_name) using btree;





!echo 'CREATE TEMP TABLE ortho_link'
CREATE TEMP TABLE ortho_link
  (
    lnkortho_linked_recid varchar(50),
    lnkortho_db_name varchar(50),
    lnkortho_acc_num varchar(50),
    lnkortho_dblink_zdb_id varchar(50)
  )
with no log;


UPDATE STATISTICS HIGH FOR table ll_gdb;
UPDATE STATISTICS HIGH FOR table ll_mgi;

--SELECT a distinct set of gdb/zfin ids
!echo 'INSERT INTO ortho_link'
INSERT INTO ortho_link
    SELECT 
      distinct zdb_id,
      'LocusLink',
      llgdb_ll_id,
      'x'
    FROM db_link, orthologue, ll_gdb
    WHERE db_name = "GDB"
      AND zdb_id = linked_recid
      AND acc_num = llgdb_gdb_id;


--INSERT OMIM links into ortho_link
INSERT INTO ortho_link
    SELECT 
      distinct zdb_id,
      'OMIM',
      llgdb_omim_id,
      'x'
    FROM db_link, orthologue, ll_gdb
    WHERE db_name = "GDB"
      AND zdb_id = linked_recid
      AND acc_num = llgdb_gdb_id
      AND llgdb_omim_id is not null
      AND llgdb_omim_id NOT IN ('193500','106210','168461','300401','601868');
          -- Ban OMIM disease links (Hard code for now 06-02-03) --


!echo 'INSERT MGI links into ortho_link'
INSERT INTO ortho_link
    SELECT 
      distinct zdb_id,
      'LocusLink',
      llmgi_ll_id,
      'x'
    FROM db_link, orthologue, ll_mgi
    WHERE db_name = "MGI"
      AND linked_recid = zdb_id
      AND acc_num = llmgi_mgi_id;


CREATE INDEX lnkortho_dblink_zdb_id_index ON ortho_link
    (lnkortho_dblink_zdb_id) using btree;
    
    
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

DELETE FROM zdb_active_data WHERE zactvd_zdb_id in (SELECT link_id FROM automated_dblink);

!echo 'get all LocusLink AND OMIM db_links that remain'
  SELECT * 
  FROM db_link 
  WHERE db_name = "OMIM" 
     or db_name = "LocusLink"
  INTO temp old_omim_and_ll
  with no log;

-- Delete orthologue load links that are redundant with existing production links.
  DELETE FROM ortho_link
  WHERE EXISTS
    (
      SELECT *
      FROM old_omim_and_ll
      WHERE lnkortho_db_name = db_name
        AND lnkortho_acc_num = acc_num
    );


      
-----------------------------------------------
--| INSERT LOCUSLINK ORTHOLOGUE ACTIVE DATA |--
-----------------------------------------------

UPDATE STATISTICS HIGH FOR table ortho_link;

--add ZDB ids
  UPDATE ortho_link
  SET lnkortho_dblink_zdb_id = get_id('DBLINK');
  
!echo 'add LocusLink Orthologue active data'
  INSERT INTO zdb_active_data 
  SELECT lnkortho_dblink_zdb_id 
  FROM ortho_link
  WHERE lnkortho_db_name = 'LocusLink'
    AND lnkortho_acc_num NOT IN 
        (SELECT acc_num FROM old_omim_and_ll WHERE db_name = 'LocusLink');

!echo 'add OMIM active data'
  INSERT INTO zdb_active_data 
  SELECT lnkortho_dblink_zdb_id 
  FROM ortho_link
  WHERE lnkortho_db_name = 'OMIM'
    AND lnkortho_acc_num NOT IN 
        (SELECT acc_num FROM old_omim_and_ll WHERE db_name = 'OMIM');
  
!echo 'insert LocusLink Orthologue db_links.'
INSERT INTO db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display)
    SELECT distinct
        lnkortho_linked_recid,
        lnkortho_db_name,
        lnkortho_acc_num,
        'Uncurrated: RefSeq load ' || TODAY,
        lnkortho_dblink_zdb_id,
        lnkortho_acc_num
    FROM ortho_link;


-- --------------  DELETE duplicate ORTHO  ----------------- --
-- select the mrkr for LocusLink
-- select the mrkr for OMIM
-- select the conflict acc_num for each mrkr
-- delete conflict acc_num that are from this load

CREATE TEMP TABLE tmp_multiple_ortho_gene
  (
    multortho_mrkr_zdb_id varchar(50),
    multortho_ortho_zdb_id varchar(50)
  )
  with no log;

INSERT INTO tmp_multiple_ortho_gene
SELECT mrkr_zdb_id, zdb_id
FROM marker, orthologue 
WHERE mrkr_zdb_id = c_gene_id 
  AND 1 < 
    (
      SELECT COUNT(*) 
      FROM db_link 
      WHERE linked_recid = zdb_id 
        AND db_name = "LocusLink"
    );

INSERT INTO tmp_multiple_ortho_gene
SELECT mrkr_zdb_id, zdb_id
FROM marker, orthologue 
WHERE mrkr_zdb_id = c_gene_id 
  AND 1 < 
    (
      SELECT COUNT(*) 
      FROM db_link 
      WHERE linked_recid = zdb_id 
        AND db_name = "OMIM"
    );

SELECT multortho_mrkr_zdb_id as tmp_mrkr_zdb_id,  
       dblink_zdb_id as tmp_dblink_zdb_id
FROM tmp_multiple_ortho_gene, db_link
WHERE multortho_ortho_zdb_id = linked_recid
INTO temp tmp_multiple_ortho;

    
UNLOAD to ortho_with_multiple_acc_num.unl
SELECT mrkr_abbrev, acc_num
FROM tmp_multiple_ortho, db_link, marker
WHERE mrkr_zdb_id = tmp_mrkr_zdb_id
  AND tmp_dblink_zdb_id = dblink_zdb_id
ORDER by mrkr_abbrev;


DELETE FROM zdb_active_data
WHERE zactvd_zdb_id IN 
  (
    SELECT tmp_dblink_zdb_id
    FROM tmp_multiple_ortho
    WHERE tmp_dblink_zdb_id NOT IN
      ( 
        SELECT dblink_zdb_id from old_omim_and_ll
      )
  );


!echo 'Attribute human LL links to source LocusLink curation pub.'
INSERT INTO record_attribution
    SELECT dblink_zdb_id, 'ZDB-PUB-020723-3'
    FROM db_link, ortho_link
    WHERE dblink_zdb_id = lnkortho_dblink_zdb_id;
{
!echo 'Attribute OMIM links to source LocusLink curation pub.'
INSERT INTO record_attribution
    SELECT dblink_zdb_id, 'ZDB-PUB-020723-3'
    FROM db_link
    WHERE db_name = "OMIM"
      AND dblink_zdb_id NOT IN (SELECT dblink_zdb_id FROM old_omim_and_ll);
}


-- ======================= --
--  MULTIPLE REFSEQ LINKS  --
-- ======================= --

-- --------------  DELETE REDUNDANT DB_LINKS  ----------------- --
UPDATE STATISTICS HIGH FOR TABLE tmp_db_link;

DELETE FROM tmp_db_link
WHERE 0 <  
  (
    SELECT count(*)
    FROM db_link
    WHERE linked_recid = tmp_linked_recid
      AND db_name = tmp_db_name
      AND acc_num = tmp_acc_num
  );

-- --------------  DELETE MULTIPLES REFSEQ  ----------------- --
-- Find all genes that have multiple RefSeq acc_nums.
-- Unload the gene_abbrev/acc_num and delete the records.
-- Failing to do this will result in a unique constraint violation.

SELECT tmp_linked_recid as multref_linked_recid, 
       count(tmp_linked_recid) as multref_count
FROM tmp_db_link
WHERE tmp_db_name = "RefSeq"
GROUP BY tmp_linked_recid
HAVING count(tmp_linked_recid) > 1
order by 1
INTO temp tmp_multiple_refseq;

UNLOAD to gene_with_multiple_linked_recid.unl
SELECT mrkr_abbrev, tmp_acc_num
FROM tmp_multiple_refseq, tmp_db_link, marker
WHERE multref_linked_recid = tmp_linked_recid
  AND tmp_db_name = 'RefSeq'
  AND tmp_linked_recid = mrkr_zdb_id;

DELETE FROM tmp_db_link
WHERE tmp_linked_recid in 
    (SELECT multref_linked_recid FROM tmp_multiple_refseq)
  AND tmp_db_name = 'RefSeq';


-- ----------------------  DB_LINK  ------------------------ --

-- ---------  CREATE DB_LINK ZDB IDs  ----------- --
-- Don't add zdb_ids until all redundant data has been removed.

  UPDATE tmp_db_link
  SET tmp_dblink_zdb_id = get_id('DBLINK');
  
-- ------------------  add new links  ---------------------- --
!echo 'add active data'
INSERT INTO zdb_active_data SELECT tmp_dblink_zdb_id FROM tmp_db_link WHERE tmp_db_name = "RefSeq";
INSERT INTO zdb_active_data SELECT tmp_dblink_zdb_id FROM tmp_db_link WHERE tmp_db_name = "LocusLink";
INSERT INTO zdb_active_data SELECT tmp_dblink_zdb_id FROM tmp_db_link WHERE tmp_db_name = "Genbank";
 
!echo 'insert new db_links'
INSERT INTO db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display) SELECT *, tmp_acc_num FROM tmp_db_link WHERE tmp_db_name = "RefSeq";
INSERT INTO db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display) SELECT *, tmp_acc_num FROM tmp_db_link WHERE tmp_db_name = "LocusLink";
INSERT INTO db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display) SELECT *, tmp_acc_num FROM tmp_db_link WHERE tmp_db_name = "Genbank";


!echo 'Attribute ZFIN_LL links to an artificial pub record.'
INSERT INTO record_attribution
    SELECT dblink_zdb_id, 'ZDB-PUB-020723-3'
    FROM db_link, tmp_db_link
    WHERE dblink_zdb_id = tmp_dblink_zdb_id
;


-- ------------------  UNI_GENE  ------------------- --
!echo 'remove existing temp_db_link records'
DELETE FROM tmp_db_link;

!echo 'INSERT INTO temp_db_link'
INSERT INTO tmp_db_link
  SELECT
    llzdb_zdb_id,
    'UniGene',
    uni_cluster_id,
    'Uncurrated: RefSeq load ' || TODAY,
    get_id('DBLINK')
  FROM uni_gene, ll_zdb, zdb_active_data
  WHERE uni_ll_id = llzdb_ll_id
    AND llzdb_zdb_id = zactvd_zdb_id
;

-- ------------------ add new records ------------------ --
!echo 'get all UniGene db_links that remain'
  SELECT * 
  FROM db_link 
  WHERE db_name = "UniGene"
  INTO temp unigene_link
  with no log;


!echo 'add active source AND active data'
INSERT INTO zdb_active_data SELECT tmp_dblink_zdb_id FROM tmp_db_link
       WHERE tmp_acc_num NOT IN (SELECT acc_num FROM unigene_link);


!echo 'insert new db_links'
INSERT INTO db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display)
    SELECT *, tmp_acc_num
    FROM tmp_db_link
    WHERE tmp_acc_num NOT IN (SELECT acc_num FROM unigene_link);


!echo 'Attribute RefSeq links to an artificial pub record.'
INSERT INTO record_attribution
    SELECT a.dblink_zdb_id, 'ZDB-PUB-020723-3'
    FROM db_link a
    WHERE a.db_name = "UniGene"
      AND a.acc_num NOT IN (SELECT acc_num FROM unigene_link)
;


commit work;

