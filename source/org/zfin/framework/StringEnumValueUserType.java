package org.zfin.framework;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.zfin.mapping.GenomeLocation;
import org.zfin.ontology.Ontology;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        return new int[]{StandardBasicTypes.STRING.sqlType()};
    }

    public Class returnedClass() {
        return enumClass;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        String name = rs.getString(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        if (enumClass.getName().equals(GenomeLocation.Source.class.getName())) {
            return GenomeLocation.Source.getSource(name);
        }

        try {
            Method fromString = enumClass.getMethod("fromString", String.class);
            return fromString.invoke(null, name);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) { }

        // convert enumeration name into upper case as the names are always all upper case.
        // this needs to be done if the enumeration string is not all caps.
        name = name.replace('-', '_');
        name = name.replace('+', '_');
        name = name.replace(' ', '_');
        name = name.replace('(', '_');
        name = name.replace(')', '_');
        if (enumClass.getName().equals(Ontology.class.getName())) {
            return Ontology.getOntology(name);
        }
        name = name.toUpperCase();
        return Enum.valueOf(enumClass, name);
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, StandardBasicTypes.STRING.sqlType());
        } else {
            if (value instanceof Ontology) {
                Ontology ontology = (Ontology) value;
                st.setString(index, ontology.getDbOntologyName());
            } else if (value instanceof GenomeLocation.Source) {
                st.setString(index, ((GenomeLocation.Source) value).getName());
            } else {
                st.setString(index, value.toString());
            }
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
