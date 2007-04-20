--Final function for aggregator concatenate ;
CREATE FUNCTION concat_final(final lvarchar)
	RETURNING lvarchar ; 
	RETURN final ; 
END FUNCTION ;
