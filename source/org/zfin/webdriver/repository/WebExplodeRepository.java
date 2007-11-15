package org.zfin.webdriver.repository;

import java.sql.SQLException;

/**
 * Repository to call a web explode function.
 */
public interface WebExplodeRepository {

    /**
     * This method calls a web Explode contents by directly querying the database.
     * @param apgName apg file name
     * @param parameters query parameter string
     * @return html page
     * @throws SQLException if anything fails during webExplode call
     */
    String getWebExplodeContents(String apgName, String parameters) throws SQLException;

}
