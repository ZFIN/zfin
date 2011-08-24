We  will be leaving the gff3 table in the database and truncating and
reloading when vega or ensembl releases come along.
this means there it does not make sense to update all the tracks with the same frequency
the assembly backbones clone tracks for both Vega and Ensembl only need 
to be updated when there is a new load.  morphalinos and other features 
that are aligned to the assenblies by us should also not be updated often.
tracks bases on genes whos name change get merged etc should be  updated
more often. 

so re partition the existing scripts by frequency they run

0) sehema creation (typically) just once. include index definition.

1) data loading whenever Vega or Ensembl change (just the set that changed)
within a transaction

2) tracks for features aligned to the backbone by us; eg. morpholinos


3) ZFIN gbrowse tracks updated frequently to reflect nomenclature changes


schema change:

echo "drop table gff3" | dbaccess -a $DBNAME 
dbaccess -a $DBNAME create_gff3.sql



load the Ensembl and Vega data, 
Ensembl note;  use the gtf2gff3.pl from SO, not the one from SeqAnswers. 
 	requires making gtf2gff3.pl accept gtf on stdin and increasing the 
 	width of columns created in the gff3 table.

loading the Ensembl backbone includes fetching the fasta file and extracting 
a chromosome/contig file from it.

the toplevel Ensembl fasta is indexed with bowtie. (about four hours)
/bin/time nice +10 \
/private/bin/bowtie-build --big -f  Danio_rerio.Zv9.62.dna.toplevel.fa Ensembl_Zv9.62


a current set of morpholinos are extracted from the DB


 align morpholinos with toplevel Ensembl fasta 
~/bin/bowtie --best -strata --sam -f /research/zprodmore/gff3/Vega_Zv9_40 all_mo.fa >! all_mo2.sam


nawk -f sam2gff3.awk < all_mo.sam > ! all_mo.gff3

sort -k9,9 -k 1,1n -k 4,4n  all_mo.gff3 | increment_id.awk >! all_mo_bowtie.gff3












########################################################################
########################################################################
########################################################################
Historical as of 2011 Apr 
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
# Gbrowse does not like the LGs to stert with 1 so print them in reverse order
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
## to put the file in order they need to be sorted by name then LG & LOC

sort -t \| -k10,10 -k1,1n -k4,4n drerio_vega.unl | generate_id.awk > ! drerio_vega_id.unl


### load the .unl files
dbaccess -a $DBNAME load_drerio_vega_id.sql

