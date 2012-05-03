#! /usr/bin/nawk -f
# for_tom_2_unl

# Turn Sanger's deflines of incomming transcripts into 
# an informix loadable file.
# known irregularities are:
# 	the  "no translation"  value looks like two fields
# 	sometimes they leave the "gene_type" field empty

/^>OTTDART/ {
	row="";
	$1=substr($1,2);
	if ($9 !~ /^OTTDARP/){$9="";for(i=10;i<NF;i++){$i=$(i+1)};NF--};
	if ($20 != "clone"){NF++;$21=$20;$20=$19;$19=""};
	for(i=1;i<=NF;i+=2){row=row $i "|"};
	if (NF != 21){print $0 > "/dev/stderr"}else{print row}
}
