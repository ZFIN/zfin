import React from 'react';
import PropTypes from 'prop-types';
import CommaSeparatedList from '../CommaSeparatedList';
import PhenotypeStatementLink from './PhenotypeStatementLink';
import {entityIDType} from '../../utils/types';

const PhenotypeStatementList = ({entityList}) => (
    <CommaSeparatedList>
        {entityList.map(entity => {
            return <PhenotypeStatementLink key={entity.id} entity={entity}/>
        })}
    </CommaSeparatedList>
);

PhenotypeStatementList.propTypes = {
    entityList: PropTypes.arrayOf(entityIDType),
};

export default PhenotypeStatementList;
