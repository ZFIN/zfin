package org.zfin.expression;

/**
 * Domain object that describe a figure that is not a figure in the
 * publication but something alike that holdds information said by the
 * authors.
 */
public class TextOnlyFigure extends Figure {

    public Type getType(){
        return Type.TOD;
    }
}
