
-- make a fasta defline that will also work as a gff3 attribute column

unload to mo_seq.fa_line delimiter " "
select ">ID=;Name=" || mrkr_abbrev || ";zdb_id="|| mrkr_zdb_id || ";~" || mrkrseq_sequence
 from marker, marker_sequence
 where mrkr_zdb_id[1,12] = 'ZDB-MRPHLNO-'
   and mrkrseq_mrkr_zdb_id = mrkr_zdb_id
   --and  mrkr_zdb_id != 'ZDB-MRPHLNO-070130-6'  --TGTGTGTGTGTGTGTGTGTGAGCAC
;
-- rearange .unl format to fasta format
!tr \~ '\n' < mo_seq.fa_line >! mo_seq.fa
