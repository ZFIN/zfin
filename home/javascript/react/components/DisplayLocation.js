import React from 'react';
import PropTypes from 'prop-types';

const DisplayLocation = ({entity, hideLink = false}) => {
    if (entity.chromosome) {
        return <span>
            {(entity.chromosome === 'Ambiguous' || entity.chromosome.includes('Zv9')) ? '' : 'Chr: '}
            {entity.chromosome}{' '}
            {!hideLink &&
            <a href={`/action/mapping/detail/${entity.entity.zdbID}`}>
                Details
            </a>}
        </span>;
    } else {
        return <>Unmapped</>
    }
};

DisplayLocation.propTypes = {
    entity: PropTypes.any,
    hideLink: PropTypes.bool,
};

export default DisplayLocation;
