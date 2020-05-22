import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import {useFetch, useRibbonState, useTableState} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';

import NoData from '../components/NoData';
import Ribbon from '../components/Ribbon';
import GenericErrorMessage from '../components/GenericErrorMessage';
import {DEFAULT_TABLE_STATE} from '../components/data-table';
import {
    GeneExpressionAnnotationDetailTable,
    GeneExpressionAnnotationSummaryTable,
    GeneExpressionFigureGallery,
} from '../components/gene-expression';

const GeneExpressionRibbon = ({geneId}) => {
    const [summaryTableState, setSummaryTableState] = useTableState();
    const [detailTableState, setDetailTableState] = useTableState();
    const [selectedRibbonTerm, setSelectedRibbonTerm] = useRibbonState();
    const [selectedTableEntity, setSelectedTableEntity] = useState(null);
    const [includeReporter, setIncludeReporter] = useState(false);
    const [isDirectlySubmitted, setIsDirectlySubmitted] = useState(false);

    let url = `/action/api/marker/${geneId}/expression/ribbon-summary`;
    let params = {};
    if (includeReporter) {
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
        setIncludeReporter(event.target.checked);
    };

    const handleDirectSubmissionSelection = (event) => {
        setIsDirectlySubmitted(event.target.checked);
    };

    const handleRibbonCellClick = (subject, group) => {
        setSummaryTableState(DEFAULT_TABLE_STATE);
        setDetailTableState(DEFAULT_TABLE_STATE);
        setSelectedTableEntity(null);
        setSelectedRibbonTerm(subject, group);
    };

    let selectedTermName = '';
    if (selectedTableEntity) {
        selectedTermName = selectedTableEntity.superterm.termName
        if (selectedTableEntity.subterm) {
            selectedTermName +=  ' ' + selectedTableEntity.subterm.termName
        }
    } else if (selectedRibbonTerm) {
        selectedTermName = selectedRibbonTerm.group.label;
    }

    return (
        <div>
            <div className='custom-control custom-checkbox'>
                <input
                    type='checkbox'
                    id='reporterSelectionCheckbox'
                    className='custom-control-input'
                    onChange={(event) => handleReporterSelection(event)}
                    checked={includeReporter}
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
                    includeReporters={includeReporter}
                    onlyDirectlySubmitted={isDirectlySubmitted}
                    selectedRibbonTerm={selectedRibbonTerm}
                    selectedTableEntity={selectedTableEntity}
                />
            }

            {selectedRibbonTerm && !selectedTableEntity &&
                <GeneExpressionAnnotationSummaryTable
                    geneId={geneId}
                    includeReporter={includeReporter}
                    isDirectlySubmitted={isDirectlySubmitted}
                    onEntityClick={handleEntityNameClick}
                    selectedRibbonTerm={selectedRibbonTerm}
                    setTableState={setSummaryTableState}
                    tableState={summaryTableState}
                />
            }

            {selectedTableEntity &&
                <GeneExpressionAnnotationDetailTable
                    geneId={geneId}
                    includeReporter={includeReporter}
                    isDirectlySubmitted={isDirectlySubmitted}
                    selectedEntity={selectedTableEntity}
                    setTableState={setDetailTableState}
                    tableState={detailTableState}
                />
            }
        </div>
    );
};

GeneExpressionRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneExpressionRibbon;
