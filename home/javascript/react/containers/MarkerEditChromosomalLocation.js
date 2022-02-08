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
import { useForm } from 'react-form';
import NoData from '../components/NoData';
import MarkerEditSequences from './MarkerEditSequences';


const MarkerEditChromosomalLocation = ({markerId, type, group = 'gene edit addable chromosome information', groupDB = 'gene edit addable chromosome information'}) => {
    const [error, setError] = useState('');
    const [deleting, setDeleting] = useState('');
    const [modalData, setModalData] = useState(null);
    // const isEdit = modalData && modalData.zdbID;
    const isEdit = false;
    const [liveData, setLiveData] = useState([]); //instead of useFetch (liveData=suppliers in markerEditSuppliers.js)
    const hdr = type;
    const assemblies = ['GRCz11', 'GRCz10', 'Zv9'];

    const setLiveDataProxy = (val) => {
        console.log('liveDataProxy');
        console.log(val);
        setLiveData(val);
    };

    const handleOnSuccess = (val) => {
        console.log("onSuccess: val");
        console.log(val);
        setModalData(null);
    }

    const {
        pushFieldValue,
        removeFieldValue,
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/marker/${markerId}/chromosomal-location`,
        editUrl: isEdit && `/action/marker/${markerId}/chromosomal-location/todoEdit`,
        deleteUrl: `/action/marker/${markerId}/chromosomal-location/todoDelete`,
        onSuccess: handleOnSuccess,
        items: liveData,
        setItems: setLiveDataProxy,
        defaultValues: modalData,
        isEdit
        // validate: values => true //TODO: validate
    });

    console.log("modalProps", modalProps);
    console.log("values", values);

    const handleDeleteClick = async (event, item) => {
        console.log('not impl');
    }

    const handleAddClick = (a,b) => {
        console.log('add click');

        console.log(a);
        // console.log(b);
        console.log(values);
        console.log(liveData);
        setModalData({});
    }

    return (
        <>
            {liveData.length === 0 && <NoData placeholder='None' />}

            <ul className='list-unstyled'>
                {liveData.map(item => {
                    return (
                        <li key={item.zdbID}>
                            <a href={`/${item.zdbID}`}>{item.chromosome}</a>
                            {deleting === item.zdbID ?
                                <LoadingSpinner /> :
                                <a className='show-on-hover px-2' href='#' onClick={e => handleDeleteClick(e, item)}>
                                    Delete
                                </a>
                            }
                        </li>
                    );
                })}
            </ul>

            {error && <div className='text-danger'>{error}</div>}

            <button type='button' className='btn btn-link px-0' onClick={handleAddClick}>Add</button>
            <div><span>isEdit:</span>{modalProps.isEdit}</div>
            <AddEditDeleteModal {...modalProps} header={hdr}>
                {values &&
                    <>
                        <FormGroup
                            labelClassName='col-md-3'
                            inputClassName='col-md-9'
                            label='Assembly'
                            id='assembly'
                            field='assembly'
                            tag='select'
                        >
                            <option value=''/>
                            {assemblies.map(assembly => (
                                <option
                                    value={assembly}
                                    key={assembly}
                                >{assembly}</option>
                            ))}
                        </FormGroup>
                        <FormGroup
                            labelClassName='col-md-3'
                            inputClassName='col-md-9'
                            label='Chromosome'
                            id='chromosome'
                            field='chromosome'
                            // validate={value => value ? false : 'An accession is required'}
                        />
                        <FormGroup
                            labelClassName='col-md-3'
                            inputClassName='col-md-9'
                            label='Start Location'
                            id='start-location'
                            field='startLocation'
                            // validate={value => value ? false : 'An accession is required'}
                        />
                        <FormGroup
                            labelClassName='col-md-3'
                            inputClassName='col-md-9'
                            label='End Location'
                            id='end-location'
                            field='endLocation'
                            // validate={value => value ? false : 'An accession is required'}
                        />
                    </>
                }
            </AddEditDeleteModal>
        </>
    );
};

MarkerEditChromosomalLocation.propTypes = {
    markerId: PropTypes.string,
    group: PropTypes.string,
    groupDB: PropTypes.string,
};

export default MarkerEditChromosomalLocation;