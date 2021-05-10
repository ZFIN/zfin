import React, {useState} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import useAddEditDeleteForm from '../hooks/useAddEditDeleteForm';
import FormGroup from '../components/form/FormGroup';
import InputField from '../components/form/InputField';
import PublicationInput from '../components/form/PublicationInput';
import AddEditDeleteModal from '../components/AddEditDeleteModal';
import AddEditList from '../components/AddEditList';
import LoadingSpinner from '../components/LoadingSpinner';


const MarkerAddSequences = ({markerId, type, group = 'gene edit addable nucleotide sequence', groupDB = 'gene edit addable nucleotide sequence'}) => {
    const links = useFetch(`/action/marker/${markerId}/${type}/links?group=${group}`);

    const databases = useFetch(`/action/marker/${markerId}/link/${type}?group=${groupDB}`);
    const [modalLink, setModalLink] = useState(null);
    const isEdit = modalLink && !!modalLink.dblinkZdbID;

    const hdr = type + ' ' + 'Sequence';


    const {
        pushFieldValue,
        removeFieldValue,
        values,
        modalProps,

    } = useAddEditDeleteForm({
        addUrl: `/action/marker/${markerId}/${type}/seqLinks`,
        editUrl: isEdit ? `/action/marker/link/${modalLink.dblinkZdbID}` : '',
        deleteUrl: isEdit ? `/action/marker/link/${modalLink.dblinkZdbID}` : '',
        onSuccess: () => setModalLink(null),
        items: links.value,
        setItems: links.setValue,
        itemKeyProp: 'zdbID',
        defaultValues: modalLink,

        validate: values => {
            if (values && values.references.length === 0) {
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
                {' '}
                {link.references && link.references.length && <>({link.references.length})</>} {editLink}
            </>
        );
    }

    if (links.pending || databases.pending) {
        return <LoadingSpinner/>;
    }

    if (!links.value) {
        return null;
    }

    return (
        <>

            <AddEditList
                title={hdr}
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

            <AddEditDeleteModal {...modalProps} header={hdr}>

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
                    {!isEdit &&
                    <FormGroup
                        inputClassName='col-md-10'
                        label='Length'
                        id='length'
                        field='data.length'
                        readOnly

                    />
                    }
                    {!isEdit &&
                    <FormGroup
                        inputClassName='col-md-10'
                        label='Sequence'
                        id='data'
                        field='data'
                        validate={value => value ? false : 'A sequence is required'}
                    />
                    }
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


MarkerAddSequences.propTypes = {
    markerId: PropTypes.string,
    type: PropTypes.string,
    group: PropTypes.string,
    groupDB: PropTypes.string,
};

export default MarkerAddSequences;
