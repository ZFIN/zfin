import React from 'react';
import PropTypes from 'prop-types';

const SequenceInput = (props) => {
    return (
        <div className='input-group'>
            <div className='input-group-prepend'>
                <span className='input-group-text'>5&apos; - </span>
            </div>
            <input {...props} />
            <div className='input-group-append'>
                <span className='input-group-text'> - 3&apos;</span>
            </div>
        </div>
    );
};

SequenceInput.propTypes = {
    onChange: PropTypes.func,
    type: PropTypes.string,
};

export default SequenceInput;
