begin work;

update fish_annotation_Search
set fas_gene_order = 'zzzzzzzzzzzzzzzzzz'
 where fas_gene_order is null;

update fish_annotation_Search
set fas_feature_order = 'zzzzzzzzzzzzzzzzzz'
 where fas_feature_order is null;

update fish_annotation_Search
 set fas_fish_significance = '999999'
 where fas_Fish_significance = '0';

update fish_annotation_Search
 set fas_gene_count = '999999'
 where fas_gene_count = '0';

update fish_annotation_Search
 set fas_fish_parts_count = '999999'
 where fas_fish_parts_count = '0';

update fish_annotation_search set fas_fish_significance = '999999' where fas_fish_significance is null;

!echo 'delete wildtype lines should not be more than 42';
delete from fish_annotation_Search
 where fas_feature_group is null
 and fas_gene_group is null
and fas_morpholino_group is null
    and fas_construct_group is null
    ;


commit work ;


