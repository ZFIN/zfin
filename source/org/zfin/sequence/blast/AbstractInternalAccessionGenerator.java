package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.zfin.framework.HibernateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractInternalAccessionGenerator {

    private static final Logger logger = Logger.getLogger(AbstractInternalAccessionGenerator.class);

    public abstract String getInternalAcessionHeader();

    public synchronized String generateAccession() {
        Session session = HibernateUtil.currentSession();
        Connection connection = session.connection();
        String accessionNum;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("execute function getZfinAccessionNumber('" + getInternalAcessionHeader() + "');");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                accessionNum = resultSet.getString(1);
            } else {
                logger.error("failed to return sequence value");
                return null;
            }
        }
        catch (SQLException sqlException) {
            logger.error(sqlException);
            throw new RuntimeException("failed to generate sequence accession", sqlException);
        }
        return accessionNum;
    }
}
