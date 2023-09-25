import React, {useState} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import FormGroup from '../components/form/FormGroup';
import NoData from '../components/NoData';
import AddEditDeleteModal from '../components/AddEditDeleteModal';
import useAddEditDeleteForm from '../hooks/useAddEditDeleteForm';
import AddEditList from '../components/AddEditList';
import InputField from '../components/form/InputField';
import PublicationInput from '../components/form/PublicationInput';
import Modal from '../components/Modal';
import http from '../utils/http';
import DEFAULT_PUB_SUGGESTIONS from '../constants/AntibodyPublications';

const AntibodyEditAliases = ({
    antibodyId,
}) => {

    const [modalInformation, setModalInformation] = useState(null);
    const [modalError, setModalError] = useState(null);

    const {
        value: aliases,
        setValue: setAliases,
    } = useFetch(`/action/api/antibody/${antibodyId}/aliases`, {defaultValue: []});

    const {
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/api/antibody/${antibodyId}/aliases`,
        onSuccess: () => setModalInformation(null),
        items: aliases,
        setItems: setAliases,
        defaultValues: modalInformation,
    });

    const handleDeleteClick = async (event, alias) => {
        event.preventDefault();

        try {
            await http.delete(`/action/api/antibody/${alias.publicationZdbID}/${alias.zdbID}`);
            setAliases(aliases.filter(item => !(item.name === alias.name && item.publicationZdbID === alias.publicationZdbID) ));
        } catch (error) {
            if (error?.responseJSON?.message) {
                setModalError(error.responseJSON.message);
            } else {
                setModalError('Error deleting alias');
            }
        }
    };

    const formatRelationship = (alias) => {
        if (!alias) {
            return null;
        }
        return (
            <>
                <span>{alias.name}</span>
                {alias.publicationZdbID && <> ({alias.publicationZdbID})</>}
                <a className='show-on-hover px-1' href='#' onClick={(e) => handleDeleteClick(e, alias)}>
                    <i className='fas fa-trash'/>
                </a>
            </>
        );
    };


    if (!aliases) {
        return null;
    }

    const labelClass = 'col-md-3 col-form-label';
    const inputClass = 'col-md-9';

    return (
        <>
            {aliases.length === 0 && <NoData placeholder=' ' />}

            <AddEditList
                items={aliases}
                newItem={{
                    name: '',
                    publicationZdbID: '',
                }}
                setModalItem={setModalInformation}
                itemKeyProp='name'
                formatItem={formatRelationship}
            />

            <AddEditDeleteModal {...modalProps} header='Alias'>
                {values &&
                <>
                    <FormGroup
                        inputClassName={inputClass}
                        labelClassName={labelClass}
                        label='Alias'
                        id='name'
                        field='name'
                        validate={value => {
                            if (!value) {
                                return 'Alias is required';
                            }
                            return false
                        }}
                    />
                    <div className='form-group row'>
                        <label className={labelClass}>Citations</label>
                        <div className={inputClass}>
                            <div className={'d-flex align-items-baseline}'}>
                                <div className='flex-grow-1'>
                                    <InputField
                                        tag={PublicationInput}
                                        field={'publicationZdbID'}
                                        validate={value => {
                                            if (!value) {
                                                return 'A publication ZDB ID is required';
                                            }
                                            return false
                                        }}
                                        defaultPubs={DEFAULT_PUB_SUGGESTIONS}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
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

AntibodyEditAliases.propTypes = {
    antibodyId: PropTypes.string,
};

export default AntibodyEditAliases;
