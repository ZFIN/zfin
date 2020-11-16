import React, { useState } from 'react';
import PropTypes from 'prop-types';
import useAddEditDeleteForm from '../../hooks/useAddEditDeleteForm';
import AddEditList from '../AddEditList';
import AddEditDeleteModal from '../AddEditDeleteModal';
import FormGroup from '../form/FormGroup';
import InputField from '../form/InputField';
import PublicationInput from '../form/PublicationInput';

const MarkerAliases = ({markerId, aliases, setAliases}) => {
    const [modalAlias, setModalAlias] = useState(null);
    const isEdit = modalAlias && modalAlias.zdbID;

    const {
        pushFieldValue,
        removeFieldValue,
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/marker/${markerId}/aliases`,
        editUrl: isEdit ? `/action/marker/alias/${modalAlias.zdbID}` : '',
        deleteUrl: isEdit ? `/action/marker/alias/${modalAlias.zdbID}` : '',
        onSuccess: () => setModalAlias(null),
        items: aliases,
        setItems: setAliases,
        defaultValues: modalAlias,
        validate: values => {
            if (values && values.references.length === 0) {
                return 'At least one reference is required';
            }
            return false;
        },
    });

    const formatAlias = (alias, editLink) => {
        return <>{alias.alias} {alias.references.length > 0 && <>({alias.references.length})</>} {editLink}</>
    }

    return (
        <>
            <AddEditList
                formatItem={formatAlias}
                items={aliases}
                newItem={{
                    alias: '',
                    references: [{ zdbID: '' }],
                }}
                setModalItem={setModalAlias}
            />

            <AddEditDeleteModal {...modalProps} header='Previous Name'>
                {values && <>
                    <FormGroup
                        inputClassName='col-md-10'
                        label='Name'
                        id='alias'
                        field='alias'
                        validate={value => value ? false : 'An alias is required'}
                    />

                    <div className='form-group row'>
                        <label className='col-md-2 col-form-label'>Citations</label>
                        <div className='col-md-10'>
                            {
                                values.references.map((reference, idx) => (
                                    <div key={idx} className={`d-flex align-items-baseline ${idx > 0 ? 'mt-2' : ''}`}>
                                        <div className='flex-grow-1'>
                                            <InputField
                                                tag={PublicationInput}
                                                field={`references.${idx}.zdbID`}
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
                                            onClick={() => removeFieldValue('references', idx)}
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
                                onClick={() => pushFieldValue('references', { zdbID: '' })}
                            >
                                Add Citation
                            </button>
                        </div>
                    </div>
                </>}
            </AddEditDeleteModal>
        </>
    )
};

MarkerAliases.propTypes = {
    markerId: PropTypes.string,
    aliases: PropTypes.array,
    setAliases: PropTypes.func,
};

export default MarkerAliases;
