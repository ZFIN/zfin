#! /usr/bin/nawk -f

# the rows with 'F' in the 5th col are "finished"
# the rows with 'N" in the 5th col are "gaps"
# omit the gaps.
#
# to turn agp into a gff3 file
#
# strip 'chr' off of $1  for seqname  
# 'Ensembl_finished'     for source  ($5=='F')
# 'clone'                for feature if not match ($6,"CABZ") != 1
# $2                     for start  ($2-$7 for full?)  
# $3                     for end    ($2-47+$8 for full?) 
# '.'                    for score
# $9                     for strand
# '.'                    for frame
# ID=$1:$4;
# Name=$6.vers;          trim .vers  
# Alias=                 augment with zfin clone unload.

BEGIN			{
	zrow = 1;
	while((getline < "zfin_DNA_clone.txt") > 0){
		z[zrow,1] = $1;  # acc
		z[zrow,2] = $2;  # name
		z[zrow,3] = $3;	 # zdbid
		z[zrow,4] = $4;  # length
		zrow++
	};
	zmax = zrow; 
	zrow = 1;
	OFS = "\t";
	# from agp def
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

$5 != "N" {
	$6=substr($6,1,index($6,".")-1); #chop accession version
	while( (z[zrow,1] < $6) && (zrow <= zmax)){ zrow++ };
	feature="clone"; 
	if(1 == match($6,"CABZ")){feature="contig"};
	start = $2; 
	end = $3;
	
	if($6 == z[zrow,1]){
		if($9 == "+"){ 
			start = $2-$7-1; end = start + z[zrow,4]
		}else if($9 == "-"){ 
			end   = $3+$7+1  ; start = end - z[zrow,4]
		}
		print substr($1,4) "\tEnsembl_" src[$5] "\t" feature "\t" start "\t" end "\t.\t" $9 "\t.\tID="$1 ":" $4 "T;Name=" z[zrow,2] ";zdb_id=" z[zrow,3] ";Alias=" z[zrow,1] "," z[zrow,3]
	} else{
		print substr($1,4) "\tEnsembl_" src[$5] "\t" feature "\t" start "\t" end "\t.\t" $9 "\t.\tID="$1 ":" $4 "T;Name=" $6 
	}
} 



