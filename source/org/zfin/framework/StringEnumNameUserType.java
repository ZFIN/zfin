package org.zfin.framework;

import org.hibernate.HibernateException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class to convert Enumeration types into Strings that get stored in the database.
 * It also converts a string from the database into an enumeration type.
 * Enumeration items are assumed to always be all upper case while the
 * the string the enumeration is mapped to does not need to be upper case.
 */
public class StringEnumNameUserType extends StringEnumValueUserType{


    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        String name = rs.getString(names[0]);
        if (rs.wasNull())
            return null;

        Enum[] enums = enumClass.getEnumConstants() ;
        for(Enum anEnum: enums){
            if(anEnum.name().equals(name)){
                return anEnum;
            }
        }
        throw new HibernateException("Failed to map enum for name: "+name) ;
    }


}
