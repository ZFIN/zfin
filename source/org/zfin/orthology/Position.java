package org.zfin.orthology;

import java.io.Serializable;

/**
 * User: giles
 * Date: Jul 13, 2006
 * Time: 3:38:46 PM
 */

/**
 * Business object which holds information about the position of a gene along a chromosome.
 */
public class Position  implements Serializable {
    String position;

    public void setPosition(String position) {
        this.position = position;    
    }

    public String getPosition() {
        return position;
    }

}
