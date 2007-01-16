begin work ;

update statistics for procedure ;

insert into image
  select * from fish_image ;

insert into image_stage
  select * from fish_image_stage ;

commit work ;

--rollback work ;