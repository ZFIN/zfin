import { ProblemDetail } from './types';

const API_BASE = '/action/api/zirc';

export class ApiError extends Error {
    constructor(public status: number, public problem: ProblemDetail) {
        super(problem.title || problem.detail || `HTTP ${status}`);
        this.name = 'ApiError';
    }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
    const hasBody = init.body !== undefined && init.body !== null;
    // FormData carries its own multipart Content-Type with a boundary; we
    // must NOT pre-set application/json or the browser won't add the
    // boundary param and the request becomes unparseable on the server.
    const isFormData = hasBody && typeof FormData !== 'undefined' && init.body instanceof FormData;
    const response = await fetch(API_BASE + path, {
        ...init,
        headers: {
            Accept: 'application/json, application/problem+json',
            ...(hasBody && !isFormData ? { 'Content-Type': 'application/json' } : {}),
            ...init.headers,
        },
    });

    if (!response.ok) {
        let problem: ProblemDetail;
        try {
            problem = await response.json();
        } catch {
            problem = { title: response.statusText, status: response.status };
        }
        throw new ApiError(response.status, problem);
    }

    if (response.status === 204) {return undefined as unknown as T;}
    return response.json() as Promise<T>;
}

export const api = {
    get: <T>(path: string) => request<T>(path),
    post: <T>(path: string, body?: unknown) =>
        request<T>(path, {
            method: 'POST',
            body: body === undefined ? undefined : JSON.stringify(body),
        }),
    patch: <T>(path: string, body: unknown) =>
        request<T>(path, { method: 'PATCH', body: JSON.stringify(body) }),
    delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
    // Multipart POST. body is a FormData; the request helper detects it and
    // skips the JSON Content-Type so the browser can supply the boundary.
    upload: <T>(path: string, body: FormData) =>
        request<T>(path, { method: 'POST', body }),
};
