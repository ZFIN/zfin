begin work;

insert into expression_pattern_assay
    (xpatassay_name,xpatassay_comments,xpatassay_display_order,xpatassay_abbrev)
   values ('Nuclease protection assay','',10,'NPA');

insert into expression_pattern_assay
    (xpatassay_name,xpatassay_comments,xpatassay_display_order,xpatassay_abbrev)
    values ('Reverse transcription PCR','',8,'RT');

update expression_experiment
set xpatex_assay_name='Nuclease protection assay'
where xpatex_assay_name ='Nuclease S1'
or xpatex_assay_name='RNase protection' ;

update expression_experiment
set xpatex_assay_name='Reverse transcription PCR'
where xpatex_assay_name='reverse transcription PCR';


delete from expression_pattern_assay
where xpatassay_name ='Nuclease S1';
delete from expression_pattern_assay
where xpatassay_name ='RNase protection';

delete from expression_pattern_assay
where xpatassay_name ='reverse transcription PCR';

update expression_pattern_Assay
set xpatassay_abbrev='RTPCR' 
where xpatassay_abbrev='RT';

--rollback work;
commit work;
