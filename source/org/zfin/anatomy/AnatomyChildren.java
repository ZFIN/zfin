package org.zfin.anatomy;

import org.apache.commons.lang.ObjectUtils;

import java.io.Serializable;

/**
 */
public class AnatomyChildren implements Serializable {

    private AnatomyItem root;
    private AnatomyItem child;

    public AnatomyItem getRoot() {
        return root;
    }

    public void setRoot(AnatomyItem root) {
        this.root = root;
    }

    public AnatomyItem getChild() {
        return child;
    }

    public void setChild(AnatomyItem child) {
        this.child = child;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnatomyChildren))
            return false;
        AnatomyChildren aoChildren = (AnatomyChildren) o;
        return
                ObjectUtils.equals(root, aoChildren.getRoot()) &&
                        ObjectUtils.equals(child, aoChildren.getChild());
    }

    @Override
    public int hashCode(){
        int hash = 37;
        if (root != null)
            hash = hash * root.hashCode();
        if(child != null)
            hash += hash * child.hashCode();
        return hash;
    }
}
