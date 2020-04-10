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

const GeneExpressionRibbon = ({geneId}) => {
    const [tableState, setTableState] = useState(DEFAULT_TABLE_STATE);
    const [selectedRibbonTerm, setSelectedRibbonTerm] = useRibbonState(() => setTableState(DEFAULT_TABLE_STATE));
    const [selectedTableTerm, setSelectedTableTerm] = useState(null);

    const data = useFetch(`/action/api/marker/${geneId}/expression/ribbon-summary`);

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

    const handleRibbonCellClick = (subject, group) => {
        setSelectedTableTerm(null);
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
            content: ({experiment}) => <div>{experiment.conditions}</div>,
            width: '200px',
        },
        {
            label: 'Assay',
            content: ({assay}) => <div>{assay.abbreviation}</div>,
            width: '200px',
        },
        {
            label: 'Terms',
            content: ({entities}) => (<PostComposedEntities entities={entities}/>),
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
            content: ({startStage}) => <div>{startStage}</div>,
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
    ]

    const columns = [
        {
            label: 'Expression Location',
            content: ({term}) => <a href='#' onClick={event => handleTermNameClick(event, term)}>{term.termName}</a>,
            width: '200px',
        },
        {
            label: (
                <div>
                    <div>Stage Observed</div>
                    <ul className='list-unstyled d-flex justify-content-between font-weight-normal'>
                        <li>Zygote</li>
                        <li>Adult</li>
                    </ul>
                </div>
            ),
            content: ({stages}, supplementalData) => (
                <StageTimeline highlightedStages={stages} allStages={supplementalData.stages}/>),
            width: '400px',
        },
        {
            label: 'Citations',
            content: row => (
                <AttributionLink
                    url={`/action/marker/${row.term.oboID}`}
                    publicationCount={row.numberOfPublications}
                    publication={row.publication}
                    multiPubAccessionID={row.term.oboID}
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
            <Ribbon
                subjects={data.value.subjects}
                categories={data.value.categories}
                itemClick={handleRibbonCellClick}
                selected={selectedRibbonTerm}
            />

            {selectedTableTerm &&
            <button className='btn btn-link btn-sm px-0' onClick={() => setSelectedTableTerm(null)}>
                <i className='fas fa-chevron-left'/> Back to expression in {selectedRibbonTerm.group.label}
            </button>
            }
            {selectedTermName && <h5>Expression in {selectedTermName}</h5>}

            {(selectedRibbonTerm || selectedTableTerm) &&
            <GeneExpressionFigureGallery
                geneId={geneId}
                selectedTermId={selectedTermId}
                selectedTermIsOther={selectedTermIsOther}
            />
            }

            {selectedRibbonTerm && !selectedTableTerm &&
            <DataTable
                url={`/action/api/marker/${geneId}/expression/ribbon-detail${getSelectedTermQueryParams(selectedRibbonTerm)}`}
                columns={columns}
                rowKey={row => row.term.zdbID}
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