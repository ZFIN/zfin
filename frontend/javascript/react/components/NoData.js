import React from 'react';
import PropTypes from 'prop-types';

const NoData = ({placeholder}) => (
    <i className='text-muted'>{placeholder || 'No data available'}</i>
);

NoData.propTypes = {
    placeholder: PropTypes.string,
};

export default NoData;
