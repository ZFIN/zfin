package org.zfin.expression;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Domain object that describes a figure that is a true figure in the
 * publication.
 */
@Entity
@DiscriminatorValue("FIG")
public class FigureFigure extends Figure {

    public FigureType getType(){
        return FigureType.FIGURE;
    }
}
