import React from 'react';

import EntityAbbreviation from './EntityAbbreviation';
import {entityType} from '../../utils/types';

const EntityLink = ({entity}) => {
    return (
        <a href={`/${entity.zdbID}`}>
            <EntityAbbreviation entity={entity} />
        </a>
    );
};

EntityLink.propTypes = {
    entity: entityType,
};

export default EntityLink;
