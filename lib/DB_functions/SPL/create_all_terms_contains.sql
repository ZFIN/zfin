----------------------------------------------------------------
-- This function creates a mirror table from the all_anatomy_contains that points
-- to the TERM table rather than the anatomy_item table.
--
-- INPUT VARS:
--             none
--
-- OUTPUT VARS:
--              None
-- EFFECTS:
-------------------------------------------------------------

CREATE procedure create_all_terms_contains()
	returning varchar;

    define container_term_zdb_id    lvarchar;
    define contained_term_zdb_id       lvarchar;
    define distance       integer;

begin

    delete from all_term_contains;
     
    foreach
        select term_contained.term_zdb_id, term_container.term_zdb_id, allanatcon_min_contain_distance
           into contained_term_zdb_id, container_term_zdb_id, distance
        from all_anatomy_contains, anatomy_item as ao_contained, anatomy_item as ao_container,
             term as term_contained, term as term_container
        where allanatcon_container_zdb_id = ao_container.anatitem_zdb_id
              and ao_container.anatitem_obo_id = term_container.term_ont_id
              and allanatcon_contained_zdb_id = ao_contained.anatitem_zdb_id
              and ao_contained.anatitem_obo_id = term_contained.term_ont_id

           insert into all_term_contains (alltermcon_container_zdb_id,alltermcon_contained_zdb_id,alltermcon_min_contain_distance)
           values(container_term_zdb_id, contained_term_zdb_id, distance);
     end foreach

end
end procedure

