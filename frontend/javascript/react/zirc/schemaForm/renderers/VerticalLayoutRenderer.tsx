import * as React from 'react';
import {
    JsonFormsRendererRegistryEntry,
    LayoutProps,
    rankWith,
    uiTypeIs,
    VerticalLayout,
} from '@jsonforms/core';
import { ResolvedJsonFormsDispatch, withJsonFormsLayoutProps } from '@jsonforms/react';

/**
 * Trivial VerticalLayout renderer: render children in declaration order, no
 * wrapping element. Sections come from the Group renderer; this is the
 * top-level glue between Groups in the form-schema's VerticalLayout root.
 */
function VerticalLayoutRenderer({
    uischema,
    schema,
    path,
    enabled,
    renderers,
    cells,
}: LayoutProps) {
    const layout = uischema as VerticalLayout;
    return (
        <>
            {(layout.elements ?? []).map((child, index) => (
                <ResolvedJsonFormsDispatch
                    key={`${path}-${index}`}
                    uischema={child}
                    schema={schema}
                    path={path}
                    enabled={enabled}
                    renderers={renderers}
                    cells={cells}
                />
            ))}
        </>
    );
}

export const verticalLayoutRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(10, uiTypeIs('VerticalLayout')),
    renderer: withJsonFormsLayoutProps(VerticalLayoutRenderer),
};
