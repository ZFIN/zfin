import java.lang.*;
import java.sql.*;
import com.informix.udr.*;

/**
 *	Example UDRs to demonstrate the use of the UDREnv object.
 */

public class Env
{

    /**	
     * Return the UDR's SQL signature.
     */

    public static String signature(int xint, String xchar, String
		xvchar, boolean xboolean)
    	throws SQLException
    {

String res = "not changed" ; 
      try{
	UDREnv env = UDRManager.getUDREnv();
	res = env.getReturnTypeName() + " " + env.getName() + "(";

	String param[] = env.getParamTypeName();
	for (int j = 0; j < param.length; ++ j)
	    {
	    if (j > 0)
		res += ",";
	    res += param[j];
	    }
	res += ")";
	return res;
}
      catch(Throwable t ){
  return res +"\n" + t.toString() ; 
}
    }

    public int count;

    /**
     * A simple iterative function that count down from N through 1.
     */

    public static String countDown(int N)
	throws SQLException
    {
	UDREnv env = UDRManager.getUDREnv();


	int iter = env.getSetIterationState();
	UDRLog log = env.getLog();

	if (iter == UDREnv.UDR_SET_INIT)
	    {
	    /* before 1st call, allocate state object and set its value */
	    Env state = new Env();
	    state.count = N;
	    env.setUDRState(state);
   	    env.setSetIterationIsDone(false);
	    return null;
	    }
	else if (iter == UDREnv.UDR_SET_END)
	    {
	    /* after last call, clean up (a no-op for this example) */
   	    env.setSetIterationIsDone(true);
	    return null;
	    }	
	else if (iter == UDREnv.UDR_SET_RETONE)
	    {
	    Env state = (Env)env.getUDRState();
	    -- state.count;

	    if (state.count < 0)
		env.setSetIterationIsDone(true);
	    else
		env.setSetIterationIsDone(false);

	    log.log("#" + (state.count + 1));
	    return "#" + (state.count + 1);
	    }
	else
	    throw new SQLException("Unknown iterator code");
    }
}
