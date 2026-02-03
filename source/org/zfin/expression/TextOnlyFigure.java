package org.zfin.expression;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Domain object that describe a figure that is not a figure in the
 * publication but something alike that holdds information said by the
 * authors.
 */
@Entity
@DiscriminatorValue("TOD")
public class TextOnlyFigure extends Figure {

    public FigureType getType(){
        return FigureType.TOD;
    }
}
