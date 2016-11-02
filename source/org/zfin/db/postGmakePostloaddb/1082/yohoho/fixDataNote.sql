--liquibase formatted sql
--changeset sierra:fixDataNote

update data_note
 set dnote_text = 'This is a target protector morphoino. There are three nts missing in the middle so it does not perfectly match prkci. Genetools verified that they did generate this MO so these nts were not left our by accident.\
Hi Holly,\
Yes, we did.\
Best Regards,\
Zhengfeng Li\
Gene Tools, LLC\
Phone:1-541-929-7840 X1011\
Fax:1-541-929-7841\
On 10/24/2014 3:09 PM, do_not_reply@support.gene-tools.com wrote:\
> =======================================================\
>    DEPARTMENT: Customer Support\
>      HOSTNAME: d57-235.uoregon.edu\
>            IP: 128.223.57.235\
>    USER AGENT: chrome 38.0.2125.104\
>       DETAILS: mozilla/5.0 (macintosh; intel mac os x 10_9_5) applewebkit/537.36 (khtml, like gecko) chrome/38.0.2125.104 safari/537.36\
>       REFERER: http://www.gene-tools.com/\
> CURRENT PAGE: http://www.gene-tools.com/live_support\
>                ( Live Support  Gene Tools, LLC )\
> =======================================================\
> E-mail::\
> holly@uoneuro.uoregon .edu\
> Message::\
> Could you tell me whether or not you made this MO?\
>\
> AAGCGACCGTCACACACTCCTCCGC\
>\
> Thanks!\
> Holly'
where dnote_data_zdb_id = 'ZDB-MRPHLNO-141027-1'
and dnote_zdb_id = 'ZDB-DNOTE-141027-1';

update data_note
 set dnote_text = 'Dear Sridhar,\
\
Thank you for noticing this omission. The morpholino sequence for cd36 used in the Benard et al study  is: cd36 Mo1: 5’CTATGAGGCCACAAATATTACCTGT3’. It is described in another recent paper by Fink et al.: http://www.ncbi.nlm.nih.gov/pubmed/25306962'
where dnote_zdb_id = 'ZDB-DNOTE-150209-1';

update data_note
 set dnote_text = 'DKEY-252I22 mostly overlaps with BX548046.4.	BX231503\
BX236571'
where dnote_zdb_id = 'ZDB-DNOTE-150528-1';

update data_note
 set dnote_text = 'targets exon 5\
Reported Sequence: GGCAGAGGTGAGATTA was reversed and complemented.'
where dnote_zdb_id = 'ZDB-DNOTE-151016-4';

update data_note
 set dnote_text = "Approximately 8 kb of the 5 prime upstream sequence of tal1\
[24,25] was polymerase chain reaction (PCR)-amplified from\
zebrafish genomic DNA. The amplified tal1 promoter, EGFP,\
and SV40 poly(A) were placed in the pT2KXIGDin vector\
that has Tol2 transposable elements [26]."
where dnote_zdb_id = "ZDB-DNOTE-160503-1";

update data_note
 set dnote_text = '2016-05-10 14:27:34|RVDs\
TALEN arm 1: NN-HD-NG-NN-NN-NG-NI-NN-NI-HD-NI-NG-HD-NI-NG-HD\
TALEN arm 2: NI-HD-NN-NG-NN-NG-NN-HD-NG-HD-NN-NI-NG-HD-NG-HD'
where dnote_zdb_id = 'ZDB-DNOTE-160510-1';

update data_note
 set dnote_text = 'Large reduction in enteric neurons. Posterior arch defects. loaded from Frodo data conversion scripts'
where dnote_zdb_id = 'ZDB-DNOTE-070117-143';

update data_note
 set dnote_text = 'loaded from Frodo data conversion scripts'
where dnote_zdb_id = 'ZDB-DNOTE-070117-153';

update data_note
 set dnote_text = 'loaded from Frodo data conversion scripts'
where dnote_zdb_id in ( 'ZDB-DNOTE-070117-229','ZDB-DNOTE-070117-298');

update data_note
 set dnote_text = 'Tail curled ventrally, chunky somites, no horizontal myoseptum.  recessive embryonic lethal loaded from Frodo data conversion scripts'
where dnote_zdb_id = 'ZDB-DNOTE-070117-301';

update data_note
 set dnote_text = 'Hi Ken, Sridhar<BR><BR>I have extended this gene locus using new evidence to give a full length protein product.&nbsp;&nbsp;Species \
 specific EST and cross-species protein support is very good.<BR><BR>Thanks, Jeff<BR><BR>>16.38264663-38442933 DNA CH211-135D14.2-001 9122 \
 bp<BR>ggcggcatgtggatgaggaggttgtatatttcagctcttctgttgttttt<BR>ggcgaagaaaacacacattctcaaaacgaacccgtaaaaaataagaaaaa<BR\
 >aagccccacacaaaatcatttacccgggatcgagcccatgacgtccattc<BR>acttcgtggttcacccgttacccgggaccgaagaccagcttaatgacaga<BR\
 >ctccgagaagtgtcggaaaagctcaacaaatacaactgtaatagtcatcc<BR>acaccttattccattggagcaggccaaacttaagcagtgtgtggttggac<BR\
 >caaatcatgctggatttctccttgaggatggacgcatctgcaggataagc<BR>tttgccgttcagcctgaccgtctggagcttggcaagccggatggcaatga<BR\
 >tggttcaaagttgagcagtgtctcaggggcaggaaggagctccaggccag<BR>gcaggactagtgatccgccctggttcctgtctggttctgacacactgggc<BR\
 >agactggcaggcaacacccttgggacccgctggagttcaggagtaaatgg<BR>cggaggcagtggaggagggggcagcagcggaggaggcgcaggaggaggca<BR\
 >gcgggagcggaggaggcgctggaggaggcacaggaggaggaggatcttca<BR>ggacgttcatccacagcagcccgtgattcacgcaggcagacccgcgtcat<BR\
 >ccgcaccggcagagatcgaggctctggcctgctgggaagccaaccacagc<BR>ctgtgattccagcctccgtcatccccgaggagctcatctctcaggctcag<BR>gtgcggaggaggcgcaggaggaggca<BR\
 >gcgggagcggaggaggcgctggaggaggcacaggaggaggaggatcttca<BR>ggacgttcatccacagcagcccgtgattcacgcaggcagacccgcgtcat<BR\
 >ccgcaccgggttcttcaggggaaatccagaagtgtcatcattcgagagctgcagcg<BR>cacaaaccttgacgtgaacttggctgtaaacaacctgctgagcagagatg<BR\
 >atgaggatggagatgatggcgatgacacggccagcgaatcctacttacct<BR>ggagaagacttgatgtcgctgctagatgcagacattcactccgctcaccc<BR\
 >cagcgtgatcatagatgccgatgccatgttctctgaggacatcagctact<BR>tcggttacccctcttttcgtcgctcctccttgtctcgtcttggctcctca<BR\
 >cgagttctccttctccccctagagagagactctgagctgctccgcgagcg<BR>tgagtcggtcctgaggctgcgggagaggcggtggctggacggggcctcct<BR\
 >tcgatgcggagcgtggctccaccagccgcgagggagagcccagccttgac<BR>aagaagagtgtcccgctgcagagcccagtcactctgggggaagagctcca<BR\
 >gtggtggcctgataaggactatgtaacaaagtttgtcagcattggtgctt<BR>tgtactcggagctggttgccgtcagtaccaaaggagaactctaccagtgg<BR\
 >aagtggaatgaacctgaaccttacagaaacgcacaaaatccttctataca<BR>tcaccctcgtgttcctttcctgggcttgaccaatgaaaagatcacccacc<BR\
 >tgtccgccaacagtatccgagccactgtggccaccgagaacaacaaggtg<BR>gcaacatgggtggatgaaactctgagcactgttgctgctaaactcgaaca<BR\
 >cggagcacaaacgttcccggagctgcagggggagcggatcgtctctctgc<BR>actgctgtgccctgtatacctgcgctcagctggagaacagcctttactgg<BR\
 >tggggtgttgtgccttttagtcaacggaaaaaaatgcttgaaaaggccag<BR>agccaagaacaagaaaccaaagtccagtgctggaatctcttcgataccca<BR\
 >acatcacagtaggaactcaggtgtgcctgaggaataatcctctttatcac<BR>gctggtgccgtggccttttccgtgaacgccgggatccctaaagtgggcgt<BR\
 >cctgctggagtcggtgtggaacatgaacgacagctgccgcttccagctgc<BR>gctcgccagagagcctgaaaaacatggagaaaaacaccaagacacaggag<BR\
 >accaagacggagagcaagccagagctggtgaagacggaaatgggccctcc<BR>gccctcaccagcgtccacatgcagtgacacttcatccatcgctagcagcg<BR\
 >cttcactgccgtacaagcgaaggcgctcaacaccggctccgaaggaggag<BR>gagaaggtgaatgaggagcagtggcctctgcgagaggttgttttcgtgga<BR\
 >ggatgttaaaaacgtccccgtgggaaaggtgctaaaagtcgatggtgcat<BR>atgttgctgtgaaatttccagggacgtcgagcagcgtgagcagccagagc<BR\
 >gcagctcccactgactctgacccgtcctcactgctgcaagactgtcgact<BR>gctcagaatagatgagctacaggttgttaaaactggtggaacaccaaaag<BR\
 >ttcctgactgttttcagcgaacacctaaaaaactttgcatcccagaaaaa<BR>gctgaaattttagcagtgaatgttgactccaaaggagttcatgctgtgtt<BR\
 >gaaaacgggcagctgggtgagatactgtgtctttgacttagccaccggca<BR>aagccgagcaagagaatcactttcccaccagtaacctggcgtttcttggt<BR\
 >cagagcgagcgaaatgtggccatcttcactgctggccaggagagtcccat<BR>tattttgagggatggcaatggcactatttatcccatggctaaagactgta<BR\
 >tgggtggaatacgtgaccctgattggctggatctgccgcctatagccagt<BR>ctgggcatgggagttcactcgctcgccaaccttcccagtaactccacaat<BR\
 >caaaaagaaggcggccatcatcataatggctgtagagaagcagactctga<BR>tgcagcacgtgctgcgctgtgattacgaggcgtgtaggcagtatctgatc<BR\
 >agtctggagcaggccatgctactggagcagaaccctcacgctctggacac<BR>tctgctaggacaccgctgtgacggaaaccgcaacgtcctccatgcctgtg<BR\
 >tgtccgtctgcttccccgtcagcaacaaggaaaccaaggaggaagaagaa<BR>gcagagcggtctgaaagaaacacgttcgctgagaggctttctgcagtgga<BR\
 >ggccattgccaatgccatatcagtggtgtcgagcaacagctccgggaacc<BR>ggacaggatcatccagcagcaggggcttgcgtttgagagagatgatgcgg<BR\
 >cgctcactccgtgctgcaggtttaggccgacatgaatccggaccttcctc<BR>cagcgaccatcaagaccccgtttcacccccgatagcacctcctagctggg<BR\
 >taccagaccctccaccgatggatcctgacggtgacattgatttcatcttg<BR>gcccctgcagtgggctccctcaccactgcttccacaggcaccagccaggg<BR\
 >acccagcacttccaccatcccaggaccctcctctgaaccatcagtagtgg<BR>aatctaaagaccgtaaggcgaacgcacacctgatactgaagctgatgtgt<BR\
 >gacagcatcgtcctgcggcctcatctaagagaactgctctctgccaagga<BR>tgctcgtggaatgactccattcatgttagcagtgagtggcagagcttacc<BR\
 >cagcagccattactgtcttagaagcagcacagaaaattgccaaaggtgaa<BR>ccaggtcttggagagaaagaggatacagcatctgtgttcatggagatgat<BR\
 >ctgtccttcaggaaccaatccagacgactctccgctgtacgtcctgtgct<BR>gtaacgacacctgcagcttcacctggactggagcagaacacatcaaccag<BR\
 >gacatctttgagtgcagaacttgtggcctgctggagtcgctgtgctgctg<BR>cactgaatgtgctagagtctgtcacaaggggcacgactgcaaactgaaga<BR\
 >ggacgtcaccaacagcttactgtgactgctgggagaagtgcaaatgcaag<BR>actctcatcgcaggccagaaagcggcacgtctggatctgctctacaggct<BR\
 >gctgaccaccactaatctagtgaccactcctaacagcaggggagagcaca<BR>tcttgctgtttctggtgcagactgttgcaaggcagagtgtcgaacactgt<BR\
 >cagtatcgacctcctcgcattcgagaggacaggaaccggaaagctgccag<BR>tgctgaagactctgatatgccagatcatgacctggaacctccacgctttg<BR\
 >ctcagctggctctggagcgagtgttgcaggactggaacgcactgaagtca<BR>atgataatgttcggctctcaggaaaataaagatccgttaagcgctagcag<BR\
 >cagaatcgctcaccttttaccggaggagcagatgtacctcaaccagcaga<BR>gtggcaccatcagactggactgtttcacacactgccttattgtcaagtgt<BR\
 >gcaccagacatcactttcattgacacactgttagggaccctggtgaagga<BR>gctgcagaataaatacacccccggccggcgggaagaggccatcaatgtca<BR\
 >cacggaggttcttgcgctctgtggccagagtgtttgtcatcctcagcgtg<BR>gagatggcatcttccaagaaaaagaacaacttcatccctcagcctattgg<BR\
 >gaagtgcaggcgtgtgttccaggcactgttgccgtacgcagtggaggagc<BR>tctgcaatgtggccgagtctctgatcgtgccggtgcgtatgggcatcgct<BR\
 >cggcccacagctcccttcactctggccagtaccagcattgatgcagtgca<BR>gggcagtgaggagctcttctctgtggagcctctgccacctcgaccctccc<BR\
 >cggaccagtccagcagctccagtcagtctgcctcttcatacatcatcagg<BR>aacccgcagccccgccgcagcagccagtctcagacagcccgcggacggga<BR\
 >cgaggagcaggatgatatcgtgtctgctgacgtagaagaggtggaggtgg<BR>tggagggtgttgcaggtgaggaggatcaccatgatgaccaggaggagcag<BR\
 >ggagaggagaacgcagaggctgaaggacagcatgatgagcacgatgagga<BR>tgggagcgacatggagctggatttattggctgccgctgagaccgagagtg<BR\
 >acagtgagagtaaccatagcaaccaggacaatgcgagtggccgcaggagt<BR>gttgtgacggcagccactgctggatctgaagcaggtgccagcagtgttcc<BR\
 >tgctttcttctccgaggacgactcccagtcgaatgactccagcgactcgg<BR>acagcagcagcagccagagtgacgatgtggatcaggagacgtttctgctg<BR\
 >gacgagcctcttgagagaaccaccggctcagcacatgctaacagtgcagc<BR>ccaggccccacgctccatgcagtgggctgttcgcactacacccagccagc<BR\
 >gctctggaggtggcgccccatccagctcgtccgctcctgctgcgagctcc<BR>actggcctcatctacatcgacccgtcaaacctgcgccgcagcagcgccat<BR\
 >cagcaccagcgcagccgctgccgcagcagccctggaggccagtaactcca<BR>gcagttatctgacgtctgccagcagcctggctcgagcatacagcatcgtc<BR\
 >atccgccagatctctgacctcatgagtctcattcccaaatacaaccacct<BR>ggtctactcgcagtaccccgctgctgtcaaactcacctatcaggatgctg<BR\
 >tcaatctgcagaactttgtggaggacaagttgattcccacctggaactgg<BR>atggtgtccatcatggactccactgaagcgcagctgcgttacggctcggc<BR\
 >tctgtcttcagccggggatcctggtcatcccagccacccgctacacgcat<BR>cccagcatgccggccgcagagagcgcatgaccgctcgagaggaggccagt<BR\
 >ctgcgcactctagaaggacgaaggcgtgctgctacgcttctgacggcacg<BR>tcagggcatgatgtcagcgcgaggagatttcctgaactacgctctgtctc<BR\
 >tgatgcgttcccataatgacgagcattcagacgttctgcctgtgttagat<BR>gtctgttcgctcaaacacgtggcttacgtcttccaggcgctcatctactg<BR\
 >gattaaagccatgaatctgcagaccacgctcgacaccactcagattgaca<BR>ggaagaggaatcgtgagctgctggagctcggtctggacaatgaagactct<BR\
 >gagcacgagaacgatgaggacaccaatcagagctctgcgttcccataatgacgagcattcagacgttctgcctgtgttagat<BR>gtctgttcgcttacactgcaggataa<BR\
 >ggacgaggagcctgtgcctgcagagactggacaacaccatccgtttttcc<BR>gccgctcggactccatgaccttcctggggtgcatccctccaaaccctttt<BR\
 >gaagttcctctagcagaggccattcctctggctgaccagccgcatctcct<BR>gcagcccaatgccagaaaggaggatttgtttggacggcctagtcaaggtc<BR\
 >tatattcgtcctcatatatggccagtaaaggcctcaccgatctgactgtg<BR>gacatgaattgcttgcagattctacccacaaagatgtcgtattcggcgaa<BR\
 >catgaagaacgtgatgagtatggagtcccggcagaggggtggcgaggagc<BR>agccggtggcggagcaggagatggatgtgtctaaacccggcccttcacca<BR\
 >cacgatctggcggcccagttaaagagcagcctgctggcagagattggcct<BR>gactgagagtgatggcccaccgctgcccacgtttattccacactgtagtt<BR\
 >ttatggggatggtgatctctcatgacatgctgctgggccgctggcggctt<BR>tctctggagctgtttggccgtgttttcatggaggacgttggagcagaacc<BR\
 >aggatctatcctgacagagctggggggttttgaggtaaaggagtccaagt<BR>ttcgtcgtgagatggagaagctgcgtaaccttcagtcgcgtgatctggcc<BR\
 >ctggaggtggaccgagaccgtgagcagctcatccagcagaccatgcggca<BR>gctgaacgctcacttcggccgccgctgtaccaccacacccatggccgtgc<BR\
 >accgcgtaaaggtgacattcaaggatgagcctggagagggcagtggtgtc<BR>gctcgcagcttctacaccgccatcgctcaggccttcttgtccaacgacaa<BR\
 >gctgcccaatctggactgcgtgcagagcgtcagcaaggggatgcaggcta<BR>gcaatcttatgcagcggctgaggaaccgagatcgtgagcgtgagaggagg<BR\
 >agcggaggactgagagctgcatccagacgagatcgtgacagggactcgcg<BR>gaggcagttgtccatcgacacccggccgttcagacctgcatcagaaggaa<BR\
 >accccagtgatgaaccagaaccgctgccggctcacagacaggctctggga<BR>gagagactttacccacgagttcatgccatgcaaccggcgtttgctagtaa<BR\
 >aatcacagggatgctgctggaactgtctcccgctcagctgctgctactcc<BR>tggccagcgaagactcgctcagagcccgagtggaggaggccatggagctg<BR\
 >ctcataactcatggacgggaaaatggtgctgacagtatactagacctcgg<BR>cctcctcgacactcctgagaaagcacaacaggagaaccgaaagaggcatg<BR\
 >gttcgactcgcagtgtggtggacatggagctggatgaccctgatgatggg<BR>gatgataacgctcctctgttttaccagcctggaaagaggggtttctactc<BR\
 >accacgtcccggcaagaacactgaggccaggctcaactgtttccgtaaca<BR>tcggcaggatactgggtttgtgtctgctacagaatgagctttgcccaatc<BR\
 >actctcaacagacacgtcatcaaggtgctgctcggcagaaaggtgaactg<BR>gcacgactttgcgttcttcgacccggtaatgtacgagagtttgcggcagc<BR\
 >tcatccgtcactctcagactgaagaggcggaggctgtgtttgcggcgatg<BR>gacctggccttcgctatcgacctctgcaaggaggagggagcaggacaggt<BR\
 >ggagctgctgtccggtggagtcaacatgccggtgacgcctctgaatgtgt<BR>acgagtacgtgaggagatacgcagagcaccggatgctggtggtggcagaa<BR\
 >cagcctctgcatgccatgcggaaaggtcttctggatgtgcttcctaagaa<BR>cgctctggaagatctgacagcggaggatttcagactgctggtcaacggct<BR\
 >gtggagaagtcaacgtccagatgctcattagtttcacttcattcaatgat<BR>gagtcaggtgagaatgcagagaagctgctgcagttcaagcgctggttttg<BR\
 >gtccatcgttgagaagatgagcatgacggaaaggcaggacctggtgtact<BR>tctggacgtccagcccgtctctgccggccagtgaagaaggtttccagccg<BR\
 >atgccctccatcaccatccgcccgccggacgaccagcacctgcccacagc<BR>caacacctgcatctctcgcctctacgtgccactttactcttccaaacaga<BR\
 >tcctcaaacagaaacttttactagccatcaagaccaagaacttcggtttt<BR>gtgtagatgaacgaaacttgtttaatcgttgtgtaatattactagttcca<BR\
 >tttttgtagatttatttttctttgtctatacaattttatgaaattgagaa<BR>agtgctgtcgcccccctggcagtcttatctttttggggtctgcgttgcaa<BR\
 >acgtggacggcttgttcttattttttgtttcttttttttattattattat<BR>tattattatttccggttatcggagtataacgcaacattcctgcattggct<BR\
 >tttgtaaagttgaagcctgcagctccattggcttgaactgtaacttcccg<BR>tgactttggcctgacggtaaactgtcccgccaaacccagccaaagacgac<BR\
 >agcaggagtatttaaagaaaaagaaggttacactgtgatttatgattttt<BR>tttttcctgccctaaacataaatattactatcactgagagttcactgctg<BR\
 >cctttgtggaaagtcattttttcttgtatagagggagtagggaatttcaa<BR>aaataaacttggatgcaaacta<BR>'
where dnote_zdb_id = 'ZDB-DNOTE-080226-3';


update data_note
 set dnote_text = '>3.3271974-3556868 DNA CH211-250L3.1-001 1928 \
 bp<BR>gccttgctgtgatgccgtcggatccgagcagctggtacgcgtgagctgct<BR>ttttgggctcgtgtggtcaccccgtttctttgctggaggaagagggagga<BR\
 >gggcgcatcaggtttgtgtccgttctcgcctgcatctggaccccggcgcc<BR>agggagcgtggggggcattagggccaggcggccagtcggggggacgacat<BR\
 >gagcgttccgctcctgaagattggcgcggtgctcagcaccatggccatgg<BR>tcaccaactggatgtcccagaccttgccctctctagtggggcttaacgga<BR\
 >accaccatctctcgtgcaggcacctcggaaaggattgtcagtgccctgta<BR>tccaagcccggaggagggctggcagatctacagctcggcacaggatgcag<BR\
 >atgggaaatgtatctgcaccgttgttgctccagcacagaacatgtgtaat<BR>cgagacccacgcagcagacagctccggcaacttatggagaaggttcagaa<BR\
 >tatcactcaatccatggaggtgctagacctgaggacgtacagagacctgc<BR>agtatgtgagagatacggagaacctcatgaaaacagtggacggcaaactg<BR\
 >aagactgcctctgaaaatcctcgcagcctaaaccccaagagttttcagga<BR>gttaaaggacaaggttactcagctgctccccctgttgccagtgctggagc<BR\
 >agtacaaggcagatgcaagaatgattctgcggctgagggaggaagtgaga<BR>aatctgtctctggtacttatggccattcaagaagagatgggagcctatga<BR\
 >ttacgaggagctcaggcagcgagtgctattactagagactcgcctgcact<BR>cctgcatgcagaaactcggttgcggaaagctaactggcgtcagtaacccc<BR\
 >atcaccgtccgtgcctcagggtcaaggttcggttcctggatgactgacac<BR>aatgatccccagctctgataatagggtgtggtcaatggatggttacttta<BR>agggacgtcgagatgggagcctatga<BR\
 >ttacgaggagctcaggcagcgagtgctattactagagactcgcctgcact<BR>cctgcatgcagaaactcggttgcggaaagctaactggcgtcagtaacccc<BR\
 >atcaccgtccgtgcctgtgctggagtaccgcaccatgaatgacttcatgaagggc<BR>cagaactttgtccagcatctcctgccccacccttgggctggtacgggtca<BR\
 >cgtggtctacaacggctcactatactacaacaaataccagagcaacatcc<BR>tcatcaagtaccacttccggtctcgaagtgtcctggtgcagcggagcctt<BR\
 >agcggtgccggctacaacaacaccttcccctactcctggggcggatcatc<BR>cgacatcgacctcatggctgatgagaatggtctgtgggctgtctacacca<BR\
 >ccattcccaatgccggaaacatcgtcattagccgtctagagccgcaaagc<BR>ctggaagtgcttcaaacgtgggacacaggctttcccaaacggagcgcagg<BR\
 >cgagtccttcatgatctgcggcactctttatgtaaccaactcccacttgg<BR>ccggtgctaaaatctacttcgcatattacaccaacacctcgacctatgag<BR\
 >tacactgacatccccttccataaccaatactcccacatctccatgatgga<BR>ctacaatccccgagaaagagttctctacacctggaacaatggacatcaag<BR\
 >tcctctacaacgtcacgttgttccaggtcatcaaaaccgctgaggactaa<BR>gtcattcagactttacaaacacatttgctcatacacaaatgcacacacac<BR\
 >tattataacacacacatacacacacttatgtggaggggggactaaagttg<BR>aaacttttttactgcattccaaacttcatagagtgctattaattatctaa'
where dnote_zdb_id = 'ZDB-DNOTE-080311-3';

update data_note
 set dnote_text = '>DrJAK3_Ensembl_ENSDART00000049469<BR>LMKSERAGSQRSSCDSALQVHLYYSPSLNSETTFSIPTGHVTAESVCVLAAKASGILPVYHNLFALASEDLS<BR\
 >YWYPPNHLFKSEEPVKVYYRVRFFFSSWFGQESRASYRFSLSKGRIFAVLDYAVIDYVFAQSRSDFVTGCGG<BR>ISPALSLQQECLGLAVLDLWRLAKERNQSLAEICNTTSYKSCLPETHRQDIQRMNRLARYQIRKTLKRFLKK\
 <BR>LGKCSAGERSLKLKYLMKLSELEPDYGSESFPLHHSGWLEQSEQQRVLAVKVSGEGGIQIQKTDRQEWQTFC<BR\
 >DFPQIIDISIKRLCQEQMPLEGRVVTLTRQDDQCMEEAEFQTLTDALSFVSLVDGYFRLTTDSTHYFCAEVA<BR>PPSLLEDIQNYCHGPITSEFAVHKLKKAGGKNGMFLLRHSPKEFDKYFLTVCIQTPLGMDYKDCLIEKNEKF\
 <BR>SLAGIHNSFINLKQLIDFYQLSTLYVSDIPVTLGKCCPPRAKELTNMIIIRNSSMTEIPSSPTLQRHKPSHM<BR\
 >QFHMIKHEDLIWSESLGQGSFTHIFRGSKIDQRDGGTHSTEVLLKVLDANHKNCWESLFEAASLMSQISHRH<BR>LLLVYGISVHKSKNIMVQEFVKHGALDLYLKRSMCVSVSWKLDVAKQLACALNFLEEKNIAHGNICAKNLLL\
 <BR>VREGDSPFIKLSDPGVSMSLLGKDVVLDRIPWVAPEVLDTLEIELECDKWSFGTTLWEIFNGGEAPLQGLDL<BR\
 >MQKLQFYENFSNLPTLEWTELAELISHCMQYQPELRPSCRSIIRQLNSLITSVDYEILHATDTLPESNGFWK<BR>KLNIFKKQQEDVFEERYLRFISVLGKGNFGSVELCRYDPWGDNTGELVAVKELQSNKQATMADFQREIQTIS\
 <BR>SLHCDYIVKYKGICYSTGRLSTKLVMEYLPYGSLIGYMEKHRHNVGNRKLLLFASQICKGMEYLQSMRYVHR<BR\
 >DLAARNILVASDNLVKIADFGLTKIIPVDKEYYRVTQPGESPVFWYAPESISELKFSHKSDVWSFGIVLHEL<BR\
 >FSYCDISQNPKKLCIQKIGRYVHSPSMAIHLLTLLKNNWRLPAPAQCPLKVHSIMMQCYDPWGDNTGELVAVKELQSNKQATMADFQREIQTIS'
where dnote_zdb_id = 'ZDB-DNOTE-080320-1';

update data_note
 set dnote_text = 'Reported Sequence: CCAAAACCTCACAGAAGCTCGTCAT was reversed and complemented. Author verified this sequence. It targets inside an \
 intron of tjap1 upstream of dlk2.'
where dnote_zdb_id = 'ZDB-DNOTE-140123-1';

