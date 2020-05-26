import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../data-table';
import StageTimelineHeader from '../StageTimelineHeader';
import StageTimeline from '../StageTimeline';
import AttributionLink from '../AttributionLink';
import {getSelectedTermQueryParams} from '../Ribbon';
import {tableStateType} from '../../utils/types';

const PhenotypeAnnotationSummaryTable = ({geneId, onEntityClick, selectedRibbonTerm, setTableState, tableState}) => {
    const columns = [
        {
            label: 'Phenotype',
            key: 'locations',
            content: ({phenotype, phenotypeIDs}) =>
                <a
                    href='#'
                    onClick={event => onEntityClick(event, phenotypeIDs, phenotype)}
                    dangerouslySetInnerHTML={{__html: phenotype}}
                />,
            width: '140px',
            filterName: 'termName',
        },
        {
            label: <StageTimelineHeader/>,
            key: 'stages',
            content: ({stages}, supplementalData) => (
                <StageTimeline highlightedStages={stages} allStages={supplementalData.stages}/>
            ),
            width: '300px',
        },
        {
            label: 'Citations',
            content: row => (
                <AttributionLink
                    url={`/action/marker/${geneId}`}
                    publicationCount={row.numberOfPublications}
                    publication={row.publication}
                    multiPubAccessionID={geneId}
                    multiPubs={row.ribbonPubs}
                />
            ),
            width: '120px',
        },
    ];

    const summaryTableQuery = getSelectedTermQueryParams(selectedRibbonTerm);

    return (
        <DataTable
            dataUrl={`/action/api/marker/${geneId}/phenotype/summary?${qs.stringify(summaryTableQuery)}`}
            columns={columns}
            rowKey={row => row.phenotype}
            tableState={tableState}
            setTableState={setTableState}
        />
    )
};

PhenotypeAnnotationSummaryTable.propTypes = {
    geneId: PropTypes.string,
    onEntityClick: PropTypes.func,
    selectedRibbonTerm: PropTypes.object,
    setTableState: PropTypes.func,
    tableState: tableStateType,
};

export default PhenotypeAnnotationSummaryTable;
