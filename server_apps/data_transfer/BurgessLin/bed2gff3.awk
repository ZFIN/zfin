#! /usr/bin/nawk -f
# bed2gff3.awk

### BED format
#    chrom      - The name of the chromosome (e.g. chr3, chrY, chr2_random) or scaffold (e.g. scaffold10671).
#    chromStart - The starting position of the feature in the chromosome or scaffold. The first base in a chromosome is numbered 0.
#    chromEnd   - The ending position of the feature in the chromosome or scaffold. The chromEnd base is not included in the display of the feature. For example, the first 100 bases of a chromosome are defined as chromStart=0, chromEnd=100, and span the bases numbered 0-99.
#
#	additional optional BED fields are:
#
#    name   - Defines the name of the BED line. This label is displayed to the left of the BED line in the Genome Browser window when the track is open to full display mode or directly to the left of the item in pack mode.
#    score  - A score between 0 and 1000. If the track line useScore attribute is set to 1 for this annotation data set, the score value will determine the level of gray in which this feature is displayed (higher numbers = darker gray). This table shows the Genome Browser's translation of BED score values into shades of gray:
#    shade  - score in range   0-1000
#   strand - Defines the strand - either '+' or '-'.

### o	rearrange & drop unused columns from bed file
### o	strip the 'chr' 
### o	change from 0 based start coordinates to '1s' based
### o	add string constants

/^chr/ {
	LG = substr($1,4);
	strand=substr($6,1,1); # (windows?) line endings cause trouble otherwise
	printf("%s|BurgessLin|Transgenic_insertion|%i|%i|.|%s|.|%s||||\n",LG,1+$2,$3,strand,$4);
}
