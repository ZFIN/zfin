import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../data-table';
import {tableStateType} from '../../utils/types';
import StageTimelineHeader from '../StageTimelineHeader';
import StageTimeline from '../StageTimeline';
import AttributionLink from '../AttributionLink';
import {getSelectedTermQueryParams} from '../ribbon';

const GeneExpressionAnnotationSummaryTable = (
    {
        geneId,
        includeReporter,
        onlyInSitu,
        onEntityClick,
        selectedRibbonTerm,
        setTableState,
        tableState,
    }
) => {
    const columns = [
        {
            label: 'Expression Location',
            content: ({entity}) =>
                <a
                    href='#'
                    onClick={event => onEntityClick(event, entity)}
                    key={entity}
                >
                    {entity.superterm.termName} {entity.subterm && entity.subterm.termName}
                </a>,
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

    const query = {
        ...getSelectedTermQueryParams(selectedRibbonTerm),
        includeReporter: includeReporter,
        onlyInSitu: onlyInSitu,
    };

    return (
        <div>
            <DataTable
                dataUrl={`/action/api/marker/${geneId}/expression/ribbon-detail?${qs.stringify(query)}`}
                columns={columns}
                rowKey={row => row.entity.superterm.oboID + (row.entity.subterm && ',' + row.entity.subterm.oboID)}
                tableState={tableState}
                setTableState={setTableState}
            />
            <small className='text-muted'>
                Click a location to see annotation details
            </small>

        </div>
    );
};

GeneExpressionAnnotationSummaryTable.propTypes = {
    geneId: PropTypes.string,
    includeReporter: PropTypes.bool,
    onlyInSitu: PropTypes.bool,
    onEntityClick: PropTypes.func,
    selectedRibbonTerm: PropTypes.object,
    setTableState: PropTypes.func,
    tableState: tableStateType,
};

export default GeneExpressionAnnotationSummaryTable;