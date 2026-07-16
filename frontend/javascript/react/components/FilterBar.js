import React from 'react';
import PropTypes from 'prop-types';

const FilterBar = ({children}) => {
    return (
        <div className='row filter-bar'>
            <div className='col-md-12'>
                <form className='form-inline'>
                    {children}
                </form>
            </div>
        </div>
    );
};

FilterBar.propTypes = {
    children: PropTypes.node,
};

export default FilterBar;
