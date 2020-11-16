import React from 'react';
import PropTypes from 'prop-types';
import CommaSeparatedList from '../CommaSeparatedList';
import EntityAbbreviation from './EntityAbbreviation';
import EntityLink from './EntityLink';
import {entityType} from '../../utils/types';

const EntityList = ({entities, focusEntityId}) => (
    <CommaSeparatedList>
        {entities.map(entity => {
            if (focusEntityId && entity.zdbID === focusEntityId) {
                return <EntityAbbreviation key={entity.zdbID} entity={entity} />;
            } else {
                return <EntityLink key={entity.zdbID} entity={entity} />
            }
        })}
    </CommaSeparatedList>
);

EntityList.propTypes = {
    entities: PropTypes.arrayOf(entityType),
    focusEntityId: PropTypes.string,
};

export default EntityList;
