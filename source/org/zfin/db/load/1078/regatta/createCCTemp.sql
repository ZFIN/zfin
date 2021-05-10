--liquibase-formatted sql


create table cc_temp (ccid varchar(50),
       	     	      cctype varchar(50),
		      cccategory varchar(50),
		      compid varchar(50),
		      cccomp varchar(50),
		      cccasset int,
		      ccorder int) 
in tbldbs1
extent size 16 next size 16;
			   
