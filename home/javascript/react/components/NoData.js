import React from 'react';
import PropTypes from 'prop-types';

const NoData = ({placeholder}) => (
    <i className='text-muted'>{placeholder || 'No Data Available'}</i>
);

NoData.propTypes = {
    placeholder: PropTypes.string,
};

export default NoData;
