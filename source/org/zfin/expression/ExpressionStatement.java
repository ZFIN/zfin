package org.zfin.expression;

import org.zfin.ontology.PostComposedEntity;

/**
 * This class is not currently mapped in hibernate, but will be built at the
 * repository or service level in anticipation of future database changes
 *
 * it is meant to be equivalent to the E+Q+E+Tag of PhenotypeStatement
 */
public class ExpressionStatement implements Comparable<ExpressionStatement> {
    private PostComposedEntity entity;
    private boolean isExpressionFound;

    public PostComposedEntity getEntity() {
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
    }

    public boolean isExpressionFound() {
        return isExpressionFound;
    }

    public void setExpressionFound(boolean expressionFound) {
        isExpressionFound = expressionFound;
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder(100);
        if (!isExpressionFound) {
            sb.append("not ");
        }
        sb.append(entity.getDisplayName());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionStatement that = (ExpressionStatement) o;

        if (isExpressionFound != that.isExpressionFound) return false;
        if (!entity.equals(that.entity)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entity.hashCode();
        result = 31 * result + (isExpressionFound ? 1 : 0);
        return result;
    }


    @Override
    public int compareTo(ExpressionStatement o) {
        if (this.equals(o))
            return 0;

        //only look at the boolean if the entity parts aren't equal
        if (entity.compareTo(o.getEntity()) == 0) {
            //show expressed before not expressed
            if (isExpressionFound && !o.isExpressionFound())
                return 1;
            else
                return -1;
        } else {
            return entity.compareTo(o.getEntity());
        }
    }
}
