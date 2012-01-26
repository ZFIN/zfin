README
recieve email with excel file, 
I am storing the incoming files in /research/zprod/data/BurgessLin/YYYY-MMM

export as csv (tab separated values actually)
	may need to turn off formating numbers with commas before exporting as cvs.
	tabs with no string quotes

make a consistant names for the files.
either like this or symlinks or however you would like

ln -s  /research/zprod/data/BurgessLin/2011-Oct/Plate7-12FinalSubmit_sheet1.csv sheet1
ln -s  /research/zprod/data/BurgessLin/2011-Oct/Plate7-12FinalSubmit_sheet2.csv sheet2
OR 
copy then cleanup

optional? I think I have it covered downstream but
convert to unix line endings is always a good idea.
you should use whatever method you normaly use
(I use ~tomc/bin/reline)

reline sheet1
reline sheet2

note: 
(first time) size drops so it can have windows line endings

# this handled by makefile but still ok to be vaguley cognizant of
./convert_sheet1.sh
./convert_sheet2.awk $sheet2 | sort -u > la_fish_parent.tab

########################################################
# load into zfin scripts happen here

Potential Problems with the Data:

* Duplicate Accession numbers
  -- cause the load to fail

* Repeated Accession numbers
  -- cause the load to fail

* Features in the genotype sheet that are not on the Consensus sheet
  -- removed by the load

* Features in the Consensus sheet that are not in the genotype sheet
  -- removed by the load

* Duplicate genotype
  -- removed by the load
  

Commands to run the load script:

gmake load_bl

gmake load_bl_commit

 is converting & running load_Burgess_Lin.sql
then cleaning up if commited
########################################################

# make the bed file available this happens sometime after the
# the data load is in and checked.
# 	o if you care about sanity store it in /zprod/data/BurgessLin/YYYY-MMM  
#	o whatever they named it, symlink it as  "BL.bed"
#	o push a copy to the "Global Store" /research/zprod/gff3/

gmake push_gff3

# this only make the datafile available for any instance to pull
# the actual pulling is done in data_transfer/Downloads/GFF3/

# we do not know how updates will happen so it is pointless
# make decisions about what we will do before we know what 
# our options are.

################################################################################
################################################################################




load notes from first trial dataset




rearrange the plate/well (fish), LA#, background to be loadable rows. The
awk script "plate_la_split.awk" handles the conversion from spreadsheet csv
to a database loadable format

./plate_la_split.awk TransgeneSubmissionTest2.csv  | sort >! la_fish_parent.tab


TransgeneSubmissionTest1.csv                84 rows with unique la #
	"Allele (la number provided by ZFIN)",
	"Genbank Accession # of the vector",
	"Genbank Accession # of the Flanking Sequences (accession for unique integraion site)",
	"Affected gene or site of insertion",
	"ZDB gene #",
	"Chromosome #",
	"Chromosome Q start",
	"Chromosome Q end"


# quick & dirty  (seems to drop ~ 10 unique la#)
#~/bin/csv2pipe TransgeneSubmissionTest1.csv | tr -d \' | grep "^la" | sort >! la_gb_chr_loc
#join -t '	' la_gb_chr_loc la_fish_parent.tab | tr '	' \| | tailpipe | sed 's/\(.*|\)chr\(.*\)/\1\2/g'  >! burgess_lin.unl
#la010071|JN244738|JM426434|notch3(intron2)||chr3|54190534|54190524||Plate 7 A10|T/AB-5|

redo:
 tr -d \' < TransgeneSubmissionTest1.csv | sed 's/\(.*\)\ \ *\(.*\)chr\(.*\)/\1\2\3  /g' | grep "^la01" | sort > ! la_gb_chr_loc.tab


##############################################################################
issues:

need to make a
NCBI  genomic survey sequence foreign db  'GenBank_GSS'
(draft done but need decision on blast database )
----
internal question: will the insertion flank sequences (GSS)
be in our local blast system? if so how added? and maintained?
-----
the zdbid field is null  (important! )
symbol in the insertion site field is no good.
	only identify the gene with zdbid.
	we change symbols, papers are published with random symbols
	we can not programaticly be sure what gene is intended from a symbol.
----
	if they want to put "intron x" in the site field that is fine but
	what happens when alternate splice transcripts come along?

-----
perfer not to have the  leading "chr" before the linkage group
	that is a UCSC convention. is not hard to strip off

the list of GSS accessions all have 7 or 8 trailing spaces...

these la numbers only exist on the first sheet
	la010000
	la010001
	la010002
	la010003
	la010004
	la010005
	la010006
	la010007
	la010008

these la numbers only exist on the second sheet
        la010021
        la010028
        la010061
        la010063
        la010066
        la010070
        la010082
        la010086

note to self:
Remember to turn off formating numbers with commas before exporting as cvs.

strand info is in the delta of the start/end coordinates  +/- 10
the integration site is "start"
and "end" is greater by ten if forward strand or less by ten if reverse strand
(works for me no problem.)


parents:  "T/AB-5"  neither "T" nor "AB-5" are current strains on
http://zfin.org/cgi-bin/webdriver?MIval=aa-wtlist.apg
note 1: the Strain given in the GenBank report is "TAB-5"
note 2: there is a wildtype line 	AB/Tuebingen  AB/TU
	which mentions both TAB-5 and TAB-14, may be inclusive.





