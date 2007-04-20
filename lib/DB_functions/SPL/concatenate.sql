CREATE AGGREGATE concatenate
WITH ( 
INIT = concat_init,
ITER = concat_iter, 
COMBINE = concat_combine,
FINAL = concat_final
) ;
