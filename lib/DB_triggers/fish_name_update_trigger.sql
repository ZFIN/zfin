create trigger fish_name_update_trigger update of fish_name on fish
  referencing new as new_fish
 for each row (
     execute function scrub_char(new_fish.fish_name)
     	     into fish_name,
     execute function zero_pad (new_fish.fish_name)
     	     into fish_name_order,
	     execute function getFishOrder(new_fish.fish_zdb_id)
    into fish_order, fish_functional_affected_gene_count,
    execute function get_fish_full_name(new_fish.fish_zdb_id, new_fish.fish_genotype_zdb_id, new_fish.fish_name)
    into fish_full_name
);
