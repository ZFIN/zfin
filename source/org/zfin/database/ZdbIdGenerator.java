package org.zfin.database;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.zfin.construct.ConstructCuration;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.profile.Company;
import org.zfin.profile.Lab;
import org.zfin.repository.RepositoryFactory;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Creates a smart ZDB id as in the web datablade implementation.
 * For that call the get_id() function (C) that creates the key by passong in a
 * type identifier, e.g. 'GENE' which then creates a key of Nmathe form:
 * ZDB-<type>-YYmmdd-<ID>.
 * <p/>
 * If the object is a Marker then this generator will take the object type from
 * marker.getType(). This assumes, the ZDB id will always have the same string
 * as the type!
 * ToDo: Check if this function can be recreated in Java without affecting the c function.
 */
public class ZdbIdGenerator implements IdentifierGenerator, Configurable {

    private static final Logger LOG = LogManager.getLogger(ZdbIdGenerator.class);
    private String objectType;
    private boolean insertActiveData = false;
    private boolean insertActiveSource = false;
    private boolean isMarker = false;
    private boolean isConstruct = false;
    private boolean zdbExists = false;
    private String query = null;


    /**
     * Precreates ZdbIDs in a single session database access.
     *
     * @param session sessionImplementor
     * @param number  number
     * @param type    type
     * @return
     */
    public Set<String> generateZdbIDs(SessionImplementor session, int number, String type,
                                      boolean customInsertActiveData, boolean customInsertActvieSource)
            throws HibernateException, SQLException {

        if (!session.isTransactionInProgress()) {
            throw new HibernateException("Generating ZDB-ID without a transaction: " + type);
        }

        objectType = type;
        setQueryToRetrieveID();


        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

        Set<String> zdbIDs = new HashSet<>(number);
        long startTime = System.currentTimeMillis();
        long lastTime = startTime;
        long currentTime = 0;
        LOG.debug("generating zdbIDS: " + number);
        for (int i = 0; i < number; i++) {
            String zdbID = (String) doWorkInCurrentTransaction(query);
            if (customInsertActiveData) {
                LOG.debug("insertActiveData: " + customInsertActiveData + " for ZdbID[" + zdbID + "]");
                infrastructureRepository.insertActiveData(zdbID);
            }
            if (customInsertActvieSource) {
                LOG.debug("insertActiveSource: " + customInsertActvieSource + " for ZdbID[" + zdbID + "]");
                infrastructureRepository.insertActiveSource(zdbID);
            }
            zdbIDs.add(zdbID);
            if (i % 1000 == 0) {
                currentTime = System.currentTimeMillis();
                LOG.debug("zdbIDS generated: " + i + " " + (i / (number * 1.0f) * 100.0f) + "% time[" + ((currentTime - lastTime) / 1000.0f) + "]");
                lastTime = currentTime;
            }
        }
        LOG.debug("time to generate ZdbIDs: " + (currentTime - startTime));


        return zdbIDs;
    }


    /**
     * This is a convenience method for getting the generated zdbid for a specific type.
     *
     * @return Serializable String that is the ZDBId.
     */
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {

        if (isMarker) {
            Marker marker = (Marker) object;
            if (marker.getZdbID() == null) {
                Marker.Type type = marker.getMarkerType().getType();
                if (type == null)
                    throw new HibernateException("No marker type found for Marker: " + marker.getName());
                objectType = type.toString();
                setQueryToRetrieveID();
            } else {
                zdbExists = true;
            }
        } else if (object instanceof Lab) {
            objectType = "LAB";
            setQueryToRetrieveID();
        } else if (object instanceof Company) {
            objectType = "COMPANY";
            setQueryToRetrieveID();
        } else if (isConstruct) {
            ConstructCuration cng = (ConstructCuration) object;

            objectType = cng.getConstructType().getType().toString();
            setQueryToRetrieveID();
        }

        if (!session.isTransactionInProgress()) {
            throw new HibernateException("Generating ZdbID without a transaction: " + object);
        }

        try {
            if (!zdbExists) {
                String zdbID = (String) doWorkInCurrentTransaction(query);

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
            } else {
                ConstructCuration marker = (ConstructCuration) object;
                return marker.getZdbID();
            }
        } catch (SQLException sqle) {
            LOG.error("Unable to generate a zdbID: " + sqle);
            throw new HibernateException("Failed to generate a zdbID", sqle);
        }
    }


    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        objectType = params.getProperty("type");
        insertActiveData = Boolean.valueOf(params.getProperty("insertActiveData"));
        insertActiveSource = Boolean.valueOf(params.getProperty("insertActiveSource"));
        isMarker = Boolean.valueOf(params.getProperty("isMarker"));
        isConstruct = Boolean.valueOf(params.getProperty("isConstruct"));
        setQueryToRetrieveID();
    }

    private void setQueryToRetrieveID() {
        query = "select get_id('" + objectType + "') ";
    }

    private Serializable doWorkInCurrentTransaction(final String query) throws SQLException {
        Session session = HibernateUtil.currentSession();
        return session.doReturningWork((ReturningWork<Serializable>) connection -> {
                    String result;
                    LOG.debug(query);
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        ResultSet rs = statement.executeQuery();
                        if (!rs.next()) {
                            String err = "could not find sequence value ";
                            LOG.error(err);
                            throw new IdentifierGenerationException(err);
                        }
                        result = rs.getString(1);
                        rs.close();
                    } catch (SQLException sqle) {
                        LOG.error("could not read sequence value", sqle);
                        throw sqle;
                    }
                    return result;
                }
        );
    }


    @Override
    public void registerExportables(Database database) {

    }

    @Override
    public void initialize(SqlStringGenerationContext context) {

    }

    @Override
    public boolean supportsJdbcBatchInserts() {
        return false;
    }
}
