import React from 'react';
import PropTypes from 'prop-types';
import Autocompletify from './Autocompletify';

const PublicationInput = ({defaultPubs, limit = 50, ...rest}) => {
    return (
        <Autocompletify
            url={`/action/quicksearch/autocomplete?q=%QUERY&category=Publication&rows=${limit}`}
            placeholder='Enter ZDB-PUB ID or search for pub'
            typeaheadOptions={{
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
                }
            }}
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
