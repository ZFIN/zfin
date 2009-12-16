package org.zfin.orthology;

import org.zfin.util.FilterType;

/**
 * User: giles
 * Date: Aug 3, 2006
 * Time: 4:28:23 PM
 */

/**
 * Business criteria object for position input created by the OrthologyCriteriaService class which
 * is passed along to the repository by the controller.
 */
public class PositionCriteria {
    private FilterType type;
    private String humanPosCharacter;
    private double position;
    private double min;
    private double max;

    public FilterType getType() {
        return type;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public String getHumanPosCharacter() {
        return humanPosCharacter;
    }

    public void setHumanPosCharacter(String humanPosCharacter) {
        this.humanPosCharacter = humanPosCharacter;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }
}
