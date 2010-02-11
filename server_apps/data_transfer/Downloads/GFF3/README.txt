
The readme from before adding to SVN
-------------------------------------------------------------
wget -r "ftp://ftp.sanger.ac.uk/pub/grit/wc2/20100111/*.tgz"
cd ftp.sanger.ac.uk/pub/grit/wc2/20100111/
gunzip *.tgz
tar -xvf drerio_vega_gff3dump_chr1-25.tar
tar -xvf drerio_ensembl_gff3dump_chr1-25.tar

# we have decided to filter out the introns as they should be implicit in the exons
# mind the tabs
cat drerio_vega_gff3dump_chr[123456789].out drerio_vega_gff3dump_chr1[0123456789].out drerio_vega_gff3dump_chr2[012345].out | egrep -v '^$|^##sequence-region|	intron'  >! drerio_vega.gff3
cat drerio_ensembl_gff3dump_chr[123456789].out  drerio_ensembl_gff3dump_chr1[0123456789].outdrerio_ensembl_gff3dump_chr2[012345].out | egrep -v '^$|^##sequence-region|	intron' >! drerio_ensembl.gff3

cp drerio_*.gff3 ../../../../../
cd -

# in the first rational example from Sanger the gene length was never less than other choices
# Gbrowse does not like the LGs to stert with 1 so ptint them in reverse order
get_final_gene.awk drerio_vega.gff3 >! vega_chromosome.gff3
get_final_gene.awk drerio_ensembl.gff3 >! ensembl_chromosome.gff3


# MIND THE TABS
cat drerio_vega.gff3 drerio_ensembl.gff3 | cut -f9 | tr \= '	' | tr \; '	' | cut -f 1,3,5,7,9 | sort -u

ID      Name    Parent  biotype
ID      Name    biotype
Name    Parent

% cat drerio_vega.gff3 | cut -f3,9 | tr \= '  ' | tr \; '     ' | cut -f 1,2,4,6,8,10 | sort -u

                                                ID       NAME     PARENT
CDS                Name    Parent                        OTTDARP  OTTDART
exon               Name    Parent                        OTTDARE  OTTDART
intron             Name    Parent                        intronN  OTTDART
gene       ID      Name            biotype      OTTDARG  OTTDARG   NULL
transcript ID      Name    Parent  biotype      OTTDART  OTTDART  OTTDARG



### convert the gff3 files into informix unload files
rebol -sq parse-gff3-unl.r drerio_vega.gff3 >! drerio_vega.unl
rebol -sq parse-gff3-unl.r drerio_ensembl.gff3 >! drerio_ensembl.un
## although it is not needed for valid gff3 formats
## gbrowse would rather each row had a unique ID field
## only the transcript and gene come with unique IDs
## introns come with a unique per LG id but are dropped earlier
## to make slightly meaningful IDs take the name and order by start position
## if there are miltiple names append :count
to put the file in order they need to be sorted by name then LG & LOC

sort -t \| -k10,10 -k1,1n -k4,4n drerio_vega.unl | generate_id.awk > ! drerio_vega_id.unl


### load the .unl files
dbaccess -a $DBNAME load_gff3_unl.sql

### start playing

select  source[1,11], id[1,7],feature[1,11], biotype[1,35], count(*)howmany
from gff3 group by 1,2,3,4
order by 1,2,3,4


select count (id), count (distinct id)
 from gff3 where source = 'vega' and feature = 'transcript';

select count(dblink_acc_num)
  from db_link where dblink_acc_num[1,8] = 'OTTDART0';

# definatly not the current vega load
select dblink_acc_num from db_link
 where dblink_acc_num[1,8] = 'OTTDART0'
   and not exists (
	select 't' from gff3
	 where source = 'vega'
	   and feature = 'transcript'
	   and id = dblink_acc_num
);


## location for longest/shortest "ZFIN" genes when projected on the assembly

select first 10 mrkr_abbrev[1,20], seqname, min (start), max(end),  max(end) - min (start)
 from gff3, marker gene, db_link, marker_relationship
 where dblink_acc_num = id
   and dblink_linked_recid = mrel_mrkr_2_zdb_id
   and mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and mrkr_type = 'GENE'
   --and seqname <> "AB" and seqname <> "U" -- these are wonky
group by 1,2
order by 5 desc
;


select first 10 mrkr_abbrev[1,20], seqname, min (start), max(end),  max(end) - min (start)
 from gff3, marker gene, db_link, marker_relationship
 where dblink_acc_num = id
   and dblink_linked_recid = mrel_mrkr_2_zdb_id
   and mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and mrkr_type = 'GENE'
   --and seqname <> "AB" and seqname <> "U" -- these are wonky
group by 1,2
order by 5 asc
;



