import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import {useRibbonState, useTableState} from '../utils/effects';
import { DataRibbon } from '../components/ribbon';
import {DEFAULT_TABLE_STATE} from '../components/data-table';
import {
    GeneExpressionAnnotationDetailTable,
    GeneExpressionAnnotationSummaryTable,
    GeneExpressionFigureGallery,
} from '../components/gene-expression';
import Checkbox from '../components/Checkbox';

const GeneExpressionRibbon = ({geneId}) => {
    const [summaryTableState, setSummaryTableState] = useTableState();
    const [detailTableState, setDetailTableState] = useTableState();
    const [selectedRibbonTerm, setSelectedRibbonTerm] = useRibbonState();
    const [selectedTableEntity, setSelectedTableEntity] = useState(null);
    const [includeReporter, setIncludeReporter] = useState(false);
    const [isDirectlySubmitted, setIsDirectlySubmitted] = useState(false);

    const baseUrl = `/action/api/marker/${geneId}/expression/ribbon-summary`;
    const params = {};
    if (includeReporter) {
        params.includeReporter = true;
    }
    if (isDirectlySubmitted) {
        params.onlyDirectlySubmitted = true;
    }
    const ribbonDataUrl = baseUrl + qs.stringify(params, { addQueryPrefix: true });

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

    const handleNoDataLoad = () => {
        setSelectedRibbonTerm(null, null);
        setSelectedTableEntity(null);
    }

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
            <div className='mb-2'>
                <Checkbox checked={includeReporter} id='reporterSelectionCheckbox' onChange={handleReporterSelection}>
                    Include Expression in Reporter Lines
                </Checkbox>
                <Checkbox checked={isDirectlySubmitted} id='directSubmissionCheckbox' onChange={handleDirectSubmissionSelection}>
                    Show only Directly Submitted Expression Data
                </Checkbox>
            </div>

            <DataRibbon
                dataUrl={ribbonDataUrl}
                onNoDataLoad={handleNoDataLoad}
                onRibbonCellClick={handleRibbonCellClick}
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
