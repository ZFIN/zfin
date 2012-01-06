#! /local/bin/gawk -f
#  file: plate_la_split.awk

BEGIN	{FS= "\t" ; OFS="\t"}

# usage:
# ./plate_la_split.awk TransgeneSubmissionTest2.csv  | sort >! la_fish_parent.tab

/^'Plate .*/	{
	split($2, arr,",");
	# rearange, trim leading/trailspaces and quotes
	for(la in arr){
		print substr(arr[la],2,8), substr($1,2,length($1)-2), substr($3,2,length($3)-2) "\t"
	}
}
