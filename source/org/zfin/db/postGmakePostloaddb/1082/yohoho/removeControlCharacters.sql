--liquibase formatted sql
--changeset sierra:removeControlCharacters

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/barx1/index.html">unrec_barx1</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-1';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/inka1a/index.html">fh326</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-11';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/inka1a/index.html">fh327</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-12';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/mlc1/index.html">fh328</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-13';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/cx35/index.html">fh329</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-14';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/barx1/index.html">fh330</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-15';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/barx1/index.html">fh331</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-16';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/notch3/index.html">fh332</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-17';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/notch3/index.html">fh334</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-19';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/col4a1/index.html">fh336</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-21';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/col4a3/index.html">fh337</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-22';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/flt1/index.html">fh338</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-23';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/col4a1/index.html">unrec_col4a1</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-3';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/col4a3/index.html">unrec_col4a3</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-4';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/cx35/index.html">unrec_cx35</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-5';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/flt1/index.html">unrec_flt1</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-6';

update external_note
  set extnote_note = 'For more information, see the Moens lab TILLING project for <a href="http://labs.fhcrc.org/moens/Tilling_Mutants/mlc1/index.html">unrec_mlc1</a>'
 where extnote_data_zdb_id = 'ZDB-GENO-101008-8';
