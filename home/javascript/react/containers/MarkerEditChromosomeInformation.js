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


const MarkerEditChromosomeInformation = ({hdr}) => {
    const [links, setLinks] = useState([]);
    const [modalLink, setModalLink] = useState(null);
    const isEdit = modalLink && !!modalLink.dblinkZdbID;
    const [isOpen, setIsOpen] = useState(false);
    const [value, setValue] = useState(null);
    const pushFieldValue = "A";
    const removeFieldValue = "B";
    const values = "C";
    const {
        Form,
        meta: { isSubmitting, canSubmit }
    } = useForm({
        onSubmit: async (values, instance) => {
            console.log("Huzzah!");
        },
        debugForm: true
    });

    const formatLink = (link, editLink) => {
        return (
            <>
                <a href={'#'}>
                    {'link.referenceDatabaseName'}:{'link.accession'}
                </a>
                {' '}
                {'link.references && link.references.length && <>({link.references.length})</>'} {editLink}
            </>
        );
    }

    const modalProps = {
        'Form': Form,
        'formMeta': "b",
        'isOpen':   isOpen,
        'isEdit':   isEdit,
        'deleting': "e",
        'onDelete': "f",
        'onCancel': "g"
    };

    const setModalLinkProxy = (val) => {
        console.log('val received');
        console.log(val);
        setIsOpen(true);
        return setModalLink(val);
    }

    return (
        <>
            <AddEditList
                title={hdr}
                formatItem={formatLink}
                itemKeyProp='dblinkZdbID'
                items={links}
                newItem={{
                    accession: '',
                    length: '',
                    referenceDatabaseZdbID: '',
                    references: [{zdbID: ''}],
                }}
                setModalItem={setModalLinkProxy}
            />
            <AddEditDeleteModal {...modalProps} header={hdr}>
                <>
                    {!isEdit &&
                    <FormGroup
                        inputClassName='col-md-10'
                        label='Sequence'
                        id='data'
                        field='data'
                        validate={value => value ? false : 'A sequence is required'}
                    />
                    }
                </>
            </AddEditDeleteModal>
        </>
    );
};

export default MarkerEditChromosomeInformation;