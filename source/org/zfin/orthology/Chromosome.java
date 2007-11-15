package org.zfin.orthology;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Business object for storing a chromosome number.  Contains a reference to the
 * corresponding position object for the given gene.
 */
public class Chromosome  implements Serializable {
    private String number;
    private Position position;

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public String toString() {
        if (number != null) {
            if (position == null || position.getPosition() == null || StringUtils.isEmpty(position.getPosition())) {
                return number;
            } else {
                return number + " (" + position.getPosition() + ")";
            }
        } else return "";
    }
}
