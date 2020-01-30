import React from 'react';
import PropTypes from 'prop-types';

const BlastDropDown = ({dbLink}) => {
    return (
        <div className='dropdown'>
            <a
                className='btn btn-info btn-sm dropdown-toggle'
                href='#'
                role='button'
                id='dropdownMenuLink'
                data-toggle='dropdown'
                aria-haspopup='true'
                aria-expanded='false'
            >
                Select Tool
            </a>
            <div className='dropdown-menu' aria-labelledby='dropdownMenuLink'>
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
