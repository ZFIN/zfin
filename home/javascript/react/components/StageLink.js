import React from 'react';
import PropTypes from 'prop-types';

const URL = '/zf_info/zfbook/stages/index.html#';

const StageLink = ({stage}) => {
    const idx = stage.name.indexOf(':');
    if (idx < 0) {
        return <a href={URL + stage.name}>{stage.name}</a>;
    }

    const period = stage.name.substring(0, idx);
    const name = stage.name.substring(idx + 1);
    return (
        <a href={URL + period}>
            {name}
        </a>
    );
}

StageLink.propTypes = {
    stage: PropTypes.shape({
        name: PropTypes.string,
    }),
};

export default StageLink;
