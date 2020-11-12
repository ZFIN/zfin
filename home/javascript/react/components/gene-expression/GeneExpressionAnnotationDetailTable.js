import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../data-table';
import {tableStateType} from '../../utils/types';
import StageRange from '../StageRange';
import PublicationCitationLink from '../PublicationCitationLink';

const GeneExpressionAnnotationDetailTable = (
    {
        geneId,
        includeReporter,
        onlyInSitu,
        selectedEntity,
        selectedRibbonTerm,
        setTableState,
        tableState,
    }
) => {
    const columns = [
        {
            label: 'Fish',
            content: ({fish}) => <a href={`/${fish.zdbID}`} dangerouslySetInnerHTML={{__html: fish.displayName}}/>,
            width: '150px',
        },
        {
            label: 'Experiment',
            content: ({experiment}) => experiment.conditions,
            width: '150px',
        },
        {
            label: 'Assay',
            content: ({assay}) => assay.abbreviation,
            width: '75px',
        },
        {
            label: 'Stage',
            content: ({endStage, startStage}) => <StageRange start={startStage} end={endStage} />,
            width: '200px',
        },
        {
            label: 'Figure',
            content: ({figure}) => <a href={`/${figure.zdbID}`} dangerouslySetInnerHTML={{__html: figure.label}}/>,
            width: '100px',
        },
        {
            label: 'Publication',
            content: ({publication}) => <PublicationCitationLink publication={publication} />,
            width: '200px',
        },
    ];

    const detailTableQuery = {
        supertermId: selectedEntity.superterm.oboID,
        includeReporter: includeReporter,
        onlyInSitu: onlyInSitu,
    };
    if (selectedRibbonTerm.group.id !== 'all') {
        detailTableQuery.ribbonTermId = selectedRibbonTerm.group.id;
    }
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
    onlyInSitu: PropTypes.bool,
    selectedEntity: PropTypes.object,
    selectedRibbonTerm: PropTypes.object,
    setTableState: PropTypes.func,
    tableState: tableStateType,
};

export default GeneExpressionAnnotationDetailTable;
