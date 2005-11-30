create trigger fish_pheno_keywords_update_trigger 
  update of pheno_keywords on fish 
    referencing new as new_fish
    for each row (
	execute function scrub_char(new_fish.pheno_keywords)
          into fish.pheno_keywords
    );
