package org.zfin.sequence.blast.results.view;

/**
 */
public class ExpressionMapBean {

    protected boolean hasExpression ;
    protected boolean hasExpressionImages;
    protected boolean hasGO ;
    protected boolean hasPhenotype ;
    protected boolean hasPhenotypeImages ;

    public boolean isHasExpression() {
        return hasExpression;
    }

    public void setHasExpression(boolean hasExpression) {
        this.hasExpression = hasExpression;
    }

    public boolean isHasPhenotype() {
        return hasPhenotype;
    }

    public void setHasPhenotype(boolean hasPhenotype) {
        this.hasPhenotype = hasPhenotype;
    }

    public boolean isHasPhenotypeImages() {
        return hasPhenotypeImages;
    }

    public void setHasPhenotypeImages(boolean hasPhenotypeImages) {
        this.hasPhenotypeImages = hasPhenotypeImages;
    }

    public boolean isHasGO() {
        return hasGO;
    }

    public void setHasGO(boolean hasGO) {
        this.hasGO = hasGO;
    }

    public boolean isHasExpressionImages() {
        return hasExpressionImages;
    }

    public void setHasExpressionImages(boolean hasExpressionImages) {
        this.hasExpressionImages = hasExpressionImages;
    }

    @Override
    public String toString() {
        return "ExpressionMapBean{" +
                "hasExpression=" + hasExpression +
                ", hasExpressionImages=" + hasExpressionImages +
                ", hasGO=" + hasGO +
                ", hasPhenotype=" + hasPhenotype +
                ", hasPhenotypeImages=" + hasPhenotypeImages +
                '}';
    }
}
