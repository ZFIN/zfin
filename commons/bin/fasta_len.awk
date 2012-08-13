#! /local/bin/gawk -f
# extracts accession & length of sequence from NCBI fasta records
# if the accession is not in the fourth position change at the commandline

BEGIN{FS="|"; ACC=4; a=""; l=0}
/^>/ {
	if(a!="") print a "|" l "|";
	if(match($ACC,/\./)){a=substr($ACC,1,RSTART-1)}else{a=$ACC}
	l=0
}
# I suppose any ambiguity code ... or protein. so start with loose bound
/^[A-Za-z]/{l+=length($0)}
END{print a "|" l "|"}
