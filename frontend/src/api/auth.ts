import { apiFetch } from './client';

export type User = { username: string };

export async function fetchCurrentUser(): Promise<User | null> {
  const response = await apiFetch('/api/auth/me');
  if (response.status === 401 || response.status === 403) {
    return null;
  }
  if (!response.ok) {
    throw new Error(`auth/me responded ${response.status}`);
  }
  return (await response.json()) as User;
}

export async function login(username: string, password: string): Promise<User> {
  const response = await apiFetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  if (!response.ok) {
    throw new Error('Invalid username or password');
  }
  return (await response.json()) as User;
}

export async function register(username: string, password: string): Promise<User> {
  const response = await apiFetch('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  if (response.status === 409) {
    throw new Error('Username already taken');
  }
  if (!response.ok) {
    throw new Error('Could not register');
  }
  return (await response.json()) as User;
}

export async function logout(): Promise<void> {
  await apiFetch('/api/auth/logout', { method: 'POST' });
}
