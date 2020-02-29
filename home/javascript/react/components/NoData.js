import React from 'react';
import PropTypes from 'prop-types';

const NoData = ({placeholder = 'No data available'}) => {
    return <i className='text-muted'>{placeholder}</i>
};

NoData.propTypes = {
    placeholder: PropTypes.string,
};

export default NoData;
