import React from 'react';
import PropTypes from 'prop-types';

const LoadingSpinner = ({loading = true}) => loading ? (<span><i className='fas fa-spinner fa-spin' /></span>) : null;

LoadingSpinner.propTypes = {
    loading: PropTypes.bool,
};

export default LoadingSpinner;
