#! /usr/bin/nawk -f
#  file: plate_la_split.awk
#  usage: ./convert_sheet2.awk sheet2  | sort -u >! la_fish_parent.tab

BEGIN {FS="\t";OFS="\t"}

/^Plate.*/ {
	split($2,a,",");
	for(x in a){
		gsub(/ /,"",a[x]);
		print a[x],$1,$3 "\t"
	}
}
