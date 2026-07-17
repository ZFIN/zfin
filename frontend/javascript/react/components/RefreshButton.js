import React from 'react';
import PropTypes from 'prop-types';

const RefreshButton = ({loading, onClick}) => (
    <button
        title='Refresh query'
        className='btn btn-link pub-dashboard-reload'
        disabled={loading}
        type='button'
        onClick={onClick}
    >
        <span className={`fa-animation-container ${loading ? 'fa-spin' : ''}`}><i className='fas fa-sync' /></span>
    </button>
);

RefreshButton.propTypes = {
    loading: PropTypes.bool,
    onClick: PropTypes.func,
};

export default RefreshButton;
