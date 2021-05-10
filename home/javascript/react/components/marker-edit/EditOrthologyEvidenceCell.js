import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { publicationType } from '../../utils/types';
import AddEditList from '../AddEditList';
import Modal from '../Modal';
import { useForm } from 'react-form';
import PublicationInput from '../form/PublicationInput';
import FormGroup from '../form/FormGroup';
import CheckboxList from '../CheckboxList';
import http from '../../utils/http';
import LoadingButton from '../LoadingButton';

const ORTHO_CURATION_PUB_ID = 'ZDB-PUB-030905-1';
const ORTHO_CURATION_PUB_NAME = 'Ortho Curation Pub';

const DEFAULT_PUB_SUGGESTIONS = [
    {
        id: ORTHO_CURATION_PUB_ID,
        name: ORTHO_CURATION_PUB_NAME,
    },
];

function zdbIdToDate(id) {
    const parts = id.split('-');
    const date = parts[2];
    if (!date) {
        return new Date(0);
    }
    // sorry, zfinners in the year 2090 :(
    const century = (date.substr(0, 1) === '9') ? '19' : '20';
    return new Date(century + date.substring(0, 2), Number(date.substring(2, 4)) - 1, date.substring(4, 6));
}

function comparePubs (a, b) {
    const aPubId = a[0];
    const bPubId = b[0];
    if (aPubId === ORTHO_CURATION_PUB_ID) {
        return -1;
    }
    if (bPubId === ORTHO_CURATION_PUB_ID) {
        return 1;
    }

    // ...the rest by zdbID
    const aDate = zdbIdToDate(aPubId);
    const bDate = zdbIdToDate(bPubId);
    return bDate.getTime() - aDate.getTime();
}

const EditOrthologyEvidenceCell = ({defaultPubId, evidenceCodes, evidenceSet, orthoZdbId, ortholog, onSave}) => {
    const [modalEvidence, setModalEvidence] = useState(null);
    const [modalError, setModalError] = useState('');
    const [modalWarning, setModalWarning] = useState('');
    const [deleting, setDeleting] = useState(false);
    const [isEdit, setIsEdit] = useState(false);

    const saveEvidence = async (evidence) => {
        try {
            const updated = await http.post(`/action/api/marker/orthologs/${orthoZdbId}/evidence`, evidence);
            onSave(updated);
            setModalEvidence(null);
        } catch (error) {
            setModalError(error.responseJSON.message);
        }
    };

    const {
        Form,
        meta: { isSubmitting, isValid },
        values,
        getFieldValue,
        setFieldValue,
    } = useForm({
        defaultValues: modalEvidence,
        onSubmit: saveEvidence,
    })

    const onDelete = async () => {
        setDeleting(true);
        await saveEvidence({
            ...values,
            evidenceCodeList: [],
        });
        setDeleting(false);
    }

    if (!evidenceSet) {
        return null;
    }

    const evidenceGroupedByPub = {};
    evidenceSet.forEach(evidence => {
        if (!evidenceGroupedByPub.hasOwnProperty(evidence.publication.zdbID)) {
            evidenceGroupedByPub[evidence.publication.zdbID] = [];
        }
        evidenceGroupedByPub[evidence.publication.zdbID].push(evidence.evidenceCode);
    });

    const items = Object.entries(evidenceGroupedByPub)
        .sort(comparePubs)
        .map(([pubId, codes]) => ({
            publicationID: pubId,
            orthologID: orthoZdbId,
            evidenceCodeList: codes,
        }));

    const formatItem = ({ publicationID, evidenceCodeList }, editLink) => {
        return (
            <>
                <a href={'/' + publicationID}>
                    {publicationID === ORTHO_CURATION_PUB_ID ? ORTHO_CURATION_PUB_NAME : publicationID}
                </a>: {evidenceCodeList.sort().join(', ')} {editLink}
            </>
        )
    }

    const setModalItem = (item, isEdit, isInputChange) => {
        const exists = evidenceGroupedByPub.hasOwnProperty(item.publicationID);
        if ((!isEdit || isInputChange) && exists) {
            setModalWarning(`${item.publicationID} is already a reference for ${ortholog.organism} ${ortholog.abbreviation} orthology. You are now editing the existing record.`);
        } else {
            setModalWarning('');
        }
        setIsEdit(isEdit);
        setModalEvidence(item);
    };

    const handlePubInputChange = (event) => {
        const pubId = event.target.value;
        const isEdit = evidenceGroupedByPub.hasOwnProperty(pubId);
        setModalItem({
            publicationID: pubId,
            orthologID: orthoZdbId,
            evidenceCodeList: evidenceGroupedByPub[pubId] || getFieldValue('evidenceCodeList'),
        }, isEdit, true);
    }

    return (
        <>
            <AddEditList
                items={items}
                itemKeyProp='publicationID'
                formatItem={formatItem}
                newItem={{
                    publicationID: defaultPubId || '',
                    orthologID: orthoZdbId,
                    evidenceCodeList: defaultPubId ? (evidenceGroupedByPub[defaultPubId] || []) : [],
                }}
                setModalItem={setModalItem}
            />

            <Modal open={modalEvidence !== null}>
                {values && <>
                    <div className='popup-header'>Evidence for {ortholog.organism} {ortholog.abbreviation} ortholog</div>
                    <div className='popup-body show-overflow'>
                        <Form>
                            <FormGroup
                                id={`${orthoZdbId}-pub-id`}
                                label='Publication'
                                tag={PublicationInput}
                                defaultPubs={DEFAULT_PUB_SUGGESTIONS}
                                field='publicationID'
                                inputClassName='col-md-10'
                                onChange={handlePubInputChange}
                                validate={value => {
                                    if (!value) {
                                        return 'A publication ZDB ID is required';
                                    }
                                    return false;
                                }}
                            />

                            {modalWarning && <div className='alert alert-warning'>{modalWarning}</div>}

                            <div className='form-group row'>
                                <label className='col-md-2 col-form-label'>Evidence Codes</label>
                                <div className='col-md-10'>
                                    <CheckboxList
                                        items={evidenceCodes}
                                        getItemDisplay={c => `${c.name} (${c.code})`}
                                        getItemKey={c => c.code}
                                        itemIdPrefix={orthoZdbId}
                                        value={values.evidenceCodeList}
                                        onChange={value => setFieldValue('evidenceCodeList', value)}
                                    />
                                </div>
                            </div>

                            {modalError && <div className='error'>{modalError}</div>}

                            <div className='d-flex justify-content-between'>
                                {isEdit ?
                                    <LoadingButton
                                        className='btn btn-danger'
                                        loading={deleting}
                                        onClick={onDelete}
                                        type='button'
                                    >
                                        Delete
                                    </LoadingButton> :
                                    <span />
                                }
                                <span className='horizontal-buttons'>
                                    <button
                                        className='btn btn-outline-secondary'
                                        onClick={() => setModalEvidence(null)}
                                        type='button'
                                    >
                                        Cancel
                                    </button>
                                    <LoadingButton
                                        className='btn btn-primary'
                                        disabled={isSubmitting || !isValid}
                                        type='submit'
                                        loading={isSubmitting}
                                    >
                                        Save
                                    </LoadingButton>
                                </span>
                            </div>
                        </Form>
                    </div>
                </>}
            </Modal>
        </>
    );
};

EditOrthologyEvidenceCell.propTypes = {
    defaultPubId: PropTypes.string,
    evidenceCodes: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string,
        code: PropTypes.string,
    })),
    evidenceSet: PropTypes.arrayOf(PropTypes.shape({
        evidenceCode: PropTypes.string,
        evidenceName: PropTypes.string,
        publication: publicationType,
    })),
    orthoZdbId: PropTypes.string,
    ortholog: PropTypes.shape({
        abbreviation: PropTypes.string,
        organism: PropTypes.string,
    }),
    onSave: PropTypes.func,
};

export default EditOrthologyEvidenceCell;
