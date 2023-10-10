import React, {useEffect, useState, useRef} from 'react';
import PropTypes from 'prop-types';
import MarkerInput from '../components/form/MarkerInput';
import {useForm} from 'react-form';
import SequenceTargetingReagentSequenceFields from '../components/marker-edit/SequenceTargetingReagentSequenceFields';
import InputField from '../components/form/InputField';

const NewSequenceTargetingReagentForm = ({ pubId: defaultPubId, strType: defaultStrType, strTypesJson, fieldErrorsJson }) => {
    const strTypes = JSON.parse(strTypesJson);
    const fieldErrors = fieldErrorsJson === '' ? [] : JSON.parse(fieldErrorsJson);
    const [targetGenes, setTargetGenes] = useState([]);
    const [targetGene, setTargetGene] = useState('');
    const [stagedGene, setStagedGene] = useState('');
    const [strType, setStrType] = useState(defaultStrType);
    const [isManuallyNamed, setIsManuallyNamed] = useState(false);
    const [manualNamingError, setManualNamingError] = useState('');

    const [defaultFormValues] = useState({
        publicationID: defaultPubId,
        strType: defaultStrType,
        publicNote: '',
        curatorNote: '',
        alias: '',
        name: '',
        reportedSequence: '',
        sequence: '',
        reversed: false,
        complemented: false,
        reportedSequence2: '',
        sequence2: '',
        reversed2: false,
        complemented2: false,
    });

    const formChildRef = useRef(null);

    const {
        Form,
        setFieldValue,
        values,
    } = useForm({
        defaultValues: defaultFormValues,
        onSubmit: async () => {
            //validation for target genes
            let genes = [...targetGenes];
            let targetGeneSymbol = genes.join(',');
            let name = values.name;

            if (targetGene !== '' && genes.length === 0) {
                name = name + targetGene;
                targetGeneSymbol = targetGene;
            } else if (genes.length === 0) {
                //ignore lint error because we want to alert the user
                // eslint-disable-next-line no-alert
                alert('Please enter a target gene.');
                return;
            }

            //put all the elements of the genes array into the form
            formElement().elements['targetGeneSymbol'].value = targetGeneSymbol;
            formElement().elements['name'].value = name;
            formElement().submit();
        },
    });

    function isTalen() { return strType === 'TALEN' }
    function validBases() { return isTalen() ? 'ATGCR' : 'ATGC' }
    function reportedLabel() { return isTalen() ? 'Target Sequence 1 Reported' : 'Reported' }

    function handleGeneChange(event) {
        if (event.type === 'typeahead:select') {
            setStagedGene(event.target.value);
            setTargetGene('');
        } else {
            setTargetGene(event.target.value);
        }
    }

    function handleGeneDelete(event, gene) {
        event.preventDefault();
        setTargetGenes(targetGenes.filter(g => g !== gene));
    }

    function computeName(newType, newGenes) {
        if (isManuallyNamed) {
            return;
        }

        const combinedGenes = newGenes.join(',');
        fetch(`/action/marker/propose-name-by-type-and-genes?type=${newType}&genes=${combinedGenes}`)
            .then(response => response.text())
            .then(data => {
                setFieldValue('name', data);
            });
    }

    function handleNameChange(event) {
        if (isManuallyNamed) {
            fetch(`/action/marker/name-exists?name=${encodeURIComponent(event.target.value)}`)
                .then(response => response.text())
                .then(data => {
                    setManualNamingError(data);
                });
        }
    }

    function formElement() {
        return formChildRef.current.parentElement;
    }

    useEffect(() => {
        if (stagedGene === '') {
            return;
        }
        setTargetGenes([...targetGenes, stagedGene]);
        setStagedGene('');
    }, [stagedGene]);

    useEffect(() => {
        computeName(strType, targetGenes);
    }, [strType, targetGenes]);

    useEffect(() => {
        if (values.strType !== strType) {
            setStrType(values.strType);
        }
    }, [values]);

    return (
        <>
            {fieldErrors.length > 0 &&
                <ul className={'alert alert-danger'}>
                    {fieldErrors.map(error => <li key={error.message}>{error.message + (error.rejectedValue ? ' (' + error.rejectedValue + ')' : '')}</li>)}
                </ul>
            }
            <Form method='POST'>
                <div className='form-group row' ref={formChildRef}>
                    <label htmlFor='publicationID' className='col-md-2 col-form-label'>Reference</label>
                    <div className='col-md-4'>
                        <InputField
                            id='publicationID'
                            name='publicationID'
                            placeholder='ZDB-PUB-123456-7'
                            field='publicationID'
                            validate={value => {
                                if (value === '') {
                                    return 'Reference is required';
                                }
                                return false;
                            }}
                        />
                    </div>
                </div>
                <div className='form-group row'>
                    <label htmlFor='strType' className='col-md-2 col-form-label'>Type</label>
                    <div className='col-md-4'>
                        <InputField
                            id='strType'
                            name='strType'
                            field='strType'
                            tag='select'
                        >
                            <option value='' disabled='disabled'>Select...</option>
                            {Object.keys(strTypes).map(key => <option key={key} value={key}>{strTypes[key]}</option>)}
                        </InputField>
                    </div>
                </div>

                <div className='form-group row'>
                    <label htmlFor='targetGeneSymbol' className='col-md-2 col-form-label'>Target Gene(s)</label>
                    <div className='col-md-4'>
                        <MarkerInput
                            typeGroup={'GENEDOM_AND_NTR'}
                            typeGroup2={'GENEDOM_AND_NTR'}
                            id='targetGeneSymbol'
                            name='targetGeneSymbol'
                            className='form-control'
                            value={targetGene}
                            onChange={(e) => {handleGeneChange(e)}}
                        />
                    </div>
                </div>
                <div className='row mb-3'>
                    <div className='col-md-2'/>
                    <div className='col-md-4'>
                        <ul className='list-unstyled'>
                            {targetGenes.map(gene => <li key={gene}>{gene} <a href='#' onClick={e => {handleGeneDelete(e, gene)}}><i className='fa fa-trash'/></a></li>)}
                        </ul>
                    </div>
                </div>

                <div className='form-group row'>
                    <label htmlFor='name' className='col-md-2 col-form-label'>Name</label>
                    <div className='col-md-3'>
                        <InputField
                            id='name'
                            name='name'
                            field='name'
                            readOnly={!isManuallyNamed}
                            onChange={handleNameChange}
                        />
                    </div>
                    <button type={'button'} className={'btn btn-sm btn-secondary'} onClick={() => {setIsManuallyNamed(!isManuallyNamed)}}>{isManuallyNamed ? 'Auto Name' : 'Manually Name'}</button>
                </div>

                {manualNamingError &&
                <div className='form-group row'>
                    <div className='col-md-2 col-form-label'/>
                    <div className='ml-3 p-2 alert alert-danger'>{manualNamingError}</div>
                </div>}

                <div className='form-group row'>
                    <label htmlFor='alias' className='col-md-2 col-form-label'>Alias</label>
                    <div className='col-md-4'>
                        <InputField
                            id='alias'
                            name='alias'
                            field='alias'
                        />
                    </div>
                </div>

                <div className='form-group row'>
                    <label className='col-md-2 col-form-label'>Target Sequence</label>
                    <div className='col-md-6'>

                        <SequenceTargetingReagentSequenceFields
                            complementedField='complemented'
                            displayedSequenceField='sequence'
                            reportedLabel={reportedLabel()}
                            displayedLabel='Displayed'
                            reportedSequenceField='reportedSequence'
                            reversedField='reversed'
                            validBases={validBases()}
                            values={values}
                            setDisplayedSequence={value => setFieldValue('sequence', value)}
                            newRow={true}
                        />

                        {isTalen() &&
                            <div className='mt-4'>
                                <SequenceTargetingReagentSequenceFields
                                    complementedField='complemented2'
                                    displayedSequenceField='sequence2'
                                    reportedLabel='Target Sequence 2 Reported'
                                    displayedLabel='Displayed'
                                    reportedSequenceField='reportedSequence2'
                                    reversedField='reversed2'
                                    validBases={validBases()}
                                    values={values}
                                    setDisplayedSequence={value => setFieldValue('sequence2', value)}
                                    newRow={true}
                                />
                            </div>
                        }

                    </div>
                </div>

                <div className='form-group row'>
                    <label htmlFor='publicNote' className='col-md-2 col-form-label'>Public Note</label>
                    <div className='col-md-6'>
                        <InputField
                            id='publicNote'
                            name='publicNote'
                            field='publicNote'
                            tag='textarea'
                            rows='3'
                        />
                    </div>
                </div>
                <div className='form-group row'>
                    <label htmlFor='curatorNote' className='col-md-2 col-form-label'>Curator Note</label>
                    <div className='col-md-6'>
                        <InputField
                            id='curatorNote'
                            name='curatorNote'
                            field='curatorNote'
                            tag='textarea'
                            rows='3'
                        />
                    </div>
                </div>
                <div className='form-group row'>
                    <div className='offset-md-2 col-md-10'>
                        <button type='submit' className='btn btn-primary'>Submit</button>
                    </div>
                </div>
            </Form>
        </>
    );
}


NewSequenceTargetingReagentForm.propTypes = {
    pubId: PropTypes.string,
    strType: PropTypes.string,
    strTypesJson: PropTypes.string,
    fieldErrorsJson: PropTypes.string,
};

export default NewSequenceTargetingReagentForm;
