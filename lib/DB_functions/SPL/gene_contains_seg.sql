--create marker relationship

{
As of 2003/03, this function is not used anywhere.  However, from an 
e-mail from Tom:

  I made it & used it before the curators had an interface  to enter marker
  relations, so its primary purpose is finished, however I expect that there may
  still be batches of assignments made in which case the functionality would be
  useful

So, we will hang on to it.
}


drop function gene_contains_seg;

create function 
    gene_contains_seg( 
        gene varchar(30), 
        seg varchar(30),
        comments varchar(255)
    ) returning integer;
    
    define mrel varchar(30);
    define test varchar(30);

    let test = "";
    select mrkr_zdb_id into test from marker 
    where  mrkr_zdb_id = gene and mrkr_type = 'GENE';
    if test != gene then 
        return -1;
    end if;
    
    let test = "";
    select mrkr_zdb_id into test from marker 
    where  mrkr_zdb_id = seg  and mrkr_type not in ('GENE','FISH','BAC','PAC','YAC');
    if test != seg then 
        return -2;
    end if;

    let test = 'false';
    select 'true' into test from marker_relationship
    where  mrel_mrkr_1_zdb_id = gene  
    and    mrel_mrkr_2_zdb_id = seg ;
    
    if test = 'true'  then 
        return -3;
    end if;

    let mrel = get_id('MREL');

    insert into zdb_active_data values(mrel);
         
    insert into marker_relationship 
    values ( mrel, 'gene encodes small segment',gene,seg,comments);

    return 0;
end function;

update statistics for function gene_contains_seg;

