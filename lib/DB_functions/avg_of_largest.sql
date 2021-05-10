create or replace function _final_avg_of_largest(final_state numeric[])
    returns numeric as
$$
    declare final_avg numeric;
begin
    select avg(val) into final_avg
    from (
        select val
        from unnest(final_state) val
        order by 1 desc
        limit 10
    ) as mins;

    return final_avg;
end
$$
LANGUAGE plpgsql;

drop aggregate if exists avg_of_largest(numeric);
create aggregate avg_of_largest(numeric) (
    SFUNC=array_append,
    STYPE=numeric[],
    FINALFUNC=_final_avg_of_largest,
    INITCOND='{}'
);
