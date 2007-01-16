begin work ;

set constraints all deferred ;

insert into alias_group 
  values ('plural', '3') ;

insert into data_alias (dalias_zdb_id,
				dalias_data_zdb_id,
				dalias_alias,
				dalias_group)
  values (get_id('DALIAS'),
		(Select anatitem_zdb_id
		   from anatomy_item
		   where anatitem_name = 'cranial nerve'),
		'cranial nerves',
		'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'mucous cell'),
                'mucous cells',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'primordial germ cell'),
                'primordial germ cells',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'neuron'),
                'neurons',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'slow muscle cell'),
                'slow muscle cells',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item   
                   where anatitem_name = 'fast muscle cell'),
                'fast muscle cells',
                'plural');



insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'fin'),
                'fins',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'semicircular canal'),
                'semicircular canals',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'xanthophore'),
                'xanthophores',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'neuromast posterior'),
                'neuromasts posterior',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'iridophore'),
                'iridophores',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'sensory hair cell'),
                'sensory hair cells',
                'plural');

insert into data_alias (dalias_zdb_id,
                                dalias_data_zdb_id,
                                dalias_alias,
                                dalias_group)
  values (get_id('DALIAS'),
                (Select anatitem_zdb_id
                   from anatomy_item
                   where anatitem_name = 'neuromast'),
                'neuromasts',
                'plural');


select count(*), dalias_datA_zdb_id, dalias_alias
  from data_alias
  group by dalias_datA_zdb_id, dalias_alias
	having count(*) > 1;

insert into zdb_active_data
  select dalias_zdb_id
    from data_alias
    where not exists (select 'x'
			from zdb_Active_Data
			where dalias_zdb_id =zactvd_zdb_id); 


set constraints all immediate ;

--commit work ;

rollback work ;
