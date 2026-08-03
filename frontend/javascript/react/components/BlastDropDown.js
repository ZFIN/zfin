import React from 'react';
import PropTypes from 'prop-types';

const BlastDropDown = ({dbLink}) => {
    return (
        <div className='dropdown'>
            <a
                className='btn btn-outline-secondary btn-sm dropdown-toggle'
                href='#'
                role='button'
                data-toggle='dropdown'
                data-boundary='window'
                aria-haspopup='true'
                aria-expanded='false'
            >
                Select Tool
            </a>
            <div className='dropdown-menu'>
                {dbLink.blastableDatabases.map((blast) => (
                    <a className='dropdown-item' href={blast.urlPrefix + dbLink.accessionNumber} key={blast.zdbID}>
                        {blast.displayName}
                    </a>
                ))}
            </div>
        </div>
    );
}

BlastDropDown.propTypes = {
    dbLink: PropTypes.object,
};

export default BlastDropDown;
