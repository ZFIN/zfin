UNLOAD to 'zName_mismatch.unl'
  SELECT *
  FROM zgc_tmp_zname_mismatch;

UNLOAD to 'zLib_not_found.unl'
  SELECT * FROM zgc_tmp_Lib_not_found;

UNLOAD to 'zLib_vector_not_found.unl'
  SELECT * FROM zgc_tmp_zLib_vector_not_found;

UNLOAD to 'refseq_relation.unl'
  SELECT * FROM zgc_tmp_acc_num_assigned_in_zfin;

UNLOAD to 'no_gene_cdna.unl'
  SELECT * FROM zgc_tmp_no_gene;
  
  
DROP TABLE tmp_Zgc_Lib; 
DROP TABLE tmp_Lib_Bank ;
DROP TABLE tmp_Zgc;
DROP TABLE zgc_tmp_zname_mismatch;
DROP TABLE zgc_tmp_lib_not_found;
DROP TABLE zgc_tmp_zlib_vector_not_found;
DROP TABLE zgc_tmp_acc_num_assigned_in_zfin;
DROP TABLE zgc_tmp_no_gene;
