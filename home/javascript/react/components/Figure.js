import React from 'react';
import PropTypes from 'prop-types';

const Figure = ({figure}) => {
    return (
        <div key={figure.zdbID}>
            <a
                href={'/' + figure.zdbID}
                dangerouslySetInnerHTML={{__html: figure.label}}
            />
        </div>
    )
};

Figure.propTypes = {
    figure: PropTypes.shape({
        zdbID: PropTypes.string,
        label: PropTypes.string,
    })
};

export default Figure;
