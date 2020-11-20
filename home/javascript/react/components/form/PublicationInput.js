import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import Autocompletify from '../Autocompletify';

const PublicationInput = ({defaultPubs, limit = 50, ...rest}) => {
    // this needs to be memoized to prevent the autocomplete from being reinitialized on each render
    const typeaheadOptions = useMemo(() => ({
        displayKey: 'id',
        defaultSuggestions: defaultPubs,
        limit: limit,
        highlight: true,
        templates: {
            suggestion: item => (`
                            <div>
                                <div>${item.id}</div>
                                <div class="text-muted">${item.name}</div>
                            </div>
                        `),
            notFound: '<i class="tt-item text-muted">No publications match query</i>',
            pending: '<span class="tt-item text-muted"><span><i class="fas fa-spinner fa-spin"></i> Searching</span></span>'
        }
    }), [defaultPubs, limit]);

    return (
        <Autocompletify
            url={`/action/quicksearch/autocomplete?q=%QUERY&category=Publication&rows=${limit}`}
            placeholder='Enter ZDB-PUB ID or search for pub'
            typeaheadOptions={typeaheadOptions}
            {...rest}
        />
    );
};

PublicationInput.propTypes = {
    defaultPubs: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
    })),
    limit: PropTypes.number,
};

export default PublicationInput;
