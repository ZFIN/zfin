import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import Autocompletify from '../Autocompletify';

const MarkerInput = ({typeGroup, limit = 50, ...rest}) => {
    // this needs to be memoized to prevent the autocomplete from being reinitialized on each render
    const typeaheadOptions = useMemo(() => ({
        limit
    }), [limit]);

    return (
        <Autocompletify
            url={`/action/autocomplete/marker?query=%QUERY&typeGroup=${typeGroup}`}
            typeaheadOptions={typeaheadOptions}
            {...rest}
        />
    );
};

MarkerInput.propTypes = {
    limit: PropTypes.number,
    typeGroup: PropTypes.string.isRequired
};

export default MarkerInput;
