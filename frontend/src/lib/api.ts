import { ApiResponse } from './types';

const API_BASE = '/api/v1';

export class ApiError extends Error {
  code: string;
  details?: any[];

  constructor(message: string, code: string = 'UNKNOWN_ERROR', details?: any[]) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.details = details;
  }
}

export async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {}),
  };

  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;

  try {
    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_user');
        window.location.href = '/login';
      }
    }

    const data: ApiResponse<T> = await response.json().catch(() => ({
      success: response.ok,
      timestamp: new Date().toISOString(),
      data: null as any,
    }));

    if (!response.ok || data.success === false) {
      const err = data.error;
      throw new ApiError(
        err?.message || `HTTP error ${response.status}: ${response.statusText}`,
        err?.code || `HTTP_${response.status}`,
        err?.details
      );
    }

    return data.data;
  } catch (err: any) {
    if (err instanceof ApiError) throw err;
    throw new ApiError(err.message || 'Network request failed');
  }
}

export const api = {
  get: <T>(url: string) => apiFetch<T>(url, { method: 'GET' }),
  post: <T>(url: string, body?: any) => apiFetch<T>(url, { method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  put: <T>(url: string, body?: any) => apiFetch<T>(url, { method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(url: string) => apiFetch<T>(url, { method: 'DELETE' }),
};
