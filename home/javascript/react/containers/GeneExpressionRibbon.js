import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {useFetch, useRibbonState} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';

import NoData from '../components/NoData';
import Ribbon, { getSelectedTermQueryParams } from '../components/Ribbon';
import GenericErrorMessage from '../components/GenericErrorMessage';
import DataTable, {DEFAULT_TABLE_STATE} from '../components/DataTable';
import AttributionLink from '../components/AttributionLink';
import StagePresentation from '../components/StagePresentation';
import GeneExpressionFigureGallery from './GeneExpressionFigureGallery';

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

    const columns = [
        {
            label: 'Expression Location',
            content: ({term}) => <a href='#' onClick={event => handleTermNameClick(event, term)}>{term.name}</a>,
            width: '250px',
        },
        {
            label: 'Stage Observed',
            content: row => (<StagePresentation stages={row.stageHistogram}/>),
            subHeader: 'cleavage blastula gastrula segmentation pharyngula hatching larva juvenile adult',
            width: '300',
        },
        {
            label: 'Publications',
            content: row => (<AttributionLink
                accession={null}
                url={`/action/marker/${row.term.oboID}`}
                publicationCount={row.numberOfPublications}
                publication={row.publication}
            />),
            width: '450px',
        },
    ];

    let selectedTermName = '';
    let selectedTermId = '';
    let selectedTermIsOther = false;
    if (selectedTableTerm) {
        selectedTermName = selectedTableTerm.name;
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
                    <i className='fas fa-chevron-left' /> Back to expression in {selectedRibbonTerm.group.label}
                </button>
            }
            {selectedTermName && <h5>Expression in {selectedTermName}</h5>}

            {selectedTermId &&
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
                    rowKey='rowKey'
                    tableState={tableState}
                    onTableStateChange={setTableState}
                />
            }

            {selectedTableTerm &&
                <div><i>ANNOTATION TABLE FOR {selectedTermName} HERE</i></div>
            }
        </div>
    );
};

GeneExpressionRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneExpressionRibbon;