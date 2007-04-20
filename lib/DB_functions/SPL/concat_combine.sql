--Combination function for aggregator concatenate ;
CREATE FUNCTION concat_combine(result lvarchar, value lvarchar)
	RETURNING lvarchar ; 
	RETURN result ;
END FUNCTION ;
