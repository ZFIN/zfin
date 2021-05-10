import React from 'react';
import PropTypes from 'prop-types';

const LoadingSpinner = ({loading}) => loading ? (<span><i className='fas fa-spinner fa-spin' /></span>) : null;

LoadingSpinner.propTypes = {
    loading: PropTypes.bool,
};

LoadingSpinner.defaultProps = {
    loading: true
};

export default LoadingSpinner;
