drop trigger chr_pub_integ;

-- This trigger is left over from the days when ZFIN did not have 
-- foreign keys.  This trigger should be dropped when the fish tables
-- are cleaned up.

create trigger chr_pub_integ 
  delete on chromosome 
    referencing old as this
    for each row (
        delete from alteration
	  where (chrom_id = this.zdb_id),
        delete from int_fish_chromo
	  where (target_id = this.zdb_id)
    );
