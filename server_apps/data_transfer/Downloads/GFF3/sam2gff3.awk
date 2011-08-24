#! /usr/bin/nawk -f
# bowtie exports .sam format files
# this masages them into .gff3 format

/^ID=;Name=/ 	{strnd = "+"; if(16 == $2){strnd == "-"};
	if( "*" != $3 ){
		ID = ("MO" $3 ":" $4);
		printf( "%s\tZFIN_morpholino\tmorpholino_oligo\t%s\t%s\t.\t%s\t.\t%s%s\n",
			$3, $4, $4 + substr($6,1,2), strnd, (substr($1,1,3) ID), substr($1,4))
	} else {
		printf(">%s\n%s\n",$1,$10) > "/dev/stderr";
	}
}
