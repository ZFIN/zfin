CREATE OR REPLACE FUNCTION convert_gene_to_ncrna(
    geneId VARCHAR
) RETURNS text AS $$
BEGIN

return convert_gene_to_type(geneId, 'NCRNAG');

END;
$$ LANGUAGE plpgsql;
