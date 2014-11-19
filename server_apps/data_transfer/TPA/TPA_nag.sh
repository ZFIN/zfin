#! /bin/sh
# TPA_nag.sh
/bin/cat  << END
GenBank ftp files do not include Third Party Annotation (TPA) records
so they are not added to accession_bank
(per: ftp://ftp.ncbi.nih.gov/genbank/README.genbank)
so TPA accession lengths are not updated when curators add them.

As of August 2008 ZFIN had ~50 such gnebank links
rather than find them and figure out how to add them to accession_bank

we can update their lengths manually to db_link now
and see if it is a problem worth fixing.

select dblink_acc_num --,count(*) howmany
 from db_link
 where dblink_length is NULL
   and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-36','ZDB-FDBCONT-040412-37') --DNA,RNA
 group by 1 order by 1


stick the trimmed result in a file, say: <input.list>

note:
this SQL may pick up other curatorial problems such as includeing the version
of the Genbank accession which will cause it not to be matched or errant spaces.


the TPA accessions I see so far are all of the form B[NK]nnnnnn


the full records for these accessions can be fetched from Entrez

> /research/zusers/tomc/efetch.r -i <input.list>  -o <ouput.flat>

a load file of accession and length can be made with

> grep "^LOCUS" <ouput.flat> | cut -c13-40 | sed 's/\([A-Z0-9]*\) *\([0-9]*\)/\1|\2|/g' > acc_length.unl

then:

begin work;
create table tmp_acc_len (tal_acc varchar(40) , tal_len integer) in defaultdbs ;
load from 'acc_length.unl' insert into tmp_acc_len;

select dblink_acc_num[1,20] acc, dblink_length old_len, tal_len new_len
 from db_link,tmp_acc_len
 where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-36','ZDB-FDBCONT-040412-37') --DNA,RNA
 and dblink_acc_num = tal_acc               
;

update db_link
 set dblink_length = (select tal_len from tmp_acc_len where dblink_acc_num = tal_acc)
 where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-36','ZDB-FDBCONT-040412-37') --DNA,RNA
   and exists (select 1 from tmp_acc_len where dblink_acc_num = tal_acc) 
;

drop table tmp_acc_len;
--
rollback work;
--commit work;

---------------------------------------------
--should also look for GB accessions with dot version and fix(truncate) them

select dblink_acc_num,count(*) howmany 
 from db_link
 where dblink_acc_num[9] = '.'
   and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-37','ZDB-FDBCONT-040412-36') --DNA,RNA
 group by 1 order by 1                
;

update db_link 
 set dblink_acc_num = dblink_acc_num[1,8] 
 where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-37','ZDB-FDBCONT-040412-36') --DNA,RNA
   and dblink_acc_num[9] = '.'
;




END
