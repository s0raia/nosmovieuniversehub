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
  genres: string[];
};

export type MovieDetail = Movie & {
  originalTitle: string | null;
  runtimeMinutes: number | null;
  originalLanguage: string | null;
  spokenLanguages: string[];
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

export type PlaylistCompareSide = {
  playlistId: number;
  name: string;
  owner: string;
  filmCount: number;
  averageCombinedRating: number | null;
  scorableFilmCount: number;
};

export type PlaylistCompare = {
  left: PlaylistCompareSide;
  right: PlaylistCompareSide;
  winner: 'left' | 'right' | 'tie' | 'insufficient';
  commonFilmCount: number;
  commonFilms: Movie[];
};
