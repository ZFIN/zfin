package org.zfin.framework;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.hibernate.util.ReflectHelper;
import org.zfin.ontology.Ontology;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Class to convert Enumeration types into Strings that get stored in the database.
 * It also converts a string from the database into an enumeration type.
 * Enumeration items are assumed to always be all upper case while the
 * the string the enumeration is mapped to does not need to be upper case.
 */
public class StringEnumValueUserType implements UserType, ParameterizedType {

    protected Class<Enum> enumClass;

    /**
     * This is called from Hibernate and uses the enumeration class specified
     * in the configuration file.
     *
     * @param parameters enumeration class name
     */
    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty("enumClassname");
        try {
            enumClass = ReflectHelper.classForName(enumClassName);
        } catch (ClassNotFoundException e) {
            throw new HibernateException("Enum class not found.", e);
        }
    }

    /**
     * An enumeration maps to a String type only.
     *
     * @return int[]
     */
    public int[] sqlTypes() {
        return new int[]{Hibernate.STRING.sqlType()};
    }

    public Class returnedClass() {
        return enumClass;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y)
            return true;
        if (x == null || y == null)
            return false;
        return x.equals(y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

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
        if (enumClass.getName().equals(Ontology.class.getName()))
            return Ontology.getOntology(name);
        name = name.toUpperCase();
        return Enum.valueOf(enumClass, name);
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Hibernate.STRING.sqlType());
        } else {
            if (value instanceof Ontology) {
                Ontology ontology = (Ontology) value;
                st.setString(index, ontology.getDbOntologyName());
            } else
                st.setString(index, value.toString());
        }
    }

    /**
     * Since it is immutable this returns the argument without changes.
     *
     * @param value object
     * @return Object
     * @throws HibernateException
     */
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }


    /**
     * Define Enumerations always as immutable.
     *
     * @return Object
     */
    public boolean isMutable() {
        return false;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

}
