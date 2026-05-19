package org.zfin.zirc.api.uischema;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Top-level layout: stacks its children vertically. The current ZIRC
 * forms always have exactly one {@code VerticalLayout} at the root of
 * the uiSchema with all the {@link Group} sections inside.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerticalLayout(List<UiSchemaElement> elements)
        implements UiSchemaElement {

    @Override
    public String type() {
        return "VerticalLayout";
    }
}
