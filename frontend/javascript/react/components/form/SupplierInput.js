import React, { useMemo } from 'react';
import Autocompletify from '../Autocompletify';

const SupplierInput = (props) => {
    // this needs to be memoized to prevent the autocomplete from being reinitialized on each render
    const typeaheadOptions = useMemo(() => ({
        displayKey: 'label',
        highlight: true,
        templates: {
            suggestion: item => (`
                            <div>
                                <div>${item.id}</div>
                                <div class="text-muted">${item.label}</div>
                            </div>
                        `),
            notFound: '<i class="tt-item text-muted">No suppliers match query</i>',
            pending: '<span class="tt-item text-muted"><span><i class="fas fa-spinner fa-spin"></i> Searching</span></span>'
        }
    }), []);

    return (
        <Autocompletify
            url='/action/marker/find-suppliers?term=%QUERY'
            placeholder='Search for supplier'
            typeaheadOptions={typeaheadOptions}
            {...props}
        />
    );
};

SupplierInput.propTypes = {
};

export default SupplierInput;
