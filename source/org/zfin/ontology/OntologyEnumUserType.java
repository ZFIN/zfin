package org.zfin.ontology;

import org.hibernate.HibernateException;
import org.zfin.framework.StringEnumValueUserType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class to convert Enumeration types into Strings that get stored in the database.
 * It also converts a string from the database into an enumeration type.
 * Enumeration items are assumed to always be all upper case while the
 * the string the enumeration is mapped to does not need to be upper case.
 */
public class OntologyEnumUserType extends StringEnumValueUserType {


    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        String name = rs.getString(names[0]);
        if (rs.wasNull())
            return null;
        // convert enumeration name into upper case as the names are always all upper case.
        // this needs to be done if the enumeration string is not all caps.
        name = name.replace('-', '_');
        name = name.replace('+', '_');
        name = name.replace(' ', '_');
        name = name.replace('(', '_');
        name = name.replace(')', '_');
        return Ontology.getOntology(name);
    }


}
