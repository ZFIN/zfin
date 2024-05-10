package org.zfin.framework;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.zfin.mapping.GenomeLocation;
import org.zfin.ontology.Ontology;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

//TODO: hibernate migration changes: confirm changes to this class still work
/**
 * Class to convert Enumeration types into Strings that get stored in the database.
 * It also converts a string from the database into an enumeration type.
 * Enumeration items are assumed to always be all upper case while the
 * the string the enumeration is mapped to does not need to be upper case.
 */
public class StringEnumValueUserType implements EnhancedUserType<Enum>, ParameterizedType {
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
        return new int[]{StandardBasicTypes.STRING.getSqlTypeCode()};
    }

    @Override
    public int getSqlType() {
        return StandardBasicTypes.STRING.getSqlTypeCode();
    }

    public Class returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Enum x, Enum y) {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(Enum x) {
        return x.hashCode();
    }

    @Override
    public Enum nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        String name = rs.getString(position);
        if (rs.wasNull()) {
            return null;
        }
        return fromStringOrValueOf(name);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Enum value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, StandardBasicTypes.STRING.getSqlTypeCode());
        } else {
            if (value instanceof Ontology ontology) {
                st.setString(index, ontology.getDbOntologyName());
            } else if (value instanceof GenomeLocation.Source) {
                st.setString(index, ((GenomeLocation.Source) value).getName());
            } else {
                st.setString(index, value.toString());
            }
        }
    }

    /**
     * Since it is immutable, this returns the argument without changes.
     *
     * @param value object
     * @return Object
     * @throws HibernateException
     */
    @Override
    public Enum deepCopy(Enum value) throws HibernateException {
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

    @Override
    public Serializable disassemble(Enum value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Enum assemble(Serializable cached, Object owner) throws HibernateException {
        return (Enum) cached;
    }

    @Override
    public Enum replace(Enum original, Enum target, Object owner) throws HibernateException {
        return original;
    }

    @Override
    public String toSqlLiteral(Enum value) {
        return "'" + toString(value) + "'";
    }

    @Override
    public String toString(Enum value) throws HibernateException {
        if (value instanceof Ontology ontology) {
            return ontology.getDbOntologyName();
        } else if (value instanceof GenomeLocation.Source) {
            return ((GenomeLocation.Source) value).getName();
        } else {
            return value.toString();
        }
    }

    @Override
    public Enum fromStringValue(CharSequence sequence) throws HibernateException {
        return fromStringOrValueOf(sequence.toString());
    }

    private Enum fromStringOrValueOf(String name) {
        if (enumClass.getName().equals(GenomeLocation.Source.class.getName())) {
            return GenomeLocation.Source.getSource(name);
        }

        try {
            Method fromString = enumClass.getMethod("fromString", String.class);
            return (Enum) fromString.invoke(null, name);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) { }

        name = name.replace('-', '_')
                .replace('+', '_')
                .replace(' ', '_')
                .replace('(', '_')
                .replace(')', '_')
                .toUpperCase();

        if (enumClass.getName().equals(Ontology.class.getName())) {
            return Ontology.getOntology(name);
        }

        return Enum.valueOf(enumClass, name);
    }
}
