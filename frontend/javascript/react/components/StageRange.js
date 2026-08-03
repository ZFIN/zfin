import React from 'react';
import StageLink from './StageLink';
import PropTypes from 'prop-types';

const StageRange = ({start, end}) => {
    const startIsEnd = start.zdbID === end.zdbID;
    return (
        <>
            <StageLink stage={start} />
            {!startIsEnd && ' to '}
            {!startIsEnd && <StageLink stage={end} />}
        </>
    );
}

StageRange.propTypes = {
    start: PropTypes.shape({
        zdbID: PropTypes.string,
        name: PropTypes.string,
    }),
    end: PropTypes.shape({
        zdbID: PropTypes.string,
        name: PropTypes.string,
    }),
};

export default StageRange;
