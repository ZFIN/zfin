--liquibase formatted sql
--changeset cmpich:ZFIN-9472

-- ZFIN-9472: Remove duplicate ENSDART IDs from transcripts
-- Each ENSDART should only be assigned to one transcript

delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-4130' and dblink_acc_num = 'ENSDART00000003665';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-160112-531' and dblink_acc_num = 'ENSDART00000020936';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-091110-789' and dblink_acc_num = 'ENSDART00000038301';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-21575' and dblink_acc_num = 'ENSDART00000044030';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-14137' and dblink_acc_num = 'ENSDART00000051973';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-14252' and dblink_acc_num = 'ENSDART00000056294';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-14210' and dblink_acc_num = 'ENSDART00000056441';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-1908' and dblink_acc_num = 'ENSDART00000060990';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-19690' and dblink_acc_num = 'ENSDART00000062363';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-19273' and dblink_acc_num = 'ENSDART00000076009';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-8711' and dblink_acc_num = 'ENSDART00000078723';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-7228' and dblink_acc_num = 'ENSDART00000078866';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-5817' and dblink_acc_num = 'ENSDART00000083778';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-091110-1139' and dblink_acc_num = 'ENSDART00000101036';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-19396' and dblink_acc_num = 'ENSDART00000103986';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-13149' and dblink_acc_num = 'ENSDART00000110680';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-091110-336' and dblink_acc_num = 'ENSDART00000112451';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-120217-3' and dblink_acc_num = 'ENSDART00000118179';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-14096' and dblink_acc_num = 'ENSDART00000127053';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-2634' and dblink_acc_num = 'ENSDART00000128017';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-121213-120' and dblink_acc_num = 'ENSDART00000129969';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-12920' and dblink_acc_num = 'ENSDART00000131276';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-14603' and dblink_acc_num = 'ENSDART00000131824';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-15969' and dblink_acc_num = 'ENSDART00000132824';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-21272' and dblink_acc_num = 'ENSDART00000134641';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-14883' and dblink_acc_num = 'ENSDART00000134763';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-12571' and dblink_acc_num = 'ENSDART00000135574';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-10684' and dblink_acc_num = 'ENSDART00000135956';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-091203-55' and dblink_acc_num = 'ENSDART00000135994';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-13936' and dblink_acc_num = 'ENSDART00000136257';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-19819' and dblink_acc_num = 'ENSDART00000136335';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-17654' and dblink_acc_num = 'ENSDART00000137755';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-091110-923' and dblink_acc_num = 'ENSDART00000137953';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-20097' and dblink_acc_num = 'ENSDART00000138683';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-1339' and dblink_acc_num = 'ENSDART00000138756';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-12167' and dblink_acc_num = 'ENSDART00000139896';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-13150' and dblink_acc_num = 'ENSDART00000140259';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-17184' and dblink_acc_num = 'ENSDART00000140914';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-97' and dblink_acc_num = 'ENSDART00000141448';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-12599' and dblink_acc_num = 'ENSDART00000141517';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-2962' and dblink_acc_num = 'ENSDART00000141925';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-9061' and dblink_acc_num = 'ENSDART00000142512';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-570' and dblink_acc_num = 'ENSDART00000143124';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-15385' and dblink_acc_num = 'ENSDART00000143305';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-160112-138' and dblink_acc_num = 'ENSDART00000144056';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-100915-576' and dblink_acc_num = 'ENSDART00000144221';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-5548' and dblink_acc_num = 'ENSDART00000144693';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-13262' and dblink_acc_num = 'ENSDART00000144884';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-4976' and dblink_acc_num = 'ENSDART00000146519';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-21554' and dblink_acc_num = 'ENSDART00000146739';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-091203-184' and dblink_acc_num = 'ENSDART00000147151';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-13766' and dblink_acc_num = 'ENSDART00000147381';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-1385' and dblink_acc_num = 'ENSDART00000147693';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-15503' and dblink_acc_num = 'ENSDART00000148059';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-10488' and dblink_acc_num = 'ENSDART00000148135';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-21223' and dblink_acc_num = 'ENSDART00000149792';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-1368' and dblink_acc_num = 'ENSDART00000150459';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-091110-537' and dblink_acc_num = 'ENSDART00000152062';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-3143' and dblink_acc_num = 'ENSDART00000153555';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-120213-246' and dblink_acc_num = 'ENSDART00000153596';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-1769' and dblink_acc_num = 'ENSDART00000153621';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-5823' and dblink_acc_num = 'ENSDART00000153698';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-110912-446' and dblink_acc_num = 'ENSDART00000153714';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-3155' and dblink_acc_num = 'ENSDART00000155277';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-4747' and dblink_acc_num = 'ENSDART00000155277';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-3302' and dblink_acc_num = 'ENSDART00000155530';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-2305' and dblink_acc_num = 'ENSDART00000156028';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-110912-147' and dblink_acc_num = 'ENSDART00000156950';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-16061' and dblink_acc_num = 'ENSDART00000157802';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-1765' and dblink_acc_num = 'ENSDART00000158631';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-3704' and dblink_acc_num = 'ENSDART00000160544';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-6446' and dblink_acc_num = 'ENSDART00000161483';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-3255' and dblink_acc_num = 'ENSDART00000163112';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-16682' and dblink_acc_num = 'ENSDART00000163346';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-131113-524' and dblink_acc_num = 'ENSDART00000163490';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-160623-402' and dblink_acc_num = 'ENSDART00000164327';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-3166' and dblink_acc_num = 'ENSDART00000164973';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-110325-11' and dblink_acc_num = 'ENSDART00000165997';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-2494' and dblink_acc_num = 'ENSDART00000166020';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-2496' and dblink_acc_num = 'ENSDART00000166828';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-3542' and dblink_acc_num = 'ENSDART00000168684';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-1786' and dblink_acc_num = 'ENSDART00000169750';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-53' and dblink_acc_num = 'ENSDART00000170926';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-2315' and dblink_acc_num = 'ENSDART00000171051';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-090929-16239' and dblink_acc_num = 'ENSDART00000171834';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-160623-304' and dblink_acc_num = 'ENSDART00000173644';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-160623-596' and dblink_acc_num = 'ENSDART00000173930';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-160919-160' and dblink_acc_num = 'ENSDART00000174326';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-141209-2491' and dblink_acc_num = 'ENSDART00000186681';

insert into ensembl_transcript_add
values ('ENSDART00000124538', 'ZDB-TSCRIPT-141209-3542');

insert into ensembl_transcript_add
values ('ENSDART00000135306', 'ZDB-TSCRIPT-090929-13936');

insert into ensembl_transcript_add
values ('ENSDART00000143212', 'ZDB-TSCRIPT-160112-138');

insert into ensembl_transcript_add
values ('ENSDART00000134042', 'ZDB-TSCRIPT-090929-17654');

insert into ensembl_transcript_add
values ('ENSDART00000166219', 'ZDB-TSCRIPT-141209-570');

insert into ensembl_transcript_add
values ('ENSDART00000151492', 'ZDB-TSCRIPT-120213-246');

insert into ensembl_transcript_add
values ('ENSDART00000173836', 'ZDB-TSCRIPT-160623-402');

insert into ensembl_transcript_add
values ('ENSDART00000157222', 'ZDB-TSCRIPT-131113-4747');

insert into ensembl_transcript_add
values ('ENSDART00000168739', 'ZDB-TSCRIPT-141209-1769');

insert into ensembl_transcript_add
values ('ENSDART00000193724', 'ZDB-TSCRIPT-141209-1765');

insert into ensembl_transcript_add
values ('ENSDART00000143157', 'ZDB-TSCRIPT-090929-21272');

insert into ensembl_transcript_add
values ('ENSDART00000180403', 'ZDB-TSCRIPT-141209-53');

insert into ensembl_transcript_add
values ('ENSDART00000136857', 'ZDB-TSCRIPT-090929-16682');

insert into ensembl_transcript_add
values ('ENSDART00000146222', 'ZDB-TSCRIPT-090929-5823');

insert into ensembl_transcript_add
values ('ENSDART00000192804', 'ZDB-TSCRIPT-141209-2496');

insert into ensembl_transcript_add
values ('ENSDART00000185019', 'ZDB-TSCRIPT-141209-2494');

insert into ensembl_transcript_add
values ('ENSDART00000133724', 'ZDB-TSCRIPT-090929-4976');

insert into ensembl_transcript_add
values ('ENSDART00000157033', 'ZDB-TSCRIPT-131113-3143');

insert into ensembl_transcript_add
values ('ENSDART00000131554', 'ZDB-TSCRIPT-090929-14210');

insert into ensembl_transcript_add
values ('ENSDART00000147399', 'ZDB-TSCRIPT-090929-14603');

insert into ensembl_transcript_add
values ('ENSDART00000138391', 'ZDB-TSCRIPT-090929-12167');

insert into ensembl_transcript_add
values ('ENSDART00000131629', 'ZDB-TSCRIPT-090929-15503');

insert into ensembl_transcript_add
values ('ENSDART00000146513', 'ZDB-TSCRIPT-090929-19819');

insert into ensembl_transcript_add
values ('ENSDART00000141517', 'ZDB-TSCRIPT-090929-12571');

insert into ensembl_transcript_add
values ('ENSDART00000171918', 'ZDB-TSCRIPT-090929-2315');

insert into ensembl_transcript_add
values ('ENSDART00000184324', 'ZDB-TSCRIPT-090929-5548');

insert into ensembl_transcript_add
values ('ENSDART00000160381', 'ZDB-TSCRIPT-090929-21554');

insert into ensembl_transcript_add
values ('ENSDART00000174089', 'ZDB-TSCRIPT-160919-160');

insert into ensembl_transcript_add
values ('ENSDART00000133180', 'ZDB-TSCRIPT-090929-2962');

insert into ensembl_transcript_add
values ('ENSDART00000169569', 'ZDB-TSCRIPT-090929-12599');

insert into ensembl_transcript_add
values ('ENSDART00000143028', 'ZDB-TSCRIPT-090929-15969');

insert into ensembl_transcript_add
values ('ENSDART00000144930', 'ZDB-TSCRIPT-090929-21223');

insert into ensembl_transcript_add
values ('ENSDART00000170254', 'ZDB-TSCRIPT-110912-147');

insert into ensembl_transcript_add
values ('ENSDART00000137618', 'ZDB-TSCRIPT-090929-20097');

insert into ensembl_transcript_add
values ('ENSDART00000144692', 'ZDB-TSCRIPT-091110-923');

insert into ensembl_transcript_add
values ('ENSDART00000145638', 'ZDB-TSCRIPT-091110-537');

-- Delete transcript record ZDB-TSCRIPT-240503-1485
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-240503-1485';
delete from marker_relationship where mrel_mrkr_2_zdb_id = 'ZDB-TSCRIPT-240503-1485';
delete from transcript where tscript_mrkr_zdb_id = 'ZDB-TSCRIPT-240503-1485';
delete from marker where mrkr_zdb_id = 'ZDB-TSCRIPT-240503-1485';
delete from zdb_active_data where zactvd_zdb_id = 'ZDB-TSCRIPT-240503-1485';