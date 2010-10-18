
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
## to put the file in order they need to be sorted by name then LG & LOC

sort -t \| -k10,10 -k1,1n -k4,4n drerio_vega.unl | generate_id.awk > ! drerio_vega_id.unl


### load the .unl files
dbaccess -a $DBNAME load_drerio_vega_id.sql

