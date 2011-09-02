#! /usr/bin/nawk -f
#  backbone.awk  drerio_vega.unl >! vega_backbone.unl
# 1    seqname             varchar(9),
# 2    source              varchar(25),
# 3    feature             varchar(25),
# 4    start               integer,
# 5    end                 integer,
# 6    score               varchar(5),
# 7    strand              char(1),
# 8    frame               char(1),
########################################
# 9    ID                  varchar(25),
#10    Name                varchar(44),
#11    Parent              varchar(25),
#12    biotype             varchar(55)
#
# the goal is to populate "ID="  with the baseid-00n
# here the base ID is MO <LG> : <start_loaction>
# and since they may not be unique, append cardinality
# expects the file to be sorted by LG location
# so that duplicates are adjecent
# also inserts a gff3 header
BEGIN {FS="\t";OFS="\t";split($9,ATT,";");
   ID=ATT[1];C=1; print "##gff-version 3\n";
}
{
	split($9,ATT,";");
	if(ID == ATT[1]){
		C+=1;
		$9 = sprintf("%s-%03i;%s;%s;",ATT[1],C,ATT[2],ATT[3] );
	}else{C=1;}
	ID=ATT[1];
	print $0;
}
END {}

