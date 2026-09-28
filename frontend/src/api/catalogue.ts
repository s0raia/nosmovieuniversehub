import { apiFetch } from './client';

export type Movie = {
  tmdbId: number;
  title: string | null;
  releaseYear: number | null;
  posterUrl: string | null;
  overview: string | null;
  tmdbVoteAverage: number | null;
  tmdbVoteCount: number;
  localVoteAverage: number | null;
  localVoteCount: number;
  combinedRating: number | null;
};

export type MovieDetail = Movie & {
  originalTitle: string | null;
  runtimeMinutes: number | null;
  genres: string[];
};

export type HomeSection = {
  id: string;
  title: string;
  films: Movie[];
};

export type Playlist = {
  id: number;
  externalId: string | null;
  name: string;
  owner: string;
  filmCount: number;
  films: Movie[];
};

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
