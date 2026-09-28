function readCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
  return match ? decodeURIComponent(match[1]) : null;
}

export async function apiFetch(input: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers);
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  const csrf = readCookie('XSRF-TOKEN');
  if (csrf && init.method && init.method !== 'GET') {
    headers.set('X-XSRF-TOKEN', csrf);
  }
  return fetch(input, { ...init, headers, credentials: 'include' });
}
