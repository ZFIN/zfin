import React, {useState} from 'react';
import PropTypes from 'prop-types';
import EditNomenclatureModal from '../components/EditNomenclatureModal';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';


const NomenclatureEdit = ({markerId, hasRoot}) => {
    const [showAllEvents, setShowAllEvents] = useState(false);
    const rootAccess = !!hasRoot;
    const [enableEditing, setEnableEditing] = useState(false);
    const [nomenclatureToEdit, setNomenclatureToEdit] = useState(null);

    const {
        value: markerHistory,
        pending: markerHistoryPending,
        error: markerHistoryError,
        setValue: setMarkerHistory,
    } = useFetch( '/action/api/nomenclature/history/' + markerId);

    const {
        value: reasons,
        pending: reasonsPending,
        error: reasonsError,
    } = useFetch('/action/api/nomenclature/reasons');

    const [firstLoadComplete, setFirstLoadComplete] = useState(false);

    if (!firstLoadComplete) {
        if (reasonsPending || markerHistoryPending) {
            return <LoadingSpinner/>;
        }
        if (!markerHistory || !reasons) {
            return null;
        }
        setFirstLoadComplete(true);
    }

    function toggleShowAllEvents(event) {
        event.preventDefault();
        setShowAllEvents(!showAllEvents);
    }

    function handleEnableEditingClick(event) {
        event.preventDefault();
        setEnableEditing(!enableEditing);
    }

    function handleEditClick(event, history) {
        event.preventDefault();
        setNomenclatureToEdit({...history});
    }

    function handleEditNomenclature(nomenclature) {

        const updatedHistory = markerHistory.map(h => {
            if (h.zdbID === nomenclature.zdbID) {
                return nomenclature;
            }
            return h;
        });
        setMarkerHistory(updatedHistory);
    }

    return <>

        <table className='data_manager'>
            <tbody>
                <tr><td><b>ZFIN ID:</b> {markerId}</td>
                    {rootAccess && <td><a href='#' onClick={handleEnableEditingClick} className='root'>Edit</a></td>}
                    <td><a href={'/action/updates/' + markerId}>Last Update: { markerHistory.length > 0 ? markerHistory[markerHistory.length - 1].date : '' }</a></td>
                </tr>
            </tbody>
        </table>

        {(markerHistoryError || reasonsError) && <div className='alert alert-danger'>Error loading history</div>}

        <div className='summaryTitle'>Nomenclature History</div>

        {rootAccess &&
            (showAllEvents ?
                <span id='showReducedEventsToggle'><a href='#' onClick={toggleShowAllEvents}>Hide Naming Events</a></span> :
                <span id='showAllEventsToggle'><a href='#' onClick={toggleShowAllEvents}>Show All Events</a></span>)
        }

        <table className='summary sortable'>
            <thead>
                <tr>
                    {enableEditing && <th>Edit</th>}
                    <th>New Value</th>
                    <th>Event</th>
                    <th>Old Value</th>
                    <th>Date</th>
                    <th>Reason</th>
                    <th>Comments</th>
                </tr>
            </thead>
            <tbody>

                {markerHistory.filter(h => showAllEvents || h.eventName !== 'renamed').map((history, index) => (
                    <tr key={history.zdbID} id={'all_' + index}>
                        {enableEditing &&
                        <td>
                            <span><a onClick={(e) => {handleEditClick(e, history)}} href='#'>Edit</a></span>
                        </td>}
                        <td><span className='genedom'>{history.newValue}</span></td>
                        <td>{history.eventDisplay}</td>
                        <td>
                            <span className='genedom'>{history.oldSymbol}</span>
                        </td>
                        <td>
                            {history.date}
                        </td>
                        <td>{history.reason}
                            {history.attributions && history.attributions.length === 1 &&
                                <> {' ('}<a href={'/' + history.firstPublication}>1</a>{')'}</>
                            }
                            {history.attributions && history.attributions.length > 1 &&
                                <> {' ('}<a href={'/action/publication/list/' + history.zdbID}>{history.attributions.length}</a>{')'}</>
                            }
                        </td>
                        <td>{history.comments}</td>
                    </tr>
                ))}

            </tbody>
        </table>
        <EditNomenclatureModal
            nomenclature={nomenclatureToEdit}
            reasons={reasons.reasons}
            onAttributionAdded={handleEditNomenclature}
            onAttributionDeleted={handleEditNomenclature}
            onEdit={handleEditNomenclature}
        />
    </>;
};

NomenclatureEdit.propTypes = {
    markerId: PropTypes.string,
    hasRoot: PropTypes.string,
}

export default NomenclatureEdit;
