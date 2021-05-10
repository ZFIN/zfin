select mrkr_abbrev, mrkr_zdb_id
 from marker
 where mrkr_type = 'LINCRNAG'
and mrkr_abbrev not like 'linc%'
union
select mrkr_abbrev, mrkr_zdb_id
 from marker
 where mrkr_type = 'MIRNAG'
 and mrkr_abbrev not like 'mi%'
union
select mrkr_abbrev, mrkr_zdb_id
 from marker
 where mrkr_type = 'TRNAG'
and mrkr_abbrev not like 'tr%'
union
select mrkr_abbrev, mrkr_zdb_id
 from marker
 where mrkr_type = 'LNCRNAG'
and mrkr_abbrev not like 'ln%'
union
select mrkr_abbrev, mrkr_zdb_id
 from marker
 where mrkr_type like 'NCRNAG'
and mrkr_abbrev not like 'nc%'
union
select mrkr_abbrev, mrkr_zdb_id
 from marker
where mrkr_type like 'PIRNAG'
and mrkr_abbrev not like 'pi%'
union
select mrkr_abbrev, mrkr_zdb_id
 from marker
 where mrkr_type like 'RRNAG'
and mrkr_abbrev not like 'rr%'
union
select mrkr_abbrev, mrkr_zdb_id
 from marker
 where mrkr_type = 'SCRNAG'
and mrkr_abbrev not like 'sc%'
union
select mrkr_abbrev, mrkr_Zdb_id
 from marker 
where mrkr_Type = 'SNORNAG'
and mrkr_abbrev not like 'sno%'
union 
select mrkr_abbrev, mrkr_zdb_id
 from marker
 where mrkr_type = 'SRPRNAG'
and mrkr_abbrev not like 'srp%'
;
