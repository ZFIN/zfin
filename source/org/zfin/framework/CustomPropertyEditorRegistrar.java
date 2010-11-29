package org.zfin.framework;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.AntibodyTypeEditor;
import org.zfin.expression.Assay;
import org.zfin.expression.AssayEditor;
import org.zfin.util.FilterType;
import org.zfin.util.FilterTypeEditor;

/**
 */
public final class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        registry.registerCustomEditor(FilterType.class,new FilterTypeEditor());
        registry.registerCustomEditor(Assay.class,new AssayEditor());
        registry.registerCustomEditor(AntibodyType.class,new AntibodyTypeEditor());
    }
}
