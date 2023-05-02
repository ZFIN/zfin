import React from 'react';
import PropTypes from 'prop-types';

const Radiobutton = ({children, id, ...rest}) => {
    return (
        <div className='custom-control custom-checkbox'>
            <input
                type='radio'
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

Radiobutton.props = {
    children: PropTypes.node,
    id: PropTypes.string.isRequired,
};

export default Radiobutton;
