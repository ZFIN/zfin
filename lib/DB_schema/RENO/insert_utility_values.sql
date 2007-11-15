
begin work;

insert into run_type(runtype_name) values ('Redundancy');
insert into run_type(runtype_name) values ('Nomenclature');


insert into run_program (
    runprog_program,runprog_target_type,runprog_query_type
)  values('BLASTN',  "n", "n")
;
insert into run_program (
    runprog_program,runprog_target_type,runprog_query_type
)  values('TBLASTX', "n", "n")
;
insert into run_program (
    runprog_program,runprog_target_type,runprog_query_type
)  values('BLASTX',  "n", "p")
;
insert into run_program (
    runprog_program,runprog_target_type,runprog_query_type
)  values('TBLASTN', "p", "n")
;
insert into run_program (
    runprog_program,runprog_target_type,runprog_query_type
)  values('BLASTP',  "p", "p")
;

insert into accrel_type (accrelt_type) values ('ENTREZ Gene'); -- not quite sure what this should be

--
rollback work;

-- commit work;