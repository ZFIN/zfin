import React, {useMemo} from 'react';
import PropTypes from 'prop-types';
import Autocompletify from '../Autocompletify';

const FPBaseProteinInput = ({limit = 50, ...rest}) => {
    // this needs to be memoized to prevent the autocomplete from being reinitialized on each render
    const typeaheadOptions = useMemo(() => ({
        limit
    }), [limit]);

    return (
        <Autocompletify
            url={'/action/api/efg/fpbase/autocomplete?query=%QUERY'}
            typeaheadOptions={typeaheadOptions}
            {...rest}
        />
    );
};

FPBaseProteinInput.propTypes = {
    limit: PropTypes.number,
    typeGroup: PropTypes.string.isRequired
};

export default FPBaseProteinInput;
