#! /usr/bin/nawk -f

# the rows with 'F' in the 5th col are finished
# the rows with 'N" in the 5th col are gaps
# omit the gaps.
#
# to turn agp into a gff3 file
#
# strip 'chr' off of $1  for seqname  
# 'Ensembl_finished'     for source  ($5=='F')
# 'clone'                for feature if not match ($6,"CABZ") != 1
# $2                     for start  ($2-$7 for full?)  
# $3                     for end    ($2-$7+$8 for full?) 
# '.'                    for score
# $9                     for strand
# '.'                    for frame
# ID=$1:$4;
# Name=$6.vers;          trim .vers  
# Alias=                 would be good to add the name later.

# sources allowed by the AGP specification
BEGIN {
	src["A"]="Active";
	src["D"]="Draft";
	src["F"]="Finished";
	src["G"]="Whole_Genome_Finishing";
	src["N"]="gap";
	src["O"]="Other";
	src["P"]="PreDraft";
	src["U"]="gap~";
	src["W"]="WGS";
}
# only accept rows not representing gaps
# in practice all of Ensembl source to date, 
# have only been gap or finished

$5 != "N" {
	#split($6,a,".");  
	$6=substr($6,1,index($6,".")-1); 
	feature="clone"; if(1==match($6,"CABZ")){feature="contig"};
	print substr($1,4) "\tEnsembl_" src[$5] "_trimmed\t" feature "\t" $2 "\t" $3 "\t.\t" $9 "\t.\tID="$1 ":" $4 ";Name=" $6
} 
