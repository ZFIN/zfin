#! /usr/bin/nawk -f

# goal is to print the fifth column of the last line
# of each set (contig) as determined by item in first column

# example usage:
# get_final_clone.awk drerio_vega.gff3 >! vega_chromosome.gff3
#1	vega	gene	23720	28276	.	+	.	
#  ID=OTTDARG00000029772;Name=OTTDARG00000029772;biotype=protein_coding;
# alas to avoid print an extra first row, I, sob,  hardcoded '1'

BEGIN {A=1;B=$3;C=$5; print"##gff-version 3" }
{if(NR > 2 && A == $1 &&  B != $3 && $3 == "transcript")
{printf("##sequence-region\t%s\t1\t%s\n",A,C)}A=$1;B=$3;C=$5}
#END {printf("##sequence-region\t%s\t1\t%s\n",A,C)}
