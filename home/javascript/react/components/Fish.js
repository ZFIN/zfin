import React from 'react';
import PropTypes from 'prop-types';

const Fish = ({entity}) => {
    return (
        <span className='text-break'>
            <a
                className='text-break'
                href={`/${entity.zdbID}`}
                dangerouslySetInnerHTML={{__html: entity.displayName}}
            />
            <a
                className='popup-link data-popup-link'
                href={`/action/fish/fish-detail-popup/${entity.zdbID}`}
            />
        </span>
    );
}

Fish.propTypes = {
    entity: PropTypes.shape({
        zdbID: PropTypes.string, displayName: PropTypes.string,
    }),
};

export default Fish;