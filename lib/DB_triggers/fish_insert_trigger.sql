create trigger fish_insert_trigger insert on fish
referencing new as new_fish
 for each row (
     execute function scrub_char(new_fish.fish_name)
     	     into fish_name,
     execute function zero_pad (new_fish.fish_name)
     	     into fish_name_order,
    	execute function getFishOrder(new_fish.fish_zdb_id)
    into fish_order, fish_functional_affected_gene_count
);