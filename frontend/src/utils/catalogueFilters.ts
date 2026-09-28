import type { Movie } from '../types/catalogue';

export type CatalogueSort = 'combined-desc' | 'title-asc' | 'year-desc' | 'year-asc';

export type CatalogueFilters = {
  search: string;
  genre: string;
  yearMin: string;
  yearMax: string;
  minCombined: string;
  minTmdb: string;
  sort: CatalogueSort;
};

export const defaultCatalogueFilters: CatalogueFilters = {
  search: '',
  genre: '',
  yearMin: '',
  yearMax: '',
  minCombined: '',
  minTmdb: '',
  sort: 'combined-desc',
};

export function collectGenres(movies: Movie[]): string[] {
  const names = new Set<string>();
  for (const movie of movies) {
    for (const genre of movie.genres ?? []) {
      names.add(genre);
    }
  }
  return [...names].sort((a, b) => a.localeCompare(b));
}

function parseOptionalInt(value: string): number | null {
  const trimmed = value.trim();
  if (!trimmed) {
    return null;
  }
  const parsed = Number.parseInt(trimmed, 10);
  return Number.isFinite(parsed) ? parsed : null;
}

function parseOptionalMinRating(value: string): number | null {
  const trimmed = value.trim();
  if (!trimmed) {
    return null;
  }
  const parsed = Number.parseFloat(trimmed);
  return Number.isFinite(parsed) ? parsed : null;
}

function matchesSearch(movie: Movie, search: string): boolean {
  const query = search.trim().toLowerCase();
  if (!query) {
    return true;
  }
  const title = (movie.title ?? '').toLowerCase();
  const overview = (movie.overview ?? '').toLowerCase();
  return title.includes(query) || overview.includes(query);
}

export function filterAndSortMovies(movies: Movie[], filters: CatalogueFilters): Movie[] {
  const yearMin = parseOptionalInt(filters.yearMin);
  const yearMax = parseOptionalInt(filters.yearMax);
  const minCombined = parseOptionalMinRating(filters.minCombined);
  const minTmdb = parseOptionalMinRating(filters.minTmdb);

  let result = movies.filter((movie) => {
    if (!matchesSearch(movie, filters.search)) {
      return false;
    }
    if (filters.genre && !(movie.genres ?? []).includes(filters.genre)) {
      return false;
    }
    if (yearMin != null && (movie.releaseYear == null || movie.releaseYear < yearMin)) {
      return false;
    }
    if (yearMax != null && (movie.releaseYear == null || movie.releaseYear > yearMax)) {
      return false;
    }
    if (minCombined != null) {
      if (movie.combinedRating == null || movie.combinedRating < minCombined) {
        return false;
      }
    }
    if (minTmdb != null) {
      if (movie.tmdbVoteAverage == null || movie.tmdbVoteAverage < minTmdb) {
        return false;
      }
    }
    return true;
  });

  result = [...result].sort((a, b) => compareMovies(a, b, filters.sort));
  return result;
}

function compareMovies(a: Movie, b: Movie, sort: CatalogueSort): number {
  switch (sort) {
    case 'title-asc':
      return (a.title ?? '').localeCompare(b.title ?? '', undefined, { sensitivity: 'base' });
    case 'year-desc':
      return (b.releaseYear ?? -1) - (a.releaseYear ?? -1);
    case 'year-asc':
      return (a.releaseYear ?? 9999) - (b.releaseYear ?? 9999);
    case 'combined-desc':
    default:
      return (b.combinedRating ?? -1) - (a.combinedRating ?? -1);
  }
}
