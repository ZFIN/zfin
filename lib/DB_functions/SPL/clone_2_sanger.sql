-- convert  between 
--   Sanger external clone names 
-- and 
--   Sanger internal clone names

{
Date: Wed, 21 Apr 2004 17:23:13 +0100
From: Kerstin Jekosch <kj2@sanger.ac.uk>
To: Judy Sprague , Monte Westerfield,
     Tom Conlin
Cc: Mario Jose Caccamo <mc2@sanger.ac.uk>
Subject: clone libraries

Hi,

at the moment we have the following libraries:

library                 internal identifier     external identifier
CHORI 211 (BAC)         zC                      CH211-
DanioKey (BAC)          zK                      DKEY-
DanioKeyPilot (BAC)     zKp                     DKEYP-
RPCI-71 (BAC)           bZ                      RP71-   
BUSM1 (PAC)             dZ                      BUSM1-

(see http://www.sanger.ac.uk/Projects/D_rerio/mapping.shtml)

Some clones were sent for sequencing withoutus being told what library 
they came from (either some people wanted to make really sure that no 
one could be quicker than them or they simply didn't know), they will 
then have the external prefix XX- and a mixture of internal prefixes.
}

drop function clone_2_sanger;

create function clone_2_sanger(clone like marker.mrkr_abbrev) returning varchar(40);
    -- return value should be same datatype as mrkr_abbrev.

    define sanger like marker.mrkr_abbrev;
    let clone = lower(clone);
    if   clone[1,6] = 'dkeyp-' then let sanger = 'zKp' || clone[7,20];
    elif clone[1,6] = 'ch211-' then let sanger = 'zC'  || clone[7,20];
    elif clone[1,6] = 'busm1-' then let sanger = 'dZ'  || clone[7,20];
    elif clone[1,5] = 'dkey-'  then let sanger = 'zK'  || clone[6,20];
    elif clone[1,5] = 'rp71-'  then let sanger = 'bZ'  || clone[6,20];
    else let sanger = clone;
    end if;
    return sanger;
end function;

update statistics for function clone_2_sanger;
-------------------------------------------------------------------------------
{
  This routine was never used.  We don't know if it works and we don't know
  if it will ever be useful.  Therefore we are comenting it out.
  I sent this to Tom for code re

drop function sanger_2_clone;

create function sanger_2_clone(sanger like marker.mrkr_abbrev) returning varchar(40);
    -- return value should be same datatype as mrkr_abbrev.

        define clone like marker.mrkr_abbrev; 
	let sanger = upper(sanger);
	if   sanger[1,3] = 'ZKP' then let clone = 'DKEYP-' || sanger[4,20];
	elif sanger[1,2] = 'ZC'  then let clone = 'CH211-' || sanger[3,20];
	elif sanger[1,2] = 'DZ'  then let clone = 'BUSM1-' || sanger[3,20];
	elif sanger[1,2] = 'ZK'  then let clone = 'DKEY-'  || sanger[3,20];
	elif sanger[1,2] = 'BZ'  then let clone = 'RP71-'  || sanger[3,20];
	else  let sanger = 'XX-' || clone;
	end if;
	return clone;
end function;

update statistics for function sanger_2_clone;
}