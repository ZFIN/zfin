package org.zfin.database;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.TransactionHelper;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.type.Type;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

/**
 * Creates a smart ZDB id as in the web datablade implementation.
 * For that call the get_id() function (C) that creates the key by passong in a
 * type identifier, e.g. 'GENE' which then creates a key of the form:
 * ZDB-<type>-YYmmdd-<ID>.
 * <p/>
 * If the object is a Marker then this generator will take the object type from
 * marker.getType(). This assumes, the ZDB id will always have the same string
 * as the type! 
 * ToDo: Check if this function can be recreated in Java without affecting the c function.
 */
public class ZdbIdGenerator extends TransactionHelper implements IdentifierGenerator, Configurable {

    private static final Logger LOG = Logger.getLogger(ZdbIdGenerator.class);
    private String objectType;
    private boolean insertActiveData = false;
    private boolean insertActiveSource = false;
    private boolean isMarker = false;
    private String query = null;

    /**
     * Precreates ZdbIDs in a single session database access.
     *
     * @param session
     * @param number
     * @param type
     * @return
     */
    public Set<String> generateZdbIDs(SessionImplementor session,int number, String type,
                                       boolean customInsertActiveData, boolean customInsertActvieSource)
            throws HibernateException, SQLException
    {

        if (!session.isTransactionInProgress()) {
            throw new HibernateException("Generating ZdbID without a transaction: " + type);
        }

        objectType = type ; 
        setQueryToRetrieveID();



        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

        Set<String> zdbIDs = new HashSet<String>(number) ;
        long startTime  = System.currentTimeMillis() ;
        long lastTime = startTime ;
        long currentTime = 0 ;
        LOG.debug("generating zdbIDS: "+number) ;
        for(int i = 0 ; i <  number ; i++){
            String zdbID = (String) doWorkInCurrentTransaction(session.connection(), query);
            if (customInsertActiveData) {
                LOG.debug("insertActiveData: " + customInsertActiveData + " for ZdbID[" + zdbID + "]");
                infrastructureRepository.insertActiveData(zdbID);
            }
            if (customInsertActvieSource) {
                LOG.debug("insertActiveSource: " + customInsertActvieSource+ " for ZdbID[" + zdbID + "]");
                infrastructureRepository.insertActiveSource(zdbID);
            }
            zdbIDs.add(zdbID) ;
            if(i%1000==0){
                currentTime = System.currentTimeMillis() ;
                LOG.debug("zdbIDS generated: "+i + " "+ (i/(number*1.0f)*100.0f)+"% time["+ ((currentTime - lastTime)/1000.0f) +"]")  ;
                lastTime = currentTime ; 
            }
        }
       LOG.debug("time to generate ZdbIDs: "+ (currentTime- startTime)) ;


        return zdbIDs ;
    }



    /**
     * This is a convenience method for getting the generated zdbid for a specific type.
     *
     * @return Serializable String that is the ZDBId.
     */
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {

        if (isMarker) {
            Marker marker = (Marker) object;
            Marker.Type type = marker.getMarkerType().getType();
            if (type == null)
                throw new HibernateException("No marker type found for Marker: " + marker.getName());
            objectType = type.toString();
            setQueryToRetrieveID();
        }

        if (!session.isTransactionInProgress()) {
            throw new HibernateException("Generating ZdbID without a transaction: " + object);
        }

        try {
            String zdbID = (String) doWorkInCurrentTransaction(session.connection(), query);
            LOG.debug("Sequence for <" + objectType + "> is [" + zdbID + "]");
            if (insertActiveData) {
                LOG.debug("insertActiveData: " + insertActiveData + " for ZdbID[" + zdbID + "]");
                InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
                infrastructureRepository.insertActiveData(zdbID);
            }
            if (insertActiveSource) {
                LOG.debug("insertActiveSource: " + insertActiveSource + " for ZdbID[" + zdbID + "]");
                InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
                infrastructureRepository.insertActiveSource(zdbID);
            }
            return zdbID;
        }
        catch (SQLException sqle) {
            LOG.error("Unable to generate a zdbID: " + sqle);
            throw new HibernateException("Failed to generate a zdbID", sqle);
        }
    }

    public void configure(Type type, Properties params, Dialect d) throws MappingException {
        objectType = params.getProperty("type");
        insertActiveData = Boolean.valueOf(params.getProperty("insertActiveData"));
        insertActiveSource = Boolean.valueOf(params.getProperty("insertActiveSource"));
        isMarker = Boolean.valueOf(params.getProperty("isMarker"));
        setQueryToRetrieveID();
    }

    private void setQueryToRetrieveID() {
        query = "execute function get_id('" + objectType + "') ";
    }

    protected Serializable doWorkInCurrentTransaction(Connection conn, String sql) throws SQLException {
        String result;
        LOG.debug(query);
        PreparedStatement statement = conn.prepareStatement(query);
        try {
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                String err = "could not find sequence value ";
                LOG.error(err);
                throw new IdentifierGenerationException(err);
            }
            result = rs.getString(1);
            rs.close();
        }
        catch (SQLException sqle) {
            LOG.error("could not read sequence value", sqle);
            throw sqle;
        }
        finally {
            statement.close();
        }
        return result;
    }
}
