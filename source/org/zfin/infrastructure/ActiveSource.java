package org.zfin.infrastructure;

import org.zfin.infrastructure.delete.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Entity
@Table(name = "zdb_active_source")
public class ActiveSource implements ZdbID, Serializable {

    @Id
    @Column(name = "zactvs_zdb_id")
    private String zdbID;
    public static final String ZDB = "ZDB-";

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        validateID(zdbID);
        this.zdbID = zdbID;
    }

    public static boolean validateActiveData(String id) {
        if (id == null) {
            return false;
        }

        if (!id.startsWith(ActiveSource.ZDB))
            return false;

        Type type = null;
        for (Type zdbType : Type.values()) {
            if (id.contains(zdbType.name())) {
                type = zdbType;
            }
        }
        return type != null;
    }

    public static boolean isValidActiveData(String id, Type type) {
        if (type == null || id == null) {
            return false;
        }
        return id.contains(type.name());
    }

    public static Type validateID(String zdbID) {
        if (zdbID == null) {
            throw new InvalidZdbID();
        }

        if (!zdbID.startsWith(ActiveSource.ZDB))
            throw new InvalidZdbID(zdbID);

        Type type = null;
        for (Type zdbType : Type.values()) {
            if (zdbID.contains(zdbType.name())) {
                type = zdbType;
            }
        }

        if (type == null) {
            throw new InvalidZdbID(zdbID, Type.getValues());
        }
        return type;
    }

    public enum Type implements Serializable {
        SALIAS(null),
        COMPANY(DeleteCompanyRule.class),
        JRNL(DeleteJournalRule.class),
        LAB(DeleteLabRule.class),
        PERS(DeletePersonRule.class),
        PUB(DeletePublicationRule.class);

        private static String allValues;
        private Class<? extends DeleteEntityRule> ruleClass;


        Type() {
        }

        Type(Class<? extends DeleteEntityRule> ruleClass) {
            this.ruleClass = ruleClass;
        }

        public static String getValues() {
            if (allValues != null)
                return allValues;
            StringBuilder sb = new StringBuilder("[");
            int size = values().length;
            int index = 0;
            for (Type type : values()) {
                sb.append(type.name());
                index++;
                if (index < size)
                    sb.append(",");
            }
            sb.append("]");
            allValues = sb.toString();
            return allValues;
        }

        public DeleteEntityRule getDeleteEntityRule(String zdbID) {
            DeleteEntityRule rule = null;
            try {
                Constructor constructor = ruleClass.getConstructor(String.class);
                rule = (DeleteEntityRule) constructor.newInstance(zdbID);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return rule;
        }

    }
}
