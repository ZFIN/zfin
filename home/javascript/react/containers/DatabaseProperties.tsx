import React, { useState, FormEvent } from 'react';
import useFetch from '../hooks/useFetch';
import http from '../utils/http';
import LoadingSpinner from '../components/LoadingSpinner';
import GenericErrorMessage from '../components/GenericErrorMessage';

const API_URL = '/action/devtool/database-properties';

interface DatabaseProperty {
    id: number;
    name: string;
    value: string;
    type: string;
}

interface Message {
    type: 'success' | 'error';
    text: string;
}

const EMPTY_FORM: DatabaseProperty = { id: 0, name: '', value: '', type: 'string' };

const DatabaseProperties = () => {
    const { pending, rejected, value: properties, setValue: setProperties, refetch } = useFetch(API_URL);
    const [form, setForm] = useState<DatabaseProperty>(EMPTY_FORM);
    const [editing, setEditing] = useState(false);
    const [message, setMessage] = useState<Message | null>(null);

    const showMessage = (type: Message['type'], text: string) => {
        setMessage({ type, text });
        setTimeout(() => setMessage(null), 3000);
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleEdit = (prop: DatabaseProperty) => {
        setForm({ ...prop });
        setEditing(true);
    };

    const handleCancel = () => {
        setForm(EMPTY_FORM);
        setEditing(false);
    };

    const handleDelete = async (prop: DatabaseProperty) => {
        if (!confirm(`Delete property "${prop.name}"?`)) { // eslint-disable-line no-alert
            return;
        }
        try {
            await http.delete(`${API_URL}/${prop.id}`);
            setProperties((properties as DatabaseProperty[]).filter(p => p.id !== prop.id));
            showMessage('success', 'Deleted');
        } catch {
            showMessage('error', 'Failed to delete');
        }
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        const payload: Partial<DatabaseProperty> = {
            name: form.name,
            value: form.value,
            type: 'string',
        };
        if (form.id) {
            payload.id = form.id;
        }
        try {
            await http.post(API_URL, payload);
            showMessage('success', form.id ? 'Updated' : 'Created');
            setForm(EMPTY_FORM);
            setEditing(false);
            refetch();
        } catch {
            showMessage('error', 'Failed to save');
        }
    };

    if (pending) {
        return <LoadingSpinner />;
    }

    if (rejected || !properties) {
        return <GenericErrorMessage />;
    }

    return (
        <div>
            {message && (
                <div className={`alert alert-${message.type === 'error' ? 'danger' : 'success'}`}>
                    {message.text}
                </div>
            )}

            <table className='table table-striped table-bordered'>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Value</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {(properties as DatabaseProperty[]).map(prop => (
                        <tr key={prop.id}>
                            <td>{prop.id}</td>
                            <td>{prop.name}</td>
                            <td>{prop.value}</td>
                            <td>
                                <button className='btn btn-sm btn-info' onClick={() => handleEdit(prop)}>Edit</button>
                                {' '}
                                <button className='btn btn-sm btn-danger' onClick={() => handleDelete(prop)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <h4>Add / Edit Property</h4>
            <form className='form-inline' style={{ marginBottom: 20 }} onSubmit={handleSubmit}>
                <div className='form-group' style={{ marginRight: 10 }}>
                    <label htmlFor='form-name' style={{ marginRight: 5 }}>Name</label>
                    <input
                        type='text'
                        className='form-control'
                        id='form-name'
                        name='name'
                        value={form.name}
                        onChange={handleChange}
                        disabled={editing}
                        required
                    />
                </div>
                <div className='form-group' style={{ marginRight: 10 }}>
                    <label htmlFor='form-value' style={{ marginRight: 5 }}>Value</label>
                    <input
                        type='text'
                        className='form-control'
                        id='form-value'
                        name='value'
                        value={form.value}
                        onChange={handleChange}
                        style={{ width: 400 }}
                    />
                </div>
                <button type='submit' className='btn btn-primary'>
                    {editing ? 'Update' : 'Save'}
                </button>
                {editing && (
                    <button type='button' className='btn btn-default' style={{ marginLeft: 5 }} onClick={handleCancel}>
                        Cancel
                    </button>
                )}
            </form>
        </div>
    );
};

export default DatabaseProperties;
