--p_locus_name_abbrev_equal.sql
-------------------------------------------------------------------
--this procedure updates the locus_registration table (specifically
--the abbrev, and locus_name values) whenever changes to these
--same values in the locus table occur.
-------------------------------------------------------------------

  drop procedure p_locus_name_abbrev_equal ;

  create procedure p_locus_name_abbrev_equal (vLocus_name  varchar(120), 
	  				      vAbbrev	   varchar(20),
		  			      vZdbId	   varchar(50))

  define vOk  integer ;

  update locus_registration
    set locus_registration.locus_name = vLocus_name
    where locus_registration.zdb_id = vZdbId ;

  update locus_registration
    set locus_registration.abbrev = vAbbrev
    where locus_registration.zdb_id = vZdbId ;

  end procedure ;
