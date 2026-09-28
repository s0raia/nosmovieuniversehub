import { apiFetch, ensureCsrfCookie } from './client';
import type { HomeSection, Movie, MovieDetail, Playlist, PlaylistCompare } from '../types/catalogue';

export type { HomeSection, Movie, MovieDetail, Playlist, PlaylistCompare } from '../types/catalogue';

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

export const fetchPlaylistCompare = (leftId: number, rightId: number) =>
  getJson<PlaylistCompare>(`/api/playlists/compare?left=${leftId}&right=${rightId}`);

export const fetchStarredIds = () => getJson<{ tmdbIds: number[] }>('/api/me/starred');

export const fetchMyRating = (tmdbId: number) =>
  getJson<{ tmdbId: number; stars: number | null }>(`/api/me/ratings/${tmdbId}`);

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

export async function starFilm(tmdbId: number): Promise<void> {
  await ensureCsrfCookie();
  const response = await apiFetch(`/api/me/starred/${tmdbId}`, { method: 'POST' });
  if (!response.ok) {
    throw new Error('Could not add to starred list');
  }
}

export async function unstarFilm(tmdbId: number): Promise<void> {
  await ensureCsrfCookie();
  const response = await apiFetch(`/api/me/starred/${tmdbId}`, { method: 'DELETE' });
  if (!response.ok) {
    throw new Error('Could not remove from starred list');
  }
}

export async function saveMyRating(tmdbId: number, stars: number): Promise<void> {
  await ensureCsrfCookie();
  const response = await apiFetch(`/api/me/ratings/${tmdbId}`, {
    method: 'PUT',
    body: JSON.stringify({ stars }),
  });
  if (!response.ok) {
    throw new Error('Could not save rating');
  }
}

export async function addPlaylistItem(
  playlistId: number,
  tmdbId: number,
): Promise<{ itemId: number; position: number; film: Movie }> {
  await ensureCsrfCookie();
  const response = await apiFetch(`/api/me/playlists/${playlistId}/items`, {
    method: 'POST',
    body: JSON.stringify({ tmdbId }),
  });
  if (response.status === 403) {
    throw new Error('You cannot edit this playlist');
  }
  if (!response.ok) {
    throw new Error('Could not add film to playlist');
  }
  return (await response.json()) as { itemId: number; position: number; film: Movie };
}

export async function removePlaylistItem(playlistId: number, itemId: number): Promise<void> {
  await ensureCsrfCookie();
  const response = await apiFetch(`/api/me/playlists/${playlistId}/items/${itemId}`, {
    method: 'DELETE',
  });
  if (response.status === 403) {
    throw new Error('You cannot edit this playlist');
  }
  if (!response.ok) {
    throw new Error('Could not remove film from playlist');
  }
}

export async function movePlaylistItem(
  itemId: number,
  targetPlaylistId: number,
): Promise<void> {
  await ensureCsrfCookie();
  const response = await apiFetch(`/api/me/playlist-items/${itemId}/move`, {
    method: 'POST',
    body: JSON.stringify({ targetPlaylistId }),
  });
  if (response.status === 403) {
    throw new Error('You cannot move into that playlist');
  }
  if (!response.ok) {
    throw new Error('Could not move film');
  }
}
