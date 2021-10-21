import React, {useState} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';
import useAddEditDeleteForm from '../hooks/useAddEditDeleteForm';
import FormGroup from '../components/form/FormGroup';
import InputField from '../components/form/InputField';
import PublicationInput from '../components/form/PublicationInput';
import AddEditList from '../components/AddEditList';
import AddEditDeleteModal from '../components/AddEditDeleteModal';

const MarkerEditSequences = ({markerId, group = 'marker linked sequence', groupDB = 'dblink adding on marker-edit'}) => {
    const links = useFetch(`/action/marker/${markerId}/links?group=${group}`);
    const databases = useFetch(`/action/marker/link/databases?group=${groupDB}`);
    const [modalLink, setModalLink] = useState(null);
    const isEdit = modalLink && !!modalLink.dblinkZdbID;

    const {
        pushFieldValue,
        removeFieldValue,
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/marker/${markerId}/links`,
        editUrl: isEdit ? `/action/marker/link/${modalLink.dblinkZdbID}` : '',
        deleteUrl: isEdit ? `/action/marker/link/${modalLink.dblinkZdbID}` : '',
        onSuccess: () => setModalLink(null),
        items: links.value,
        setItems: links.setValue,
        itemKeyProp: 'dblinkZdbID',
        defaultValues: modalLink,
        validate: values => {
            if (values && (!Array.isArray(values.references) || values.references.length === 0)) {
                return 'At least one reference is required';
            }
            return false;
        },
    });

    const formatLink = (link, editLink) => {
        return (
            <>
                <a href={link.link}>
                    {link.referenceDatabaseName}:{link.accession}
                </a>
                {link.length && <> &ndash; {link.length} nt/aa</>}
                {link.references && link.references.length && <> ({link.references.length})</>} {editLink}
            </>
        );
    };

    if (links.pending || databases.pending) {
        return <LoadingSpinner/>;
    }

    if (!links.value) {
        return null;
    }

    return (
        <>
            <AddEditList
                formatItem={formatLink}
                itemKeyProp='dblinkZdbID'
                items={links.value}
                newItem={{
                    accession: '',
                    length: '',
                    referenceDatabaseZdbID: '',
                    references: [{zdbID: ''}],
                }}
                setModalItem={setModalLink}
            />

            <AddEditDeleteModal {...modalProps} header='Sequence'>
                {values && <>
                    {!isEdit &&
                    <FormGroup
                        inputClassName='col-md-10'
                        label='Database'
                        id='database'
                        field='referenceDatabaseZdbID'
                        tag='select'
                        validate={value => value ? false : 'A database is required'}
                    >

                        <option value=''/>
                        {databases.value.map(database => (
                            <option
                                value={database.zdbID}
                                key={database.zdbID}
                            >{database.name}-{database.type}</option>
                        ))}

                    </FormGroup>
                    }


                    {isEdit &&
                    <FormGroup
                        label='Database'
                        id='referenceDatabaseName'
                        field='referenceDatabaseName'
                        readOnly

                    />
                    }

                    <FormGroup
                        inputClassName='col-md-10'
                        label='Accession'
                        id='accession'
                        field='accession'
                        validate={value => value ? false : 'An accession is required'}
                    />

                    <FormGroup
                        inputClassName='col-md-10'
                        label='Length'
                        id='length'
                        field='length'
                        validate={value => value ? false : 'A length is required'}
                    />

                    <div className='form-group row'>
                        <label className='col-md-2 col-form-label'>Citations</label>
                        <div className='col-md-10'>
                            {
                                values.references && values.references.map((reference, idx) => (
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
                                            <i className='fas fa-times'/>
                                        </button>
                                    </div>
                                ))
                            }
                            <button
                                type='button'
                                className='btn btn-link px-0'
                                onClick={() => pushFieldValue('references', {zdbID: ''})}
                            >
                                Add Citation
                            </button>
                        </div>
                    </div>
                </>}
            </AddEditDeleteModal>
        </>
    );
};

MarkerEditSequences.propTypes = {
    markerId: PropTypes.string,
    group: PropTypes.string,
    groupDB: PropTypes.string,
};

export default MarkerEditSequences;
