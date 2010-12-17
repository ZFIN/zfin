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
# sort -t \| -k10,10 -k1,1n -k4,4n drerio_vega.unl | generate_id.awk > ! drerio_vega_id.unl
#
# the goal is to populate null ID  with the name_++
# and since they are not unique, append cardnality.



BEGIN {FS="|";OFS="|";N=$10;C=0;}
{
	if(N==$10){C+=1;}else{C=1;}
	if($9==""){
		$9=$10;
		if(N==$10){$9=sprintf("%s:%03i",$9,C);$12=C;}
	}
	N=$10;
	print $0;
}
END {}
