import React, {useState} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import NoData from '../components/NoData';
import AddEditDeleteModal from '../components/AddEditDeleteModal';
import useAddEditDeleteForm from '../hooks/useAddEditDeleteForm';
import AddEditList from '../components/AddEditList';
import PublicationInput from '../components/form/PublicationInput';
import {EntityLink} from '../components/entity';
import FormGroup from '../components/form/FormGroup';

import http from '../utils/http';
import Modal from '../components/Modal';


const MarkerDirectAttributions = ({
    markerId,
}) => {

    const [modalAttribution, setModalAttribution] = useState(null);
    const [modalError, setModalError] = useState(null);
    const isEdit = modalAttribution && !!modalAttribution.zdbID;

    const {
        value: attributions,
        setValue: setAttributions,
    } = useFetch(`/action/api/marker/${markerId}/attributions`, {defaultValue: []});

    const {
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/api/marker/${markerId}/attributions`,
        editUrl: isEdit ? `/action/api/marker/${markerId}/attributions/${modalAttribution.zdbID}` : '',
        deleteUrl: isEdit ? `/action/api/marker/${markerId}/attributions/${modalAttribution.zdbID}` : '',
        onSuccess: () => setModalAttribution(null),
        items: attributions,
        setItems: setAttributions,
        defaultValues: modalAttribution,
    });

    const handleDeleteClick = async (event, attribution) => {
        event.preventDefault();

        try {
            await http.delete(`/action/api/marker/${markerId}/attributions/${attribution.zdbID}`);
            setAttributions(attributions.filter(item => item.zdbID !== attribution.zdbID));
        } catch (error) {
            if (error?.responseJSON?.message) {
                setModalError(error.responseJSON.message);
            } else {
                setModalError('Error deleting attribution');
            }
        }
    };

    const formatAttributions = (attribution) => {
        if (!attribution) {
            return null;
        }
        return (
            <>
                <EntityLink entity={{abbreviation: attribution.zdbID, ...attribution}}/>
                <a className='show-on-hover px-1' href='#' onClick={(e) => handleDeleteClick(e, attribution)}>
                    <i className='fas fa-trash'/>
                </a>
            </>
        );
    };

    if (!attributions) {
        return null;
    }

    const labelClass = 'col-md-3 col-form-label';
    const inputClass = 'col-md-9';

    return (
        <>
            {attributions.length === 0 && <NoData placeholder=' ' />}

            <AddEditList
                items={attributions}
                newItem={{
                    'zdbID': '',
                }}
                setModalItem={setModalAttribution}
                itemKeyProp='zdbID'
                formatItem={formatAttributions}
            />

            <AddEditDeleteModal {...modalProps} header='Direct Attribution'>
                {values &&
                <>
                    <FormGroup
                        inputClassName={inputClass}
                        labelClassName={labelClass}
                        label='Attribution'
                        id='attribution'
                        field='zdbID'
                        tag={PublicationInput}
                        validate={value => {
                            if (!value) {
                                return 'A publication ZDB ID is required';
                            }
                            return false
                        }}
                    />
                </>
                }
            </AddEditDeleteModal>

            <Modal open={modalError !== null}>
                <div className='popup-header'>Error</div>
                <div className='popup-body show-overflow'>
                    {modalError && <div className='error'>{modalError}</div>}
                    <button className='btn btn-outline-secondary float-right' onClick={() => setModalError(null)} type='button'>Close</button>
                </div>
            </Modal>
        </>
    );
};

MarkerDirectAttributions.propTypes = {
    markerId: PropTypes.string,
};

export default MarkerDirectAttributions;
