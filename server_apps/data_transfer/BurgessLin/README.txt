


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

the DBA script to update the schema for the new fields does not run to compleation.

fixed.

