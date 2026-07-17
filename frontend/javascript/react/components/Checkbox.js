import React from 'react';
import PropTypes from 'prop-types';

const Checkbox = ({children, id, ...rest}) => {
    return (
        <div className='custom-control custom-checkbox'>
            <input
                type='checkbox'
                id={id}
                className='custom-control-input'
                {...rest}
            />
            <label className='custom-control-label' htmlFor={id}>
                {children}
            </label>
        </div>
    );
};

Checkbox.props = {
    children: PropTypes.node,
    id: PropTypes.string.isRequired,
};

export default Checkbox;
