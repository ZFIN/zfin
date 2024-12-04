--liquibase formatted sql
--changeset cmpich:ZFIN-9448.sql

update external_note
set extnote_note = 'SJD descended from a full sibship (clutch) of <a href="http://zfin.org/ZDB-GENO-960809-13">Darjeeling</a> Wild Types that the Johnson lab genotyped. ' ||
                   'The analysis revealed that approximately 14 percent of the genome still had polymorphisms within the stock. ' ||
                   'Since then, the Johnson lab has been breeding these fish to retain that diversity and vigor.  ' ||
                   'The line was published in  <a href="https://zfin.org/ZDB-PUB-030408-14">ZFIN Publication: Rawls et al., 2003</a> <BR><BR>SJD-subB: ' ||
                   'The Johnson lab also developed a subline of SJD that has been further inbred by full sib matings, called SJD-subB. ' ||
                   'They have not been analyzed to see if they are in fact more inbred, but they likely are. ' ||
                   'The Johnson lab assumes that only 5 percent of the genome in these fish still has polymorphisms. ZIRC maintains and distributes their descendants. ' ||
                   '<BR><BR>Both SJD and SJD-subB are still quite hardy, and shock breed naturally.'
where extnote_zdb_id = 'ZDB-EXTNOTE-130618-2'
  and extnote_data_zdb_id = 'ZDB-GENO-990308-9';
