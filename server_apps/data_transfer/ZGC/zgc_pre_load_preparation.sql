begin work;

CREATE TABLE tmp_Zgc_Lib 
  (
    zLib_name    varchar(80),
    zLib_tissue  varchar(100),
    zLib_vector  varchar(80),
    junk         integer  
  );

CREATE index zLib_name_index
 ON tmp_Zgc_Lib(zLib_name)
 USING btree;
 
CREATE TABLE tmp_Lib_Bank 
  (
    Libbank_name      varchar(80),
    Libbank_zdb_id    varchar(50),
    Libbank_vec_name  varchar(80),
    Libbank_vec_type  varchar(20)
  ) ;


CREATE TABLE tmp_Zgc
  (
    zgc_mrkr_abbrev	varchar(40),
    zgc_defline		varchar(100),
    zgc_ll_id		varchar(20),
    zgc_cluster		varchar(20),
    zgc_image		varchar(20),
    zgc_acc_num		varchar(20),
    zgc_length		integer,
    zgc_lib_id		integer,
    zgc_lib		varchar(50),
    zgc_name		varchar(20),
    zgc_abbrev		varchar(40)
  );
  
CREATE index zgc_mrkr_abbrev_index
  ON tmp_Zgc(zgc_mrkr_abbrev)
  USING btree;

CREATE index zgc_name_index
  ON tmp_Zgc(zgc_name)
  USING btree;

CREATE index zgc_abbrev_index
  ON tmp_Zgc(zgc_abbrev)
  USING btree;

CREATE index zgc_lib_index
  ON tmp_Zgc(zgc_lib)
  USING btree;
  

LOAD from StaticLibList.unl INSERT into tmp_Zgc_Lib;
LOAD from fishlib.unl INSERT into tmp_Lib_Bank;
LOAD from StaticCloneList.unl INSERT into tmp_Zgc;


UPDATE STATISTICS HIGH FOR table tmp_Zgc_Lib;
UPDATE STATISTICS HIGH FOR table tmp_Lib_Bank;
UPDATE STATISTICS HIGH FOR table tmp_Zgc;

CREATE TABLE zgc_tmp_zname_mismatch
  (
    zmis_acc_num           varchar(20),
    zmis_zfin_mrkr_abbrev  varchar(50),
    zmis_zgc_mrkr_abbrev   varchar(50)
  );

CREATE TABLE zgc_tmp_lib_not_found
  (
    zlib_name              varchar(80)
  );

CREATE TABLE zgc_tmp_zlib_vector_not_found
  (
    zvec_name              varchar(80)
  );

CREATE TABLE zgc_tmp_acc_num_assigned_in_zfin
  (
    zaccmis_zfin_abbrev    varchar(40),
    zaccmis_mgc_abbrev     varchar(40),
    zaccmis_acc_num        varchar(20)
  );

CREATE TABLE zgc_tmp_no_gene
  (
    nogene_zfin_mrkr_abbrev	varchar(40),
    nogene_zgc_abbrev		varchar(40),
    nogene_acc_num              varchar(40)
  );

  
commit work;