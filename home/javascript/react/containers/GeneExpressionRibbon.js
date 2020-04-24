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
    const [selectedTableTerm, setSelectedTableTerm] = useState(null);
    const [isChecked, setIsChecked] = useState(false);
    const [filteredTerm, setFilteredTerm] = useState('');


    let url = `/action/api/marker/${geneId}/expression/ribbon-summary`;

    if (isChecked) {
        url += '?includeReporter=true';
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

    const handleTermNameClick = (event, term) => {
        event.preventDefault();
        setSelectedTableTerm(term);
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

    const handleRibbonCellClick = (subject, group) => {
        setSelectedTableTerm(null);
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
            content: ({term}) => (
                <a
                    href='#'
                    onClick={event => handleTermNameClick(event, term)}
                    key={term}
                >
                    {term.termName}
                </a>
            ),
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
                    url={`/action/marker/${row.term.oboID}`}
                    publicationCount={row.numberOfPublications}
                    publication={row.publication}
                    multiPubAccessionID={row.term.oboID}
                    multiPubs={row.ribbonPubs}
                />
            ),
            width: '120px',
        },
    ];

    let selectedTermName = '';
    let selectedTermId = '';
    let selectedTermIsOther = false;
    if (selectedTableTerm) {
        selectedTermName = selectedTableTerm.termName;
        selectedTermId = selectedTableTerm.oboID;
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

            <Ribbon
                subjects={data.value.subjects}
                categories={data.value.categories}
                itemClick={handleRibbonCellClick}
                selected={selectedRibbonTerm}
            />

            {selectedTableTerm &&
            <button className=' btn btn-link btn-sm px-0' onClick={() => setSelectedTableTerm(null)}>
                <i className=' fas fa-chevron-left'/> Back to expression in {selectedRibbonTerm.group.label}
            </button>
            }
            {selectedTermName && <h5>Expression in {selectedTermName}</h5>}

            {(selectedRibbonTerm || selectedTableTerm) &&
            <GeneExpressionFigureGallery
                geneId={geneId}
                includeReporters={isChecked}
                selectedTermId={selectedTermId}
                selectedTermIsOther={selectedTermIsOther}
            />
            }

            {selectedRibbonTerm && !selectedTableTerm &&
            <DataTable
                url={`/action/api/marker/${geneId}/expression/ribbon-detail${getSelectedTermQueryParams(selectedRibbonTerm)}&includeReporter=${isChecked}&filter.termName=${filteredTerm}`}
                columns={columns}
                rowKey={row => row.term.zdbID}
                showEmptyTable={filteredTerm}
                tableState={tableState}
                onTableStateChange={setTableState}
            />
            }

            {selectedTableTerm &&
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
