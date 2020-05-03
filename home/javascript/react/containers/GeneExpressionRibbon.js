import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {useFetch, useRibbonState} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';

import NoData from '../components/NoData';
import Ribbon, {getSelectedTermQueryParams} from '../components/Ribbon';
import GenericErrorMessage from '../components/GenericErrorMessage';
import DataTable, {DEFAULT_TABLE_STATE} from '../components/DataTable';
import AttributionLink from '../components/AttributionLink';
import StageTimeline from '../components/StageTimeline';
import GeneExpressionFigureGallery from './GeneExpressionFigureGallery';
import PostComposedEntities from '../components/PostComposedEntities';
import StageTimelineHeader from '../components/StageTimelineHeader';

const GeneExpressionRibbon = ({geneId}) => {
    const [tableState, setTableState] = useState(DEFAULT_TABLE_STATE);
    const [selectedRibbonTerm, setSelectedRibbonTerm] = useRibbonState(() => setTableState(DEFAULT_TABLE_STATE));
    const [selectedTableEntity, setSelectedTableEntity] = useState(null);
    const [isChecked, setIsChecked] = useState(false);
    const [isDirectlySubmitted, setIsDirectlySubmitted] = useState(false);
    const [filteredTerm, setFilteredTerm] = useState('');


    let url = `/action/api/marker/${geneId}/expression/ribbon-summary`;

    if (isChecked) {
        url += '?includeReporter=true';
    }
    if (isDirectlySubmitted) {
        url += '?onlyDirectlySubmitted=true';
    }

    const data = useFetch(url);

    if (data.rejected) {
        return <GenericErrorMessage/>;
    }

    if (data.pending) {
        return <LoadingSpinner/>;
    }

    if (!data.value) {
        return null;
    }

    if (data.value.subjects[0].nb_annotations === 0) {
        return <NoData/>
    }

    const handleEntityNameClick = (event, entity) => {
        event.preventDefault();
        setSelectedTableEntity(entity);
    };

    const handleFilterChange = (event) => {
        event.preventDefault();
        setFilteredTerm(event.target.value);
    };

    const handleFilter = (event, term) => {
        event.preventDefault();
        setFilteredTerm(term);
    };

    const handleReporterSelection = (event) => {
        setIsChecked(event.target.checked);
    };
    const handleDirectSubmissionSelection = (event) => {
        setIsDirectlySubmitted(event.target.checked);
    };

    const handleRibbonCellClick = (subject, group) => {
        setSelectedTableEntity(null);
        setSelectedRibbonTerm(subject, group);
        setFilteredTerm('');
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
            label: 'Expressed Location',
            content: ({entities}) => <PostComposedEntities entities={entities} />,
            width: '200px',
        },
        /*
                {
                    label: 'Antibody',
                    content: ({antibody}) => <div>{antibody}</div>,
                    width: '200px',
                },
        */
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
            label: (
                <div>
                    <div>Expression Location</div>
                    <form className='form-inline'>
                        <label className='sr-only' htmlFor='filterInputField'>Name</label>
                        <input
                            type='text'
                            className='form-control mb-2 mr-sm-2'
                            id='filterTermName'
                            size='15'
                            value={filteredTerm}
                            onChange={event => handleFilterChange(event)}
                        />
                        <button
                            type='button'
                            className='btn btn-secondary btn-sm mb-2'
                            onClick={event => handleFilter(event, '')}
                        >
                            Clear
                        </button>
                    </form>
                </div>
            ),
            key: 'locations',
            content: ({entity}) =>
                <a
                    href='#'
                    onClick={event => handleEntityNameClick(event, filteredTerm)}
                    key={entity}
                >
                    {entity.superterm.termName} {entity.subterm && entity.subterm.termName}
                </a>,
            width: '140px',
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
    let selectedTermIsOther = false;
    if (selectedTableEntity) {
        selectedTermName = selectedTableEntity.superterm.termName + (selectedTableEntity.subterm && ' ' + selectedTableEntity.subterm.termName);
        //todo: need to handle subterm also
        selectedTermId = selectedTableEntity.superterm.oboID;
    } else if (selectedRibbonTerm) {
        selectedTermName = selectedRibbonTerm.group.label;
        selectedTermIsOther = selectedRibbonTerm.group.type === 'Other';
        if (selectedRibbonTerm.group.type !== 'GlobalAll') {
            selectedTermId = selectedRibbonTerm.group.id;
        }
    }

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

            <Ribbon
                subjects={data.value.subjects}
                categories={data.value.categories}
                itemClick={handleRibbonCellClick}
                selected={selectedRibbonTerm}
            />

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
                selectedTermIsOther={selectedTermIsOther}
            />
            }

            {selectedRibbonTerm && !selectedTableEntity &&
            <DataTable
                url={`/action/api/marker/${geneId}/expression/ribbon-detail${getSelectedTermQueryParams(selectedRibbonTerm)}&includeReporter=${isChecked}&onlyDirectlySubmitted=${isDirectlySubmitted}&filter.termName=${filteredTerm}`}
                columns={columns}
                rowKey={row => row.entity.superterm.oboID + (row.entity.subterm && ',' + row.entity.subterm.oboID)}
                showEmptyTable={filteredTerm}
                tableState={tableState}
                onTableStateChange={setTableState}
            />
            }

            {selectedTableEntity &&
            <DataTable
                url={`/action/api/marker/${geneId}/expression/ribbon-expression-detail?termId=${selectedTermId}`}
                columns={columnsDetail}
                rowKey={row => row.id}
                tableState={tableState}
                onTableStateChange={setTableState}
            />
            }
        </div>
    );
};

GeneExpressionRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneExpressionRibbon;
