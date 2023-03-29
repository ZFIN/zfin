import React from 'react';
import PropTypes, {bool} from 'prop-types';
import SpecialEntityLink from './SpecialEntityLink';
import {entityType} from '../../utils/types';
import {EntityAbbreviation} from './index';

const EntityGroupList = ({entities, showLink, stringOnly}) => (
    <ul className='unordered'>
        {entities && entities.map(entity => {
            if (showLink) {
                return <>
                    <li><SpecialEntityLink key={entity.zdbID} entity={entity} displayName={entity.name}/></li>
                </>
            } else {
                if (!stringOnly) {
                    return <>
                        <li><EntityAbbreviation key={entity.zdbID} entity={entity}/></li>
                    </>
                } else {
                    return <li key={entity.zdbID}>{entity}</li>
                }
            }
        })}
    </ul>
);

EntityGroupList.propTypes = {
    entities: PropTypes.arrayOf(entityType),
    showLink: bool,
    stringOnly: bool,
};

export default EntityGroupList;
