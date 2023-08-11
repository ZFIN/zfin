import React from 'react';

import {entityType} from '../../utils/types';

const AntibodyLink = ({antibody}) => {
    return (
        antibody && <a href={`/${antibody.zdbID}`}>
            {antibody.abbreviation}
        </a>
    );
};

AntibodyLink.propTypes = {
    antibody: entityType,
};

export default AntibodyLink;
