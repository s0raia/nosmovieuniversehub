import { apiFetch, ensureCsrfCookie } from './client';
import type { HomeSection, Movie, MovieDetail, Playlist } from '../types/catalogue';

export type { HomeSection, Movie, MovieDetail, Playlist } from '../types/catalogue';

async function getJson<T>(path: string): Promise<T> {
  const response = await apiFetch(path);
  if (!response.ok) {
    throw new Error(`${path} responded ${response.status}`);
  }
  return (await response.json()) as T;
}

export const fetchHome = () => getJson<HomeSection[]>('/api/home');
export const fetchMovies = () => getJson<Movie[]>('/api/movies');
export const fetchMovie = (tmdbId: number) => getJson<MovieDetail>(`/api/movies/${tmdbId}`);
export const fetchPlaylists = () => getJson<Playlist[]>('/api/playlists');
export const fetchMyPlaylists = () => getJson<Playlist[]>('/api/me/playlists');

export async function createPlaylist(name: string): Promise<Playlist> {
  await ensureCsrfCookie();
  const response = await apiFetch('/api/me/playlists', {
    method: 'POST',
    body: JSON.stringify({ name }),
  });
  if (!response.ok) {
    throw new Error('Could not create playlist');
  }
  return (await response.json()) as Playlist;
}

export async function renamePlaylist(id: number, name: string): Promise<Playlist> {
  await ensureCsrfCookie();
  const response = await apiFetch(`/api/me/playlists/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ name }),
  });
  if (response.status === 403) {
    throw new Error('You cannot rename this playlist');
  }
  if (!response.ok) {
    throw new Error('Could not rename playlist');
  }
  return (await response.json()) as Playlist;
}
