--liquibase formatted sql
--changeset pm:PUB-457

update journal set jrnl_name='Scanning Electron Microscopy 1978' where jrnl_zdb_id='ZDB-JRNL-050621-189';

update journal set jrnl_abbrev='Scanning Electron Microscopy 1978' where jrnl_zdb_id='ZDB-JRNL-050621-189';
update journal set jrnl_name='Scanning Electron Microscopy 1977' where jrnl_zdb_id='ZDB-JRNL-050621-188';

update journal set jrnl_abbrev='Scanning Electron Microscopy 1977' where jrnl_zdb_id='ZDB-JRNL-050621-188';

delete from journal where jrnl_zdb_id='ZDB-JRNL-130606-1';


