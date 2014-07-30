SELECT fas_genox_group
FROM fish_annotation_search
WHERE LENGTH(fas_genox_group) > 7000
ORDER BY LENGTH(fas_genox_group) DESC
