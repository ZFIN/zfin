#! /usr/bin/nawk -f

# expects 'zfin_DNA_clone.txt' created with 'unload_zfin_DNA_clone.sql'
# to exist sorted by accession.
# expect the agp file on the commandline to be sorted by accession.
# augments the gff3 file derived from the Ensembl AGP file with zdbids+ 


BEGIN	{
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
	OFS = "\t"
} 

{
	split($9, attrib, "=");		
	while( (z[zrow,1] < attrib[3]) && (zrow <= zmax)){ zrow++ };	
	if(attrib[3] == z[zrow,1]){
		$9 = attrib[1] "=" attrib[2] "=" z[zrow,2] ";zdb_id=" z[zrow,3] ";Alias=" z[zrow,1] "," z[zrow,3];
		print $0 
	} else { # unknown clone or contig
		print  $0
	}
}

