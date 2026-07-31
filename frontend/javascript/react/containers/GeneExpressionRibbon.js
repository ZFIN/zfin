import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import useRibbonState from '../hooks/useRibbonState';
import useTableState from '../hooks/useTableState';
import { DataRibbon } from '../components/ribbon';
import {
    GeneExpressionAnnotationDetailTable,
    GeneExpressionAnnotationSummaryTable,
    GeneExpressionFigureGallery,
} from '../components/gene-expression';
import Checkbox from '../components/Checkbox';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

const GeneExpressionRibbon = ({geneId}) => {
    const [summaryTableState, setSummaryTableState] = useTableState();
    const [detailTableState, setDetailTableState] = useTableState();
    const [selectedRibbonTerm, setSelectedRibbonTerm] = useRibbonState();
    const [selectedTableEntity, setSelectedTableEntity] = useState(null);
    const [includeReporter, setIncludeReporter] = useState(false);
    const [onlyInSitu, setOnlyInSitu] = useState(false);

    const baseUrl = `/action/api/marker/${geneId}/expression/ribbon-summary`;
    const params = {};
    if (includeReporter) {
        params.includeReporter = true;
    }
    if (onlyInSitu) {
        params.onlyInSitu = true;
    }
    const ribbonDataUrl = baseUrl + qs.stringify(params, { addQueryPrefix: true });

    const handleEntityNameClick = (event, entity) => {
        event.preventDefault();
        setSelectedTableEntity(entity);
    };

    const handleReporterSelection = (event) => {
        setIncludeReporter(event.target.checked);
    };

    const goToFirstPage = produce(state => {
        state.page = 1;
    });

    const handleRibbonCellClick = (subject, group) => {
        setSummaryTableState(goToFirstPage);
        setDetailTableState(goToFirstPage);
        setSelectedTableEntity(null);
        setSelectedRibbonTerm(subject, group);
    };

    const handleNoDataLoad = () => {
        setSelectedRibbonTerm(null, null);
        setSelectedTableEntity(null);
    };

    let selectedTermName = '';
    let headerText = '';
    if (selectedTableEntity) {
        selectedTermName = selectedTableEntity.superterm.termName;
        if (selectedTableEntity.subterm) {
            selectedTermName +=  ' ' + selectedTableEntity.subterm.termName
        }
        headerText += 'Expression in ' +  selectedTermName
    } else if (selectedRibbonTerm) {
        selectedTermName = selectedRibbonTerm.group.label;
        if (selectedRibbonTerm.group.id.indexOf('ZFS') === -1) {
            headerText += 'Expression in ' + selectedTermName

        }
        else {
            headerText += 'Expression at ' + selectedTermName + '  stage'
        }
    }

    return (
        <div className='gene-expression-ribbon'>
            <div className='mb-2'>
                <Checkbox checked={includeReporter} id='reporterSelectionCheckbox' onChange={handleReporterSelection}>
                    Include expression in reporter lines
                </Checkbox>
                <Checkbox checked={onlyInSitu} id='directSubmissionCheckbox' onChange={e => setOnlyInSitu(e.target.checked)}>
                    Show in situs only
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

            {selectedTermName && <h5>{headerText} </h5>}

            {(selectedRibbonTerm || selectedTableEntity) &&
                <GeneExpressionFigureGallery
                    geneId={geneId}
                    includeReporters={includeReporter}
                    onlyInSitu={onlyInSitu}
                    selectedRibbonTerm={selectedRibbonTerm}
                    selectedTableEntity={selectedTableEntity}
                />
            }

            {selectedRibbonTerm && !selectedTableEntity &&
                <GeneExpressionAnnotationSummaryTable
                    geneId={geneId}
                    includeReporter={includeReporter}
                    onlyInSitu={onlyInSitu}
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
                    onlyInSitu={onlyInSitu}
                    selectedEntity={selectedTableEntity}
                    selectedRibbonTerm={selectedRibbonTerm}
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
