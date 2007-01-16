begin work ;

set constraints all deferred ;

delete from name_precedence ;

load from np.out
insert into name_precedence ;

set constraints all immediate ;

rollback work ;