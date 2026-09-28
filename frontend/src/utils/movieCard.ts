import type { Movie } from '../api/catalogue';
import type { MovieCardProps } from '../components/MovieCard/MovieCard';

export function movieCardProps(
  movie: Movie,
  inPlaylist: boolean,
  onTogglePlaylist: () => void,
  onOpen?: () => void,
): MovieCardProps {
  return {
    tmdbId: movie.tmdbId,
    title: movie.title ?? 'Untitled',
    year: movie.releaseYear,
    posterUrl: movie.posterUrl,
    tmdbAverage: movie.tmdbVoteAverage,
    tmdbVotes: movie.tmdbVoteCount,
    combinedScore: movie.combinedRating,
    combinedVotes: movie.tmdbVoteCount + movie.localVoteCount,
    inPlaylist,
    onTogglePlaylist,
    onOpen,
  };
}
