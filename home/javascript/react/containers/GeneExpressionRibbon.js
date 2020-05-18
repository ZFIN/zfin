import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import {useFetch, useRibbonState} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';

import NoData from '../components/NoData';
import Ribbon, {getSelectedTermQueryParams} from '../components/Ribbon';
import GenericErrorMessage from '../components/GenericErrorMessage';
import DataTable, {DEFAULT_TABLE_STATE} from '../components/data-table';
import AttributionLink from '../components/AttributionLink';
import StageTimeline from '../components/StageTimeline';
import GeneExpressionFigureGallery from './GeneExpressionFigureGallery';
import StageTimelineHeader from '../components/StageTimelineHeader';

const GeneExpressionRibbon = ({geneId}) => {
    const [tableState, setTableState] = useState(DEFAULT_TABLE_STATE);
    const [detailTableState, setDetailTableState] = useState(DEFAULT_TABLE_STATE);
    const [selectedRibbonTerm, setSelectedRibbonTerm] = useRibbonState();
    const [selectedTableEntity, setSelectedTableEntity] = useState(null);
    const [isChecked, setIsChecked] = useState(false);
    const [isDirectlySubmitted, setIsDirectlySubmitted] = useState(false);

    let url = `/action/api/marker/${geneId}/expression/ribbon-summary`;
    let params = {};
    if (isChecked) {
        params.includeReporter = true;
    }
    if (isDirectlySubmitted) {
        params.onlyDirectlySubmitted = true;
    }
    const data = useFetch(url + qs.stringify(params, { addQueryPrefix: true }));

    const handleEntityNameClick = (event, entity) => {
        event.preventDefault();
        setSelectedTableEntity(entity);
    };

    const handleReporterSelection = (event) => {
        setIsChecked(event.target.checked);
    };
    const handleDirectSubmissionSelection = (event) => {
        setIsDirectlySubmitted(event.target.checked);
    };

    const handleRibbonCellClick = (subject, group) => {
        setTableState(DEFAULT_TABLE_STATE);
        setDetailTableState(DEFAULT_TABLE_STATE);
        setSelectedTableEntity(null);
        setSelectedRibbonTerm(subject, group);
    };

    const columnsDetail = [
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

    const columns = [
        {
            label: 'Expression Location',
            content: ({entity}) =>
                <a
                    href='#'
                    onClick={event => handleEntityNameClick(event, entity)}
                    key={entity}
                >
                    {entity.superterm.termName} {entity.subterm && entity.subterm.termName}
                </a>,
            width: '140px',
            filterName: 'termName',
        },
        {
            label: <StageTimelineHeader />,
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

    let selectedTermName = '';
    let selectedTermId = '';
    let selectedSubtermId = '';
    let selectedSupertermId = '';
    let selectedTermIsOther = false;
    if (selectedTableEntity) {
        selectedTermName = selectedTableEntity.superterm.termName
        if (selectedTableEntity.subterm) {
            selectedTermName +=  ' ' + selectedTableEntity.subterm.termName
        }
        selectedSupertermId = selectedTableEntity.superterm.oboID;
        selectedTermId = selectedTableEntity.superterm.oboID;
        if (selectedTableEntity.subterm) {
            selectedSubtermId = selectedTableEntity.subterm.oboID;
        }
    } else if (selectedRibbonTerm) {
        selectedTermName = selectedRibbonTerm.group.label;
        selectedTermIsOther = selectedRibbonTerm.group.type === 'Other';
        if (selectedRibbonTerm.group.type !== 'GlobalAll') {
            selectedTermId = selectedRibbonTerm.group.id;
        }
    }

    const summaryTableQuery = {
        ...getSelectedTermQueryParams(selectedRibbonTerm),
        includeReporter: isChecked,
        onlyDirectlySubmitted: isDirectlySubmitted,
    };

    const detailTableQuery = {
        supertermId: selectedSupertermId,
        subtermId: selectedSubtermId,
        termId: selectedSupertermId,
        includeReporter: isChecked,
        onlyDirectlySubmitted: isDirectlySubmitted,
    };

    return (
        <div>
            <div className='custom-control custom-checkbox'>
                <input
                    type='checkbox'
                    id='reporterSelectionCheckbox'
                    className='custom-control-input'
                    onChange={(event) => handleReporterSelection(event)}
                    checked={isChecked}
                />
                <label className='custom-control-label' htmlFor='reporterSelectionCheckbox'>
                    Include Expression in Reporter Lines
                </label>
            </div>
            <div className='custom-control custom-checkbox'>
                <input
                    type='checkbox'
                    id='directSubmissionCheckbox'
                    className='custom-control-input'
                    onChange={(event) => handleDirectSubmissionSelection(event)}
                    checked={isDirectlySubmitted}
                />
                <label className='custom-control-label' htmlFor='directSubmissionCheckbox'>
                    Show only Directly Submitted Expression Data
                </label>
            </div>

            { data.rejected && <GenericErrorMessage/> }
            { data.pending && <LoadingSpinner/> }
            { !data.pending && data.value && (data.value.subjects[0].nb_annotations === 0 ?
                <NoData/> :
                <Ribbon
                    subjects={data.value.subjects}
                    categories={data.value.categories}
                    itemClick={handleRibbonCellClick}
                    selected={selectedRibbonTerm}
                />
            )}

            {selectedTableEntity &&
            <button className=' btn btn-link btn-sm px-0' onClick={() => setSelectedTableEntity(null)}>
                <i className=' fas fa-chevron-left'/> Back to expression in {selectedRibbonTerm.group.label}
            </button>
            }
            {selectedTermName && <h5>Expression in {selectedTermName}</h5>}

            {(selectedRibbonTerm || selectedTableEntity) &&
            <GeneExpressionFigureGallery
                geneId={geneId}
                includeReporters={isChecked}
                onlyDirectlySubmitted={isDirectlySubmitted}
                selectedTermId={selectedTermId}
                selectedSubtermId={selectedSubtermId}
                selectedSupertermId={selectedSupertermId}
                selectedTermIsOther={selectedTermIsOther}
            />
            }

            {selectedRibbonTerm && !selectedTableEntity &&
            <DataTable
                url={`/action/api/marker/${geneId}/expression/ribbon-detail?${qs.stringify(summaryTableQuery)}`}
                columns={columns}
                rowKey={row => row.entity.superterm.oboID + (row.entity.subterm && ',' + row.entity.subterm.oboID)}
                tableState={tableState}
                onTableStateChange={setTableState}
            />
            }

            {selectedTableEntity &&
            <DataTable
                url={`/action/api/marker/${geneId}/expression/ribbon-expression-detail?${qs.stringify(detailTableQuery)}`}
                columns={columnsDetail}
                rowKey={row => row.id}
                tableState={detailTableState}
                onTableStateChange={setDetailTableState}
            />
            }
        </div>
    );
};

GeneExpressionRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneExpressionRibbon;
