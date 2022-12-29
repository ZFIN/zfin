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
// import {EntityLink} from '../components/entity';

const AntibodyEditAliases = ({
    antibodyId,
}) => {

    const [modalInformation, setModalInformation] = useState(null);
    const isEdit = modalInformation && !!modalInformation.zdbID;

    const {
        value: aliases,
        setValue: setAliases,
    } = useFetch(`/action/api/antibody/${antibodyId}/aliases`, {defaultValue: []});

    const {
        pushFieldValue,
        removeFieldValue,
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/api/antibody/${antibodyId}/aliases`,
        editUrl: isEdit ? `/action/api/antibody/${antibodyId}/aliases/${modalInformation.markerRelationshipZdbId}` : '',
        deleteUrl: isEdit ? `/action/api/antibody/${antibodyId}/aliases/${modalInformation.markerRelationshipZdbId}` : '',
        onSuccess: () => setModalInformation(null),
        items: aliases,
        setItems: setAliases,
        defaultValues: modalInformation,
    });

    const formatRelationship = (alias, editLink) => {
        if (!alias) {
            return null;
        }
        return (
            <>
                <span>TODO: put alias here</span>
                {/*<EntityLink entity={alias}/>*/}
                {/*{alias.numberOfPublications > 0 && <> ({alias.numberOfPublications})</>}*/}
                {editLink}
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
                    abbreviation: '',
                    attributionZdbIDs: [''],
                }}
                setModalItem={setModalInformation}
                itemKeyProp='zdbID'
                formatItem={formatRelationship}
            />

            <AddEditDeleteModal {...modalProps} header='Antigen Gene'>
                {values &&
                <>
                    <FormGroup
                        inputClassName={inputClass}
                        labelClassName={labelClass}
                        label='Alias'
                        id='alias'
                        field='alias'
                    />
                    <div className='form-group row'>
                        <label className={labelClass}>Citations</label>
                        <div className={inputClass}>
                            {
                                values.attributionZdbIDs.map((reference, idx) => (
                                    <div key={idx} className={`d-flex align-items-baseline ${idx > 0 ? 'mt-2' : ''}`}>
                                        <div className='flex-grow-1'>
                                            <InputField
                                                tag={PublicationInput}
                                                field={`attributionZdbIDs.${idx}`}
                                                validate={value => {
                                                    if (!value) {
                                                        return 'A publication ZDB ID is required';
                                                    }
                                                    return false
                                                }}
                                            />
                                        </div>
                                        <button
                                            type='button'
                                            onClick={() => removeFieldValue('attributionZdbIDs', idx)}
                                            className='btn btn-link'
                                        >
                                            <i className='fas fa-times' />
                                        </button>
                                    </div>
                                ))
                            }
                            <button
                                type='button'
                                className='btn btn-link px-0'
                                onClick={() => pushFieldValue('attributionZdbIDs')}
                            >
                                Add Citation
                            </button>
                        </div>
                    </div>
                </>
                }

            </AddEditDeleteModal>
        </>
    );
};

AntibodyEditAliases.propTypes = {
    antibodyId: PropTypes.string,
};

export default AntibodyEditAliases;
