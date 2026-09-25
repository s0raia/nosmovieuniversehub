/*
  Calls go to /api, which Vite proxies to the backend in development and nginx
  proxies in the container. The browser therefore only ever sees one origin, so
  there is no CORS handling here and none on the server.
*/

/**
 * A film as the backend returns it.
 *
 * Every rating field is nullable, and null means "no information", never zero.
 * A film TMDB holds no votes for has a null average, and showing that as 0
 * would be wrong: 0 is a real and very bad score.
 */
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

export type Playlist = {
  id: number;
  externalId: string | null;
  name: string;
  owner: string;
  filmCount: number;
  films: Movie[];
};

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path, { headers: { Accept: 'application/json' } });
  if (!response.ok) {
    throw new Error(`${path} responded ${response.status}`);
  }
  return (await response.json()) as T;
}

export const fetchMovies = () => getJson<Movie[]>('/api/movies');

export const fetchPlaylists = () => getJson<Playlist[]>('/api/playlists');
