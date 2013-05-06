package org.zfin.database;

import com.informix.asf.Connection;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: cmpich
 * Date: 3/12/13
 * Time: 1:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class JndiTest {

    public static void main(String[] arguments) throws NamingException, SQLException {

        initJNDI();
        Context ctx = new InitialContext();
        DataSource ds = (DataSource)ctx.lookup("jdbc/zfin");
        Connection con = (Connection) ds.getConnection("zfinner", "Rtwm4ts");
        String name = con.getUserName();
    }

    private static void initJNDI() throws NamingException {
        DataSource vds = new DataSource();
        vds.setDriverClassName("com.informix.jdbc.IfxDriver");
        Context ctx = new InitialContext();
        ctx.bind("jdbc/zfin", vds);
    }

}
