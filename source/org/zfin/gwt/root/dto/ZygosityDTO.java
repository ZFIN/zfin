package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 */
public class ZygosityDTO extends RelatedEntityDTO implements IsSerializable, Serializable {

    protected String zdbID;  // the primary key of this object
    protected String name;

    public ZygosityDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Type getType() {
        return Type.getZygosity(name);
    }

    public String getMutantZygosityDisplay(String featureName) {
        StringBuilder builder = new StringBuilder(featureName);
        if (Type.getZygosity(name).equals(Type.HOMOZYGOUS)) {
            builder.append("/");
            builder.append(featureName);
        } else if (Type.getZygosity(name).equals(Type.HETEROZYGOUS)) {
            builder.append("/");
            builder.append("+");
        }
        return builder.toString();
    }

        enum Type {
            HOMOZYGOUS("homozygous", "2"),
            HETEROZYGOUS("heterozygous", "1"),
            UNKNOWN("unknown", "U"),
            COMPLEX("complex", "C"),
            WILD_TYPE("wild type", "W");
            private String name;
            private String symbol;

            Type(String name, String symbol) {
                this.name = name;
                this.symbol = symbol;
            }

            public String getName() {
                return name;
            }

            public String getSymbol() {
                return symbol;
            }

            public static Type getZygosity(String name) {
                for (Type type : values())
                    if (type.getName().equals(name))
                        return type;
                return null;
            }
        }
    }
