{
I. Load zgc data 
   Insert the library data into zgc_lib_tmp
   Insert the clone data into Zgc_tmp
   Delete zfin/zgc redundancy (zfin zgc record that maps to the same marker abbrev)
   Unload discrepancies from Zgc_tmp (ncbi abbrev != zfin abbrev)
   Unload non-library from Zgc_tmp


II. Create new records
     A. Add zgc ESTs
        Create temp Marker EST records for each Zgc_tmp
        Move BC Genbank links from GENE records to zgc EST (update Zgc_tmp.mrkr_name)
        Create BC Genbank links for empty zgc ESTs
        Create clone records for zgc ests
     B. Add zgc GENEs
        Upgrade bonus genes
        Create temp GENE records
     C. Add temp marker_relationships

III. Move temp tables into production
     A. Create zdb_active_data records
     B. Move gene, EST, clone, marker_relationship records

}


BEGIN WORK;

	-------------------
	--| Temp Tables |--
	-------------------

CREATE TEMP TABLE tmp_Zgc_Lib 
  (
    zLib_name    varchar(80),
    zLib_tissue  varchar(100),
    zLib_vector  varchar(20),
    junk         integer  
  ) 
with no log;

CREATE index zLib_name_index
 ON tmp_Zgc_Lib(zLib_name)
 USING btree;
 
CREATE TEMP TABLE tmp_Lib_Bank 
  (
    Libbank_name    varchar(80),
    Libbank_zdb_id  varchar(50)
  ) 
with no log;


CREATE TEMP TABLE tmp_Zgc
  (
    zgc_mrkr_abbrev	varchar(50),
    zgc_defline		varchar(100),
    zgc_ll_id		varchar(20),
    zgc_cluster		varchar(20),
    zgc_image		varchar(20),
    zgc_acc_num		varchar(20),
    zgc_length		integer,
    zgc_lib_id		integer,
    zgc_lib		varchar(50),
    zgc_name		varchar(20)
  )
with no log;
  
CREATE index zgc_mrkr_abbrev_index
  ON tmp_Zgc(zgc_mrkr_abbrev)
  USING btree;

CREATE index zgc_name_index
  ON tmp_Zgc(zgc_name)
  USING btree;

CREATE index zgc_lib_index
  ON tmp_Zgc(zgc_lib)
  USING btree;
  

CREATE TEMP TABLE tmp_Zgc_EST
  (
    zEST_zdb_id 	varchar(50),
    zEST_name		varchar(50),
    zEST_abbrev		varchar(50),
    zEST_description	varchar(100),
    zEST_owner		varchar(50)
  )
with no log;

CREATE index zEST_abbrev_index
  ON tmp_Zgc_EST(zEST_abbrev)
  USING btree;
  

CREATE TEMP TABLE tmp_Zgc_GENE
  (
    zGENE_zdb_id 	varchar(50),
    zGENE_name		varchar(50),
    zGENE_abbrev	varchar(50),
    zGENE_description	varchar(100),
    zGENE_owner		varchar(50)
  )
with no log;

CREATE index zGENE_abbrev_index
  ON tmp_Zgc_GENE(zGENE_abbrev)
  USING btree;
  

CREATE TEMP TABLE tmp_Zgc_Clone
  (
    zClone_mrkr_zdb_id 		varchar(50),
    zClone_vector		varchar(50),
    zClone_probelib_zdb_id	varchar(50)
  )
with no log;
  

CREATE TEMP TABLE tmp_Zgc_Dblink_new
  (
    zDblink_zdb_id 		varchar(50),
    zDblink_linked_recid	varchar(50),
    zDblink_fdbcont_zdb_id	varchar(50),
    zDblink_acc_num		varchar(50),
    zDblink_info		varchar(80),
    zDblink_length		integer
  )
with no log;
  

CREATE TEMP TABLE tmp_Zgc_Dblink_moved
  (
    zDblink_zdb_id 		varchar(50),
    zDblink_linked_recid	varchar(50),
    zDblink_fdbcont_zdb_id	varchar(50),
    zDblink_acc_num		varchar(50),
    zDblink_info		varchar(80),
    zDblink_length		integer
  )
with no log;

CREATE index zDblink_acc_num_index
  ON tmp_Zgc_Dblink_moved(zDblink_acc_num)
  USING btree;

CREATE index zDblink_zdb_id_index
  ON tmp_Zgc_Dblink_moved(zDblink_zdb_id)
  USING btree;
  

CREATE TEMP TABLE tmp_Zgc_MREL
  (
    zMrel_zdb_id 	varchar(50),
    zMrel_gene_zdb_id	varchar(50),
    zMrel_est_zdb_id	varchar(50)
  )
with no log;


CREATE TEMP TABLE tmp_zName_Mismatch
  (
    zMis_acc_num	varchar(50),
    zMis_zgc_zdb_id	varchar(50),
    zMis_zfin_zdb_id	varchar(50)
  )
with no log;

CREATE index zMis_zgc_zdb_id_index
  ON tmp_Zname_Mismatch(zMis_zgc_zdb_id)
  USING btree;

CREATE index zMis_zfin_zdb_id_index
  ON tmp_Zname_Mismatch(zMis_zfin_zdb_id)
  USING btree;


CREATE TEMP TABLE tmp_zUpdate
  (
    zUp_acc_num		varchar(50),
    zUp_zgc_zdb_id	varchar(50),
    zUp_zfin_zdb_id	varchar(50)
  )
with no log;

CREATE index zUp_zgc_zdb_id_index
  ON tmp_zUpdate(zUp_zgc_zdb_id)
  USING btree;

CREATE index zUp_zfin_zdb_id_index
  ON tmp_zUpdate(zUp_zfin_zdb_id)
  USING btree;


CREATE TEMP TABLE tmp_Zgc_Upgrade
  (
    zUpgrade_zdb_id	varchar(50),
    zUpgrade_old_name	varchar(50),
    zUpgrade_new_name	varchar(50),
    zUpgrade_length	integer
  )
with no log;

CREATE index zUpgrade_zdb_id_index
  ON tmp_Zgc_Upgrade(zUpgrade_zdb_id)
  USING btree;


	----------------------
	--| Load the Data |--
	----------------------
	
	-- Insert the library data into zgc_lib_tmp
	-- Insert the clone data into Zgc_tmp


LOAD from StaticLibList.unl INSERT into tmp_Zgc_Lib;
LOAD from fishlib.unl INSERT into tmp_Lib_Bank;
LOAD from StaticCloneList.unl INSERT into tmp_Zgc;

UPDATE STATISTICS HIGH FOR table tmp_Zgc_Lib;
UPDATE STATISTICS HIGH FOR table tmp_Lib_Bank;
UPDATE STATISTICS HIGH FOR table tmp_Zgc;


	--   Find related markers where (ncbi abbrev != zfin abbrev)
	--   Update the abbrev

INSERT into tmp_zUpdate
SELECT zgc_acc_num, zgc.mrkr_zdb_id, zfin.mrkr_zdb_id
FROM tmp_Zgc, marker AS zgc, marker AS zfin, marker_relationship
WHERE mrel_mrkr_1_zdb_id = zfin.mrkr_zdb_id
  and mrel_mrkr_2_zdb_Id = zgc.mrkr_zdb_id
  and mrel_type = "gene encodes small segment"
  and zgc.mrkr_abbrev = zgc_mrkr_abbrev;
  
UPDATE tmp_Zgc 
SET zgc_mrkr_abbrev = 
  (
    SELECT zfin.mrkr_abbrev 
    FROM marker AS zfin, marker AS zgc, tmp_zUpdate
    WHERE zfin.mrkr_zdb_id = zUp_zfin_zdb_id
      and zgc.mrkr_zdb_id = zUp_zgc_zdb_id
      and zgc_mrkr_abbrev = zgc.mrkr_abbrev
  )
WHERE zgc_acc_num IN (select zUp_acc_num from tmp_zUpdate);


	--  Delete zfin/zgc redundancy 
	
DELETE from tmp_Zgc
WHERE exists
    (
      select *
      from marker 
      where mrkr_abbrev = zgc_name
    );


	--   Discrepancies
	--   Unload if marker related (ncbi abbrev != zfin abbrev)

INSERT into tmp_zName_Mismatch
SELECT zgc_acc_num, zgc.mrkr_zdb_id, zfin.mrkr_zdb_id
FROM tmp_Zgc, marker AS zfin, marker AS zgc, marker_relationship
WHERE mrel_mrkr_1_zdb_id = zfin.mrkr_zdb_id
  and mrel_mrkr_2_zdb_Id = zgc.mrkr_zdb_id
  and zgc.mrkr_abbrev = zgc_name
  and zfin.mrkr_abbrev != zgc_mrkr_abbrev;


	--   Unload if the BC acc_num is attached to a gene, and the gene_abbrev != the ncbi abbrev
	
INSERT into tmp_zName_Mismatch
SELECT zgc_acc_num, zgc.mrkr_zdb_id, zfin.mrkr_zdb_id
FROM tmp_Zgc, marker AS zgc, marker AS zfin, db_link
WHERE zgc_acc_num = dblink_acc_num
  and dblink_linked_recid = zfin.mrkr_zdb_id
  and zfin.mrkr_abbrev != zgc_mrkr_abbrev
  and zgc_mrkr_abbrev != ''
  and zgc_mrkr_abbrev = zgc.mrkr_abbrev
  and zgc_acc_num not in (SELECT zUp_acc_num FROM tmp_zUpdate);

  

UNLOAD to 'zName_mismatch.unl'
  SELECT zMis_acc_num, zfin.mrkr_abbrev, zgc.mrkr_abbrev
  FROM tmp_zName_mismatch, marker AS zfin, marker AS zgc
  WHERE zMis_zgc_zdb_id = zgc.mrkr_zdb_id
    and zMis_zfin_zdb_id = zfin.mrkr_zdb_id;
  
DELETE from tmp_Zgc 
WHERE zgc_acc_num IN 
  (
    SELECT zMis_acc_num FROM tmp_zName_mismatch
  );
  
  
	--  Unload non-library from Zgc_tmp
	--  only load new libraries that are ZGC libraries


DELETE FROM tmp_Lib_Bank
WHERE libbank_name NOT IN 
  (
    SELECT zLib_name 
    FROM tmp_Zgc_Lib
  );

DELETE FROM tmp_Lib_Bank
WHERE libbank_name IN 
  (
    SELECT probelib_name  
    FROM probe_library
  );

Update tmp_Lib_Bank
SET libbank_zdb_id = get_id('PROBELIB');

INSERT INTO zdb_active_data(zactvd_zdb_id)
SELECT libbank_zdb_id 
FROM tmp_Lib_Bank;


INSERT INTO probe_library
 (probelib_zdb_id, probelib_name, probelib_species)
SELECT distinct libbank_zdb_id,libbank_name,'Danio rerio'
FROM tmp_Lib_Bank;

SELECT zLib_name 
FROM tmp_Zgc_Lib
WHERE zLib_name NOT IN 
  (
    SELECT probelib_name FROM probe_library
  )
into temp tmp_zLib_not_found;

UNLOAD to 'zLib_not_found.unl'
SELECT * FROM tmp_zLib_not_found;

DELETE from tmp_Zgc
WHERE zgc_lib IN
  (
    SELECT * from tmp_zLib_not_found
  );


SELECT zLib_vector 
FROM tmp_Zgc_Lib
WHERE zLib_vector NOT IN 
  (
    SELECT vector_name FROM vector
  )
into temp tmp_zLib_vector_not_found;

UNLOAD to 'zLib_vector_not_found.unl'
SELECT * from tmp_zLib_vector_not_found;

DELETE from tmp_Zgc
WHERE zgc_lib IN
  (
    SELECT lib.zLib_name 
    from tmp_zLib_vector_not_found vec, tmp_Zgc_Lib lib
    where vec.zLib_vector = lib.zLib_vector
  );



	--------------------------
	--| Create new records |--
	--------------------------
	
	--  Add zgc ESTs
        --  Create temp Marker EST records for each Zgc_tmp

INSERT into tmp_Zgc_EST
SELECT get_id('CDNA'), 
    zgc_name,
    zgc_name,
    '',
    'ZDB-PERS-010716-1'
FROM tmp_Zgc;
    
--        Move BC Genbank links from GENE records to zgc EST (update Zgc_tmp.mrkr_name)
INSERT into tmp_Zgc_Dblink_moved
  (
    zDblink_zdb_id,
    zDblink_linked_recid,
    zDblink_fdbcont_zdb_id,
    zDblink_acc_num,
    zDblink_info,
    zDblink_length
  )
SELECT 
    dblink_zdb_id,
    zEST_zdb_id,
    dblink_fdbcont_zdb_id,
    dblink_acc_num,
    dblink_info,
    dblink_length
FROM
    tmp_Zgc_EST, tmp_Zgc, db_link
WHERE
    dblink_acc_num = zgc_acc_num
    AND zgc_name = zEST_name;


--        Create BC Genbank links for empty zgc ESTs
INSERT into tmp_Zgc_Dblink_new
  (
    zDblink_zdb_id,
    zDblink_linked_recid,
    zDblink_fdbcont_zdb_id,
    zDblink_acc_num,
    zDblink_info,
    zDblink_length
  )
SELECT 
    get_id('DBLINK'),
    zEST_zdb_id,
    fdbcont_zdb_id,
    zgc_acc_num,
    'Uncurrated: ZGC load ' || TODAY,
    zgc_length
FROM
    tmp_Zgc, foreign_db_contains, tmp_Zgc_EST
WHERE
    zgc_acc_num NOT IN (select zDblink_acc_num from tmp_Zgc_Dblink_moved)
    AND fdbcont_fdb_db_name = 'Genbank'
    AND fdbcont_fdbdt_data_type = 'cDNA'
    AND zgc_name = zEST_name;


--        Create clone records for zgc ests
INSERT into tmp_Zgc_Clone
  (
    zClone_mrkr_zdb_id,
    zClone_vector,
    zClone_probelib_zdb_id
  )
SELECT 
    zEST_zdb_id,
    zLib_vector,
    probelib_zdb_id
FROM 
    tmp_Zgc, tmp_Zgc_EST, tmp_Zgc_Dblink_moved, tmp_Zgc_Lib, probe_library
WHERE 
    zgc_acc_num = zDblink_acc_num
    AND zDblink_linked_recid = zEST_zdb_id
    AND zgc_lib = zLib_name
    AND zLib_name = probelib_name;

INSERT into tmp_Zgc_Clone
  (
    zClone_mrkr_zdb_id,
    zClone_vector,
    zClone_probelib_zdb_id
  )
SELECT 
    zEST_zdb_id,
    zLib_vector,
    probelib_zdb_id
FROM 
    tmp_Zgc, tmp_Zgc_EST, tmp_Zgc_Dblink_new, tmp_Zgc_Lib, probe_library
WHERE 
    zgc_acc_num = zDblink_acc_num
    AND zDblink_linked_recid = zEST_zdb_id
    AND zgc_lib = zLib_name
    AND zLib_name = probelib_name;


--     B. Add zgc GENEs
INSERT into tmp_Zgc_Gene
  (
    zGENE_zdb_id,
    zGENE_name,
    zGENE_abbrev,
    zGENE_description,
    zGENE_owner
  )
SELECT
    get_id('GENE'),
    'zgc:' || zgc_name[4,20],
    'zgc:' || zgc_name[4,20],
    "This gene is characterized by full length cDNAs isolated as part of the Zebrafish Gene Collection (ZGC). When more is known about the gene, the current nomenclature will be replaced with more traditional zebrafish gene nomenclature. The prefix `zgc:' indicates that this gene is represented by cDNAs generated at the ZGC project.",
    'ZDB-PERS-010716-1'
FROM
    tmp_Zgc
WHERE
    zgc_mrkr_abbrev NOT IN (select mrkr_abbrev from marker where mrkr_type = "GENE")
    OR zgc_mrkr_abbrev is NULL;


--        Upgrade bonus genes
INSERT into tmp_Zgc_Upgrade
  (
    zUpgrade_zdb_id,
    zUpgrade_old_name,
    zUpgrade_new_name,
    zUpgrade_length
  )
SELECT 
    mrkr_zdb_id, 
    zgc_mrkr_abbrev, 
    'zgc:'||zgc_name[4,20],
    zgc_length
FROM tmp_Zgc, marker
WHERE zgc_mrkr_abbrev[1,3] != 'si:' 
  and zgc_mrkr_abbrev like "%:%"
  and zgc_mrkr_abbrev = mrkr_abbrev
  and mrkr_type = 'GENE';

--        Choose the longest zgc and remove the shorter zgc 
SELECT 
    mrkr_zdb_id AS zLong_zdb_id,
    max(zgc_length) AS zLong_length
FROM tmp_Zgc, marker
WHERE zgc_mrkr_abbrev[1,3] != 'si:' 
  and zgc_mrkr_abbrev like "%:%"
  and zgc_mrkr_abbrev = mrkr_abbrev
  and mrkr_type = 'GENE'
GROUP by 1
INTO temp tmp_zgc_Upgrade_longest;  

DELETE from tmp_Zgc_Upgrade
WHERE zUpgrade_length NOT IN 
  (
    SELECT zLong_length
    FROM tmp_zgc_Upgrade_longest
    WHERE zLong_zdb_id = zUpgrade_zdb_id
  );

--  IF the zgc are the same length, flip a coin
SELECT zUpgrade_zdb_id AS zSameLen_zdb_id, zUpgrade_old_name AS zSameLen_old_name
FROM tmp_Zgc_Upgrade, tmp_zgc_upgrade_longest
WHERE zUpgrade_zdb_id = zLong_zdb_id
GROUP by 1,2
HAVING count(*) > 1
INTO temp tmp_zgc_Upgrade_same_length;

SELECT 
    zgc_mrkr_abbrev AS zFlip_mrkr_abbrev,
    max(zgc_name) AS zFlip_name
FROM tmp_Zgc, tmp_Zgc_Upgrade_same_length
WHERE zgc_mrkr_abbrev = zSameLen_old_name 
GROUP by 1
INTO temp tmp_zgc_Upgrade_Coin_Flip; 

DELETE from tmp_Zgc_Upgrade
WHERE zUpgrade_new_name[5,20] IN 
  (
    SELECT zFlip_name[4,20] 
    FROM tmp_zgc_Upgrade_Coin_flip
  );




--     C. Add temp marker_relationships
INSERT into tmp_Zgc_Mrel
  (
    zMrel_zdb_id,
    zMrel_gene_zdb_id,
    zMrel_est_zdb_id  
  )
SELECT
    get_id('MREL'),
    mrkr_zdb_id,
    zEST_zdb_id
FROM 
    tmp_Zgc, tmp_Zgc_EST, marker
WHERE
    zgc_name = zEST_abbrev
    AND zgc_mrkr_abbrev = mrkr_abbrev;


INSERT into tmp_Zgc_Mrel
  (
    zMrel_zdb_id,
    zMrel_gene_zdb_id,
    zMrel_est_zdb_id  
  )
SELECT
    get_id('MREL'),
    zGENE_zdb_id,
    zEST_zdb_id
FROM 
    tmp_Zgc, tmp_Zgc_EST, tmp_Zgc_GENE
WHERE
    zgc_name = zEST_abbrev
    AND 'zgc:'||zgc_name[4,20] = zGENE_abbrev;


	-----------------------------------------
	--| Move temp records into production |--
	-----------------------------------------
!echo "EST"

INSERT into zdb_active_data SELECT zEST_zdb_id FROM tmp_Zgc_EST;

select * from tmp_zgc_est where zest_abbrev in (select mrkr_abbrev from marker);


INSERT into marker
  (
    mrkr_zdb_id,
    mrkr_name,
    mrkr_abbrev,
    mrkr_type,
    mrkr_comments,
    mrkr_owner
  )
SELECT 
    zEST_zdb_id,
    zEST_name,
    zEST_abbrev,
    'CDNA',
    zEST_description,
    zEST_owner
FROM tmp_Zgc_EST;

INSERT into record_attribution
  (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
  )
SELECT zEST_zdb_id, 'ZDB-PUB-040217-2'
FROM tmp_Zgc_EST;



!echo 'Dblink'

INSERT into zdb_active_data SELECT zDblink_zdb_id FROM tmp_Zgc_Dblink_new;

INSERT into db_link
  (
    dblink_zdb_id,
    dblink_linked_recid,
    dblink_fdbcont_zdb_id,
    dblink_acc_num,
    dblink_length,
    dblink_info,
    dblink_acc_num_display
  )
SELECT 
    zDblink_zdb_id,
    zDblink_linked_recid,
    zDblink_fdbcont_zdb_id,
    zDblink_acc_num,
    zDblink_length,
    zDblink_info,
    zDblink_acc_num
FROM tmp_Zgc_Dblink_new;

UPDATE db_link
SET dblink_linked_recid = (SELECT zDblink_linked_recid FROM tmp_Zgc_Dblink_moved WHERE dblink_zdb_id = zDblink_zdb_id)
WHERE dblink_zdb_id IN (SELECT zDblink_zdb_id FROM tmp_Zgc_Dblink_moved);

UPDATE db_link
SET dblink_length = (SELECT zDblink_length FROM tmp_Zgc_Dblink_moved WHERE dblink_zdb_id = zDblink_zdb_id)
WHERE dblink_zdb_id IN (SELECT zDblink_zdb_id FROM tmp_Zgc_Dblink_moved);

INSERT into record_attribution
  (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
  )
SELECT zDblink_zdb_id, 'ZDB-PUB-040217-2'
FROM tmp_Zgc_Dblink_new;

-- Already exists. Check for prior existing attribution
INSERT into record_attribution
  (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
  )
SELECT zDblink_zdb_id, 'ZDB-PUB-040217-2'
FROM tmp_Zgc_Dblink_moved
WHERE NOT EXISTS 
  (
    Select *
    From record_attribution
    Where recattrib_data_zdb_id = zDblink_zdb_id
      and recattrib_source_zdb_id = 'ZDB-PUB-040217-2'
  );

-- Delete prior existing RefSeq attribution
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id IN (select zDblink_zdb_id from tmp_Zgc_Dblink_moved)
  AND recattrib_source_zdb_id = 'ZDB-PUB-020723-3'
;


!echo 'GENE'
SELECT *
FROM tmp_Zgc_Gene
WHERE zGENE_abbrev IN (Select mrkr_abbrev From marker)
INTO temp tmp_gene_duplicate;

UNLOAD to duplicate_gene.unl
SELECT * FROM tmp_gene_duplicate;

DELETE from tmp_Zgc_Gene
WHERE zGENE_zdb_id IN (Select g2.zGene_zdb_id From tmp_gene_duplicate AS g2);

DELETE from tmp_Zgc_MREL
WHERE zMREL_gene_zdb_id IN (Select g2.zGene_zdb_id From tmp_gene_duplicate AS g2);

DELETE from tmp_Zgc_Upgrade
WHERE zUpgrade_zdb_id IN (Select g2.zGene_zdb_id From tmp_gene_duplicate AS g2);


INSERT into zdb_active_data SELECT zGENE_zdb_id FROM tmp_Zgc_Gene;
INSERT into marker
  (
    mrkr_zdb_id,
    mrkr_name,
    mrkr_abbrev,
    mrkr_type,
    mrkr_comments,
    mrkr_owner
  )
SELECT 
    zGENE_zdb_id,
    zGENE_name,
    zGENE_abbrev,
    'GENE',
    zGENE_description,
    zGENE_owner
FROM tmp_Zgc_GENE;

INSERT into record_attribution
  (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
  )
SELECT zGENE_zdb_id, 'ZDB-PUB-040217-2'
FROM tmp_Zgc_GENE;


-- Mrel
INSERT into zdb_active_data SELECT zMREL_zdb_id FROM tmp_Zgc_MREL;
INSERT into marker_relationship
  (
    mrel_zdb_id,
    mrel_mrkr_1_zdb_id,
    mrel_mrkr_2_zdb_id,
    mrel_type
  )
SELECT 
    zMREL_zdb_id,
    zMREL_gene_zdb_id,
    zMREL_est_zdb_id,
    'gene encodes small segment'
FROM 
   tmp_Zgc_MREL;

INSERT into record_attribution
  (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
  )
SELECT zMREL_zdb_id, 'ZDB-PUB-040217-1'
FROM tmp_Zgc_MREL;


-- Upgrades



UPDATE marker
SET    mrkr_name = (SELECT zUpgrade_new_name FROM tmp_Zgc_Upgrade WHERE mrkr_zdb_id = zUpgrade_zdb_id AND zUpgrade_old_name = mrkr_name)
WHERE  mrkr_zdb_id IN (SELECT zUpgrade_zdb_id FROM tmp_Zgc_Upgrade);

UPDATE marker
SET    mrkr_abbrev = (SELECT zUpgrade_new_name FROM tmp_Zgc_Upgrade WHERE mrkr_zdb_id = zUpgrade_zdb_id AND zUpgrade_old_name = mrkr_abbrev)
WHERE  mrkr_zdb_id IN (SELECT zUpgrade_zdb_id FROM tmp_Zgc_Upgrade);

UPDATE marker_history
SET mhist_reason = 'renamed to conform with zebrafish guidelines'
WHERE mhist_mrkr_zdb_id IN (SELECT zUpgrade_zdb_id FROM tmp_Zgc_Upgrade)
  and mhist_event = "renamed"
  and mhist_mrkr_abbrev_on_mhist_date = (SELECT zUpgrade_old_name FROM tmp_Zgc_Upgrade WHERE mhist_mrkr_zdb_id = zUpgrade_zdb_id);

UPDATE marker_history
SET mhist_reason = 'renamed to conform with zebrafish guidelines'
WHERE mhist_mrkr_zdb_id IN (SELECT zUpgrade_zdb_id FROM tmp_Zgc_Upgrade)
  and mhist_event = "reassigned"
  and mhist_dalias_zdb_id = 
    (
      SELECT dalias_zdb_id 
      FROM data_alias, tmp_Zgc_Upgrade 
      WHERE dalias_alias = zUpgrade_old_name
        AND dalias_data_zdb_id = mhist_mrkr_zdb_id
    );


!echo 'Clone'
INSERT into clone
  (
    clone_mrkr_zdb_id,
    clone_vector_name,
    clone_probelib_zdb_id,
    clone_sequence_type,
    clone_comments    
  )
SELECT
    zClone_mrkr_zdb_id,
    zClone_vector,
    zClone_probelib_zdb_id,
    'cDNA',
    'ZGC load ' || TODAY
FROM tmp_Zgc_Clone;


--tell me the ZGC records missing a db_link
unload to 'unNoDbLink.unl'
select zEST_zdb_id
from tmp_zgc_est
where zEST_zdb_id not in (select dblink_linked_recid from db_link);


--tell me the ZGC records with RefSeq attribution
unload to 'unRefSeqAttrib.unl'
select zest_abbrev
from record_attribution, db_link, tmp_zgc_est
where zest_zdb_id = dblink_linked_recid
  and dblink_zdb_id = recattrib_data_zdb_id
  and recattrib_source_zdb_id = 'ZDB-PUB-020723-3';


--rollback work;  
commit work;