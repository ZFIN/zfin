--Iteration function for aggregator concatenate ;
CREATE FUNCTION concat_iter(result lvarchar, value lvarchar)
	RETURNING lvarchar ; 
	RETURN result || value || " " ;
END FUNCTION ;
