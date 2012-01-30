-- --------------------------------------------------------------------- 
{
This script adds the new MGC cDNA records from ZGC. Associated data is also added.

I. Delete zfin/zgc redundancy (zfin zgc record that maps to the same marker abbrev)
   Unload discrepancies from Zgc_tmp (ncbi abbrev != zfin abbrev)
   Unload non-library from Zgc_tmp
   -- unloads are now loaded into tables for unload outside the function.

II. Create new records
     A. Add zgc ESTs
        Create temp Marker EST records for each Zgc_tmp
        Move BC GenBank links from GENE records to zgc EST (update Zgc_tmp.mrkr_name)
        Create BC GenBank links for empty zgc ESTs
        Create clone records for zgc ests
     B. Add zgc GENEs
        Upgrade bonus genes
        Create temp GENE records
     C. Add temp marker_relationships

III. Move data from temp tables into production
     A. Create zdb_active_data records
     B. Move gene, EST, clone, marker_relationship records
} 
-- INPUT VARS: 
--   None 
-- 
-- OUTPUT VARS: 
--   None
-- 
-- RETURNS: 
--   -1 for error, 0 for success 
-- 
-- EFFECTS: 
-- New Db_link, EST, Gene, Marker_Relationship, Clone, and Attribution
-- records.
-- 
-- Nomenclature changes for ':' Genes upgraded to zgc genes.
-- ---------------------------------------------------------------------


create function p_zgc_load() returning int;

  begin	-- global exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);


    on exception
      set sqlError, isamError, errorText
      begin

	on exception in (-255, -668)
          --  255: OK to get a "Not in transaction" here, since
          --       we might not be in a transaction when the rollback work 
          --       below is performed.
          --  668: OK to get a "System command not executed" here.
          --       Is probably the result of the chmod failing because we
          --       are not the owner.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
                               ' SQL Error: ' || sqlError::varchar(200) || 
                               ' ISAM Error: ' || isamError::varchar(200) ||
                               ' ErrorText: ' || errorText ||
                               ' ErrorHint: ' || errorHint ||
                               '" >> /tmp/zgc_load_exception.<!--|DB_NAME|-->';
        system exceptionMessage;

        -- Change the mode of the zgc_load_exception file.  This is
        -- only needed the first time it is created.  This allows us to 
        -- rerun the function from dbaccess as whatever user we want, and
	-- to reuse an existing regen_anatomy_exception file.

        system '/bin/chmod 666 /tmp/zgc_load_exception.<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	return -1;
      end
    end exception;

  

    BEGIN WORK;


    let errorHint = "Creating temp tables";
    -------------------
    --| Temp Tables |--
    -------------------
	
	
    CREATE TEMP TABLE tmp_Zgc_EST
      (
        zEST_zdb_id 	varchar(50),
        zEST_name		varchar(255),
        zEST_abbrev		varchar(150),
        zEST_description	lvarchar,
        zEST_owner		varchar(50)
      )
    with no log;

    CREATE index zEST_abbrev_index
      ON tmp_Zgc_EST(zEST_abbrev)
      USING btree;
  

    CREATE TEMP TABLE tmp_Zgc_GENE
      (
        zGENE_zdb_id 	varchar(50),
        zGENE_name		varchar(255),
        zGENE_abbrev	varchar(150),
        zGENE_description	lvarchar,
        zGENE_owner		varchar(50)
      )
    with no log;

    CREATE index zGENE_abbrev_index
      ON tmp_Zgc_GENE(zGENE_abbrev)
      USING btree;
  

    CREATE TEMP TABLE tmp_Zgc_Clone
      (
        zClone_mrkr_zdb_id 		varchar(50),
        zClone_vector		varchar(80),
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
        zUpgrade_old_name	varchar(255),
        zUpgrade_new_name	varchar(255),
        zUpgrade_length	integer
      )
    with no log;

    CREATE index zUpgrade_zdb_id_index
      ON tmp_Zgc_Upgrade(zUpgrade_zdb_id)
      USING btree;



    let errorHint = "Update qualifying ncbi abbrevs";
   
	--   if the ncbi abbrev is a small segment in ZFIN
	--     and ncbi_abbrev != zfin abbrev 
	--     and the sement has a marker_relationship with the zfin gene
	--   change the target to the zfin abbrev 

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


    let errorHint = "Delete zfin/zgc redundancy";
    
    
    INSERT INTO zgc_tmp_no_gene
    SELECT zgc_mrkr_abbrev, zgc_abbrev, zgc_acc_num
      FROM tmp_zgc, marker
     WHERE zgc_abbrev = mrkr_abbrev
       AND NOT EXISTS
         (
           select *
             from marker_relationship
            where mrel_mrkr_2_zdb_id = mrkr_zdb_id
              and mrel_type = "gene encodes small segment"
         );
    
    --  Delete zfin/zgc redundancy 
	
    DELETE from tmp_Zgc
    WHERE exists
        (
          select *
          from marker 
          where mrkr_abbrev = zgc_abbrev
        );


    let errorHint = "Delete zfin/zgc redundancy";
    --   Discrepancies
    --   Unload if marker related (ncbi abbrev != zfin abbrev)

    INSERT into tmp_zName_Mismatch
    SELECT zgc_acc_num, zgc.mrkr_zdb_id, zfin.mrkr_zdb_id
    FROM tmp_Zgc, marker AS zfin, marker AS zgc, marker_relationship
    WHERE mrel_mrkr_1_zdb_id = zfin.mrkr_zdb_id
      and mrel_mrkr_2_zdb_Id = zgc.mrkr_zdb_id
      and zgc.mrkr_abbrev = zgc_abbrev
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

  
    INSERT into zgc_tmp_zname_mismatch
      (
        zmis_acc_num,
        zmis_zfin_mrkr_abbrev,
        zmis_zgc_mrkr_abbrev
      )  
    SELECT zMis_acc_num, zfin.mrkr_abbrev, zgc.mrkr_abbrev
    FROM tmp_zName_mismatch, marker AS zfin, marker AS zgc
    WHERE zMis_zgc_zdb_id = zgc.mrkr_zdb_id
      and zMis_zfin_zdb_id = zfin.mrkr_zdb_id;

  
    DELETE from tmp_Zgc 
    WHERE zgc_acc_num IN 
      (
        SELECT zMis_acc_num FROM tmp_zName_mismatch
      );
  


    let errorHint = "Unload duplicate libraries.";  
    --  Unload non-library from Zgc_tmp
    --  only load new libraries that are ZGC libraries


    DELETE FROM tmp_Lib_Bank
    WHERE libbank_name NOT IN 
      (
        SELECT zLib_name 
        FROM tmp_Zgc_Lib
      );

    -- Add New Vectors to the Database
    -- tmp_vector is the set of vectors
    SELECT distinct libbank_vec_name as vec_name, libbank_vec_type as vec_type
    FROM tmp_lib_bank
    INTO TEMP tmp_vector;

    -- delete existing vectors
    Delete from tmp_vector
    WHERE vec_name in (SELECT vector_name FROM vector);

    -- prevent duplicate vector_types
    update tmp_vector
    set vec_type = NULL
    where vec_type in (select vectype_name from vector_type);

    -- insert new vector_types
    INSERT INTO vector_type (vectype_name,vectype_comments)
    SELECT distinct vec_type, 'Automated Process: ZGC load '|| TODAY
    FROM tmp_vector
    WHERE vec_type is not null;

    -- Cross original set of vector values with remaining set of modified vectors
    -- otherwise vec_type might be null.
    INSERT INTO vector (vector_name,vector_type_name)
    SELECT distinct libbank_vec_name, libbank_vec_type
    FROM tmp_vector, tmp_lib_bank
    WHERE libbank_vec_name = vec_name;

    Update tmp_Lib_Bank
    SET libbank_zdb_id = (select probelib_zdb_id from probe_library where probelib_name = libbank_name);

    update tmp_lib_bank
    set libbank_zdb_id = 'x'||get_id('PROBELIB')
    where libbank_zdb_id is null;


    INSERT INTO zdb_active_data(zactvd_zdb_id)
    SELECT libbank_zdb_id[2,50] 
    FROM tmp_Lib_Bank
    WHERE libbank_zdb_id[1] = 'x';


    INSERT INTO probe_library
     (probelib_zdb_id, probelib_name, probelib_species)
    SELECT distinct libbank_zdb_id[2,50],libbank_name,'Danio rerio'
    FROM tmp_Lib_Bank
    WHERE libbank_zdb_id[1] = 'x';

    Update tmp_Zgc_lib
    set zLib_vector = (select libbank_vec_name from tmp_lib_bank where libbank_name = zLib_name)
    where zLib_name in (select libbank_name from tmp_lib_bank);

    INSERT INTO zgc_tmp_lib_not_found    
    SELECT zLib_name 
    FROM tmp_Zgc_Lib
    WHERE zLib_name NOT IN 
      (
        SELECT probelib_name FROM probe_library
      )
      OR zLib_vector is NULL;
      

    DELETE from tmp_Zgc
    WHERE zgc_lib IN
      (
        SELECT * from zgc_tmp_Lib_not_found
      )
      or zgc_lib is NULL;
  


    --clones require a library and a vector. however,
    --libraries and vectors are independent entities in ZFIN.
    --check the doc for an explanation.

    --catch occurences of a library existing in zfin but 
    --the vector does not exist.  
    
    INSERT into zgc_tmp_zlib_vector_not_found
    SELECT libbank_vec_name
    FROM tmp_lib_bank
    WHERE libbank_vec_name NOT IN 
      (
        SELECT vector_name FROM vector
      );


    DELETE from tmp_Zgc
    WHERE zgc_lib IN
      (
        SELECT libbank_name 
        from zgc_tmp_zLib_vector_not_found, tmp_lib_bank
        where zvec_name = libbank_vec_name
      );



    let errorHint = "Create ZGC records.";
    --------------------------
    --| Create new records |--
    --------------------------

    --  Add zgc ESTs
    --  Create temp Marker EST records for each Zgc_tmp

    INSERT into tmp_Zgc_EST
    SELECT get_id('CDNA'), 
        zgc_name,
        zgc_abbrev,
        '',
        'ZDB-PERS-010716-1'
    FROM tmp_Zgc;
        
    -- Move BC GenBank links from GENE records to zgc EST (update Zgc_tmp.mrkr_name)
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
    

    INSERT into zgc_tmp_acc_num_assigned_in_zfin
      (
        zaccmis_zfin_abbrev,
        zaccmis_mgc_abbrev,
        zaccmis_acc_num
      )
    SELECT mrkr_abbrev, zgc_abbrev, zgc_acc_num
    FROM tmp_zgc, tmp_zgc_dblink_moved, marker, db_link
    WHERE zDblink_acc_num = dblink_acc_num
      AND dblink_linked_recid = mrkr_zdb_id
      AND zgc_acc_num = zDblink_acc_num
      AND zgc_mrkr_abbrev is null;


    update tmp_Zgc
    set zgc_mrkr_abbrev = 
      (
        select mrkr_abbrev 
        from marker, db_link, tmp_zgc_dblink_moved 
        where zDblink_acc_num = dblink_acc_num
          and dblink_linked_recid = mrkr_zdb_id
          and zgc_acc_num = zDblink_acc_num
      )
    where zgc_acc_num in (select zDblink_acc_num from tmp_Zgc_Dblink_moved);


    let errorHint = "Create Genbank records.";
    --        Create BC GenBank links for empty zgc ESTs

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
        'uncurated: ZGC load ' || TODAY,
        zgc_length
    FROM
        tmp_Zgc, foreign_db_contains, tmp_Zgc_EST, foreign_db, 
	foreign_db_data_type
    WHERE
        zgc_acc_num NOT IN (select zDblink_acc_num from tmp_Zgc_Dblink_moved)
        AND fdb_db_name = 'GenBank'
        AND fdbdt_data_type = 'RNA'
        AND zgc_name = zEST_name
	AND fdbcont_fdb_db_id = fdb_db_pk_id
	AND fdbcont_fdbdt_id = fdbdt_pk_id;


    let errorHint = "Create Clone records.";
    -- Create clone records for zgc ests

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
        tmp_Zgc, tmp_Zgc_EST, tmp_Zgc_Dblink_moved, tmp_Zgc_lib, probe_library
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
        libbank_vec_name,
        probelib_zdb_id
    FROM 
        tmp_Zgc, tmp_Zgc_EST, tmp_Zgc_Dblink_new, tmp_lib_bank, probe_library
    WHERE 
        zgc_acc_num = zDblink_acc_num
        AND zDblink_linked_recid = zEST_zdb_id
        AND zgc_lib = libbank_name
        AND libbank_name = probelib_name;


    let errorHint = "Create zgc Gene records.";
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
        'zgc' || zgc_name[4,20],
        'zgc' || zgc_name[4,20],
        "This gene is characterized by full length cDNAs isolated as part of the Zebrafish Gene Collection (ZGC). When more is known about the gene, the current nomenclature will be replaced with more traditional zebrafish gene nomenclature. The prefix `zgc:' indicates that this gene is represented by cDNAs generated by the ZGC project.",
        'ZDB-PERS-010716-1'
    FROM
        tmp_Zgc, tmp_Zgc_Dblink_new
    WHERE
        zgc_mrkr_abbrev NOT IN (select mrkr_abbrev from marker where mrkr_type = "GENE")
        OR zgc_mrkr_abbrev is NULL
        and zgc_acc_num = zDblink_acc_num;


    let errorHint = "Upgrade ':' genes";
    -- Upgrade bonus genes'
    
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
        'zgc'||zgc_name[4,20],
        zgc_length
    FROM tmp_Zgc, marker
    WHERE zgc_mrkr_abbrev[1,3] not in ('si:','zgc') 
      and zgc_mrkr_abbrev like "%:%"
      and zgc_mrkr_abbrev = mrkr_abbrev
      and mrkr_type = 'GENE';
    
    --  Choose the longest zgc and remove the shorter zgc '

    SELECT 
        mrkr_zdb_id AS zLong_zdb_id,
        max(zgc_length) AS zLong_length
    FROM tmp_Zgc, marker
    WHERE zgc_mrkr_abbrev[1,3] not in ('si:','zgc') 
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
    WHERE zUpgrade_new_name IN 
      (
        SELECT 'zgc'||zFlip_name[4,20] 
        FROM tmp_zgc_Upgrade_Coin_flip
      );




    let errorHint = "Create MREL records.";
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
        zgc_abbrev = zEST_abbrev
        AND zgc_mrkr_abbrev = mrkr_abbrev    
        AND get_obj_type(mrkr_zdb_id) = "GENE";


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
        zgc_abbrev = zEST_abbrev
        AND 'zgc'||zgc_name[4,20] = zGENE_abbrev;


    let errorHint = "Move Temp records into Production.";
    -----------------------------------------
    --| Move temp records into production |--
    -----------------------------------------

    INSERT into zdb_active_data SELECT zEST_zdb_id FROM tmp_Zgc_EST;

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


    -- NEW DB_LINKs
    
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

    -- MOVED DB_LINKs. Check for prior existing attribution
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


    -- GENE

    SELECT *
    FROM tmp_Zgc_Gene
    WHERE zGENE_abbrev IN (Select mrkr_abbrev From marker)
    INTO temp tmp_gene_duplicate;


    DELETE from tmp_Zgc_Gene
    WHERE zGENE_zdb_id IN (Select g2.zGene_zdb_id From tmp_gene_duplicate AS g2);

    DELETE from tmp_Zgc_MREL
    WHERE zMREL_gene_zdb_id IN (Select g2.zGene_zdb_id From tmp_gene_duplicate AS g2);



    let errorHint = "Remove upgrade duplicates.";
    -- Remove upgrade duplicates


    DELETE from tmp_Zgc_Upgrade
    WHERE zUpgrade_zdb_id IN (Select g2.zGene_zdb_id From tmp_gene_duplicate AS g2);



    -- zgc: Genes are created for CDNAs without a gene assignment. CDNA are identified
    -- by a BC accession number. If multiple dblinks are associated with the same CDNA
    -- then redundant zgc: Genes are generated.

    -- 1. Find duplicates. 
    -- 2. Delete marker_relationship records with duplicates. 
    -- 3. Delete duplicate gene records.

    SELECT zGENE_name AS zgene_unique_name, MAX(zGENE_zdb_id) AS zgene_unique_zdb_id
    FROM tmp_Zgc_Gene 
    GROUP BY 1 
    INTO temp tmp_gene_unique;

    SELECT zGENE_name AS zgene_dup_name, zGENE_zdb_id AS zgene_dup_zdb_id
    FROM tmp_ZGC_Gene
    WHERE NOT EXISTS 
      (  
        select * 
        from tmp_gene_unique
        where zGENE_name = zgene_unique_name
          and zGENE_zdb_id = zgene_unique_zdb_id
      )
    INTO temp tmp_gene_dup;

    DELETE FROM tmp_Zgc_Mrel
    WHERE EXISTS
      (
        select *
        from tmp_gene_dup
        where zgene_dup_zdb_id = zMREL_gene_zdb_id
      );

    DELETE FROM tmp_Zgc_Gene
    WHERE EXISTS
      (
        select * 
        from tmp_gene_dup
        where zgene_dup_zdb_id = zGENE_zdb_id
      );

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
    

    let errorHint = "Upgrade : genes.";
    -- Upgrades'

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

    UPDATE marker
    SET mrkr_comments = "This gene is characterized by full length cDNAs isolated as part of the Zebrafish Gene Collection (ZGC). When more is known about the gene, the current nomenclature will be replaced with more traditional zebrafish gene nomenclature. The prefix `zgc:' indicates that this gene is represented by cDNAs generated by the ZGC project."
    WHERE mrkr_zdb_id IN (SELECT zUpgrade_zdb_id FROM tmp_zgc_upgrade);

    -- Attribute dblinks of upgraded genes; Check for prior existing attribution

    INSERT into record_attribution
      (
        recattrib_data_zdb_id,
        recattrib_source_zdb_id
      )
    SELECT dblink_zdb_id, 'ZDB-PUB-040217-2'
    FROM tmp_Zgc_Upgrade, tmp_Zgc, db_link, marker_relationship
    WHERE zUpgrade_old_name = zgc_mrkr_abbrev
      AND zgc_acc_num = dblink_acc_num
      AND zUpgrade_zdb_id = mrel_mrkr_1_zdb_id
      AND mrel_mrkr_2_zdb_id = dblink_linked_recid
      AND NOT EXISTS 
      (
        Select *
        From record_attribution
        Where recattrib_data_zdb_id = dblink_zdb_id
          and recattrib_source_zdb_id = 'ZDB-PUB-040217-2'
      );

    --Delete prior attributions of BC records where attribution is ZFIN curation

    SELECT zfin.recattrib_data_zdb_id as rec_data, zfin.recattrib_source_zdb_id as rec_source
    FROM record_attribution zfin, record_attribution zgc, publication
    WHERE jtype = "Curation"
      AND zdb_id = zfin.recattrib_source_zdb_id
      AND zfin.recattrib_data_zdb_id = zgc.recattrib_data_zdb_id
      AND zgc.recattrib_source_zdb_id = 'ZDB-PUB-040217-2'
    INTO TEMP tmp_recattrib;

    DELETE FROM record_attribution
    WHERE exists
      (
        SELECT *
        from tmp_recattrib
        where recattrib_data_zdb_id = rec_data
          and recattrib_source_zdb_id = rec_source 
      );

    let errorHint = "Insert Clones.";
    -- CLONE

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


    -- supply the clone from ZGC

    insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id)
    select mrkr_zdb_id, "ZDB-LAB-040601-1" 
    from marker, tmp_Zgc_Clone
    where mrkr_zdb_id = zClone_mrkr_zdb_id;


    --rollback work;  
    commit work;

  end -- Global exception handler
  return 0;
end function;