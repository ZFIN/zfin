import React from 'react';

import {entityType} from '../../utils/types';
import {string} from 'prop-types';

const SpecialEntityLink = ({entity, displayName}) => {
    return (
        <a
            className='text-break'
            href={`/${entity.zdbID}`}
            dangerouslySetInnerHTML={{__html: displayName}}
        />
    );
};

SpecialEntityLink.propTypes = {
    entity: entityType,
    displayName: string.isRequired,
};

export default SpecialEntityLink;
