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

const MarkerAddProteinSequences = ({markerId, group = 'gene edit addable protein sequence', groupDB = 'gene edit addable protein sequence'}) => {
    const links = useFetch(`/action/marker/${markerId}/geneProtSequences?group=${group}`);
    const databases = useFetch(`/action/marker/link/databases?group=${groupDB}`);
    const [modalLink, setModalLink] = useState(null);
    const isEdit = modalLink && !!modalLink.dblinkZdbID;

    const {
        pushFieldValue,
        removeFieldValue,
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/marker/${markerId}/protein/seqLinks`,
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
                <a href={link.dbLink.link}>
                    {link.dbLink.referenceDatabaseName}:{link.dbLink.accession}
                </a>
                {' '}
                {link.dbLink.references && link.dbLink.references.length && <>({link.dbLink.references.length})</>} {editLink}
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
                title={'Protein Sequence'}
                formatItem={formatLink}
                itemKeyProp='zdbID'
                items={links.value}
                newItem={{
                    accession: '',
                    length: '',
                    sequence: '',
                    referenceDatabaseZdbID: '',
                    references: [{zdbID: ''}],
                }}
                setModalItem={setModalLink}
            />

            <AddEditDeleteModal {...modalProps} header=' Protein Sequence'>
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
                        label='Length'
                        id='length'
                        field='formattedData.length'
                        readOnly

                    />



                    <FormGroup
                        inputClassName='col-md-10'
                        label='Sequence'
                        id='sequence'
                        field='formattedData'
                        validate={value => value ? false : 'A sequence is required'}
                    />

                    {<div className='form-group row'>
                        <label className='col-md-2 col-form-label'>Citations</label>
                        <div className='col-md-10'>
                            {
                                values.references.map((reference, idx) => (
                                    <div key={idx} className={`d-flex align-items-baseline ${idx > 0 ? 'mt-2' : ''}`}>
                                        <div className='flex-grow-1'>
                                            <InputField
                                                tag={PublicationInput}
                                                field={`dbLink.references.${idx}.zdbID`}
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
                                            onClick={() => removeFieldValue('dbLink.references', idx)}
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
                                onClick={() => pushFieldValue('dbLink.references', {zdbID: ''})}
                            >
                                Add Citation
                            </button>
                        </div>
                    </div>}
                </>}
            </AddEditDeleteModal>
        </>
    );
};

MarkerAddProteinSequences.propTypes = {
    markerId: PropTypes.string,
    group: PropTypes.string,
    groupDB: PropTypes.string,
};

export default MarkerAddProteinSequences;
