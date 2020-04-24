import React from 'react';
import PropTypes from 'prop-types';

/*const NoData = ({placeholder = 'No data available'}) => {
    return <i className='text-muted'>{placeholder}</i>
};*/

const NoData = ({placeholder}) => {
    if (placeholder) {
        return <i className='text-muted'>{placeholder}</i>
    }
    else{
        return <i className='text-muted'>No data available</i>
    }
};

NoData.propTypes = {
    placeholder: PropTypes.string,
};

export default NoData;
