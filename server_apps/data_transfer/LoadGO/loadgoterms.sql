begin work;
create temp table exist_record (
                extrecd_zdb_id  varchar(50),
                new_zdb_id      varchar(50)
                );
	create temp table goterm_onto_with_dups (
		goterm_id	varchar(10),
		goterm_name	varchar(255),
		goterm_onto	varchar(30)
	)with no log;

	load from ontology.unl insert into goterm_onto_with_dups;

	create temp table goterm_onto (		
		goterm_id	varchar(10),
		goterm_name	varchar(255),
		goterm_onto	varchar(30)
	)with no log;
	create index goterm_onto_index on goterm_onto (goterm_id);
	create index goterm_name_index on goterm_onto (goterm_name,goterm_onto);
	insert into goterm_onto
		select distinct * from goterm_onto_with_dups;
unload to 'gotermonto.unl' select * from goterm_onto;

	create temp table new_goterm (		
                goterm_zdb_id   varchar(50),
		goterm_id	varchar(10),
		goterm_name	varchar(255),
		goterm_onto	varchar(30),
                goterm_is_obsolete boolean
        )with no log;
	create index new_goterm_index on new_goterm (goterm_name,goterm_onto);
	create index goterm_id_index on new_goterm (goterm_id);

         insert into new_goterm (goterm_id,goterm_name,goterm_onto,goterm_is_obsolete) select distinct goterm_id,trim(goterm_name),goterm_onto,'f' from goterm_onto where goterm_id not in (select goterm_go_id from go_term);
       update new_goterm set goterm_zdb_id = get_id("GOTERM");


insert into exist_record
                select g.goterm_zdb_id, p.goterm_zdb_id
                  from go_term g, new_goterm p
                 where g.goterm_go_id = p.goterm_id;

        delete from new_goterm
                where goterm_zdb_id in
                        (select new_zdb_id from exist_record)
                and goterm_name in (select goterm_name from go_term);

!echo 'Insert GOTERM into zdb_active_data'
--       insert into zdb_active_data select goterm_zdb_id from new_goterm;

select goterm_name from new_goterm group by goterm_name having count(*)>1;

select goterm_id from new_goterm group by goterm_id having count(*)>1;

select goterm_name,goterm_onto from new_goterm group by goterm_name,goterm_onto having count(*)>1;

unload to 'updatedterms.unl' select n.goterm_name,g.goterm_name,g.goterm_go_id from new_goterm n,go_term g where n.goterm_id=g.goterm_go_id and n.goterm_name not like g.goterm_name;
update go_term set goterm_name=(select goterm_name from goterm_onto where goterm_id=goterm_go_id);

       insert into zdb_active_data select goterm_zdb_id from new_goterm where goterm_id not in (select goterm_go_id from go_term);
unload to 'newterms.unl' select * from new_goterm where goterm_id not in (select goterm_go_id from go_term)  ;


!echo 'Insert into go_term'
	create temp table goterm (		
                go_zdb_id   varchar(50),
		goterm_id	varchar(10),
		goterm_name	varchar(255),
		goterm_onto	varchar(30),
                goterm_is_obsolete boolean
        )with no log;
       load from newterms.unl insert into go_term ;
--rollback work;
commit work;
