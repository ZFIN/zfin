import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../data-table';
import {tableStateType} from '../../utils/types';

const GeneExpressionAnnotationDetailTable = (
    {
        geneId,
        includeReporter,
        isDirectlySubmitted,
        selectedEntity,
        setTableState,
        tableState,
    }
) => {
    const columns = [
        {
            label: 'Fish',
            content: ({fish}) => <a href={`/${fish.zdbID}`} dangerouslySetInnerHTML={{__html: fish.displayName}}/>,
            width: '200px',
        },
        {
            label: 'Experiment',
            content: ({experiment}) => experiment.conditions,
            width: '200px',
        },
        {
            label: 'Assay',
            content: ({assay}) => assay.abbreviation,
            width: '200px',
        },
        {
            label: 'Stage',
            content: 'startStage',
            width: '200px',
        },
        {
            label: 'Figure',
            content: ({figure}) => <a href={`/${figure.zdbID}`} dangerouslySetInnerHTML={{__html: figure.label}}/>,
            width: '200px',
        },
        {
            label: 'Publication',
            content: ({publication}) =>
                <a href={`/${publication.zdbID}`} dangerouslySetInnerHTML={{__html: publication.shortAuthorList}}/>,
            width: '200px',
        },
    ];

    const detailTableQuery = {
        supertermId: selectedEntity.superterm.oboID,
        includeReporter: includeReporter,
        onlyDirectlySubmitted: isDirectlySubmitted,
    };
    if (selectedEntity.subterm) {
        detailTableQuery.subtermId = selectedEntity.subterm.oboID;
    }

    return (
        <DataTable
            dataUrl={`/action/api/marker/${geneId}/expression/ribbon-expression-detail?${qs.stringify(detailTableQuery)}`}
            columns={columns}
            rowKey={row => row.id}
            tableState={tableState}
            setTableState={setTableState}
        />
    )
}

GeneExpressionAnnotationDetailTable.propTypes = {
    geneId: PropTypes.string,
    includeReporter: PropTypes.bool,
    isDirectlySubmitted: PropTypes.bool,
    selectedEntity: PropTypes.object,
    setTableState: PropTypes.func,
    tableState: tableStateType,
};

export default GeneExpressionAnnotationDetailTable;
