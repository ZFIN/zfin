import React from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';

const LoadingButton = ({children, loading, disabled = false, ...rest}) => {
    return (
        <button type='button' disabled={disabled || loading} {...rest}>
            <LoadingSpinner loading={loading} />
            { !loading && children }
        </button>
    );
};

LoadingButton.propTypes = {
    children: PropTypes.node,
    loading: PropTypes.bool,
    disabled: PropTypes.bool,
};

export default LoadingButton;
