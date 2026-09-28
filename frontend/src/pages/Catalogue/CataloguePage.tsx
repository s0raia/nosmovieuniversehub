import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchMovies, type Movie } from '../../api/catalogue';
import { CatalogueFilters } from '../../components/CatalogueFilters/CatalogueFilters';
import { FilmGrid } from '../../components/FilmGrid/FilmGrid';
import { MovieCard } from '../../components/MovieCard/MovieCard';
import { PlaylistFilmDialog } from '../../components/PlaylistFilmDialog/PlaylistFilmDialog';
import { useAuth } from '../../contexts/AuthContext';
import { useMyPlaylists } from '../../contexts/MyPlaylistsContext';
import {
  collectGenres,
  defaultCatalogueFilters,
  filterAndSortMovies,
  type CatalogueFilters as Filters,
} from '../../utils/catalogueFilters';
import { movieCardProps } from '../../utils/movieCard';
import page from '../../layouts/Page.module.css';

type LoadState =
  | { status: 'loading' }
  | { status: 'loaded'; movies: Movie[] }
  | { status: 'failed'; message: string };

export function CataloguePage() {
  const navigate = useNavigate();
  const { state: authState } = useAuth();
  const { isInAnyPlaylist } = useMyPlaylists();
  const showPlaylistStar = authState.status === 'signed-in';
  const [state, setState] = useState<LoadState>({ status: 'loading' });
  const [filters, setFilters] = useState<Filters>(defaultCatalogueFilters);
  const [picker, setPicker] = useState<{ tmdbId: number; label: string } | null>(null);

  useEffect(() => {
    let active = true;
    fetchMovies()
      .then((movies) => {
        if (active) setState({ status: 'loaded', movies });
      })
      .catch((error: unknown) => {
        if (active) {
          setState({
            status: 'failed',
            message: error instanceof Error ? error.message : 'Unknown error',
          });
        }
      });
    return () => {
      active = false;
    };
  }, []);

  const genres = useMemo(
    () => (state.status === 'loaded' ? collectGenres(state.movies) : []),
    [state],
  );

  const visibleMovies = useMemo(
    () => (state.status === 'loaded' ? filterAndSortMovies(state.movies, filters) : []),
    [state, filters],
  );

  return (
    <>
      <h1 className={page.heading}>Catalog</h1>
      <p className={page.lede}>
        Browse every film in MovieUniverse, open details without logging in, and narrow the list with
        the filters below.
      </p>

      {state.status === 'loading' && (
        <p className={page.lede} role="status">
          Loading films&hellip;
        </p>
      )}

      {state.status === 'failed' && (
        <p className={page.error} role="alert">
          Could not load films: {state.message}. Is the backend running on port 8080?
        </p>
      )}

      {state.status === 'loaded' && (
        <>
          <CatalogueFilters
            filters={filters}
            genres={genres}
            matchCount={visibleMovies.length}
            totalCount={state.movies.length}
            onChange={setFilters}
            onReset={() => setFilters(defaultCatalogueFilters)}
          />

          {visibleMovies.length === 0 ? (
            <p className={page.lede}>No films match these filters. Try clearing or loosening them.</p>
          ) : (
            <FilmGrid>
              {visibleMovies.map((movie) => (
                <MovieCard
                  key={movie.tmdbId}
                  {...movieCardProps(
                    movie,
                    isInAnyPlaylist(movie.tmdbId),
                    () => {
                      const label = movie.releaseYear
                        ? `${movie.title ?? 'Untitled'} (${movie.releaseYear})`
                        : (movie.title ?? 'Untitled');
                      setPicker({ tmdbId: movie.tmdbId, label });
                    },
                    () => navigate(`/movies/${movie.tmdbId}`),
                    showPlaylistStar,
                  )}
                />
              ))}
            </FilmGrid>
          )}
        </>
      )}

      {picker && (
        <PlaylistFilmDialog
          tmdbId={picker.tmdbId}
          filmLabel={picker.label}
          onClose={() => setPicker(null)}
        />
      )}
    </>
  );
}
