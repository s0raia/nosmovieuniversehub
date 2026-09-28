import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  fetchPlaylistCompare,
  fetchPlaylists,
  type Playlist,
  type PlaylistCompare,
} from '../../api/catalogue';
import { FilmGrid } from '../../components/FilmGrid/FilmGrid';
import { MovieCard } from '../../components/MovieCard/MovieCard';
import { movieCardProps } from '../../utils/movieCard';
import page from '../../layouts/Page.module.css';
import styles from './ComparePlaylistsPage.module.css';

type LoadState =
  | { status: 'loading' }
  | { status: 'loaded'; playlists: Playlist[] }
  | { status: 'failed'; message: string };

function formatAvg(value: number | null): string {
  return value == null ? 'No scorable films' : value.toFixed(2);
}

function winnerLabel(result: PlaylistCompare): string {
  switch (result.winner) {
    case 'left':
      return `${result.left.name} ranks higher on average combined rating.`;
    case 'right':
      return `${result.right.name} ranks higher on average combined rating.`;
    case 'tie':
      return 'Both playlists tie on average combined rating.';
    default:
      return 'Neither playlist has films with a combined rating yet.';
  }
}

export function ComparePlaylistsPage() {
  const navigate = useNavigate();
  const [state, setState] = useState<LoadState>({ status: 'loading' });
  const [leftId, setLeftId] = useState('');
  const [rightId, setRightId] = useState('');
  const [compareResult, setCompareResult] = useState<PlaylistCompare | null>(null);
  const [compareError, setCompareError] = useState<string | null>(null);
  const [comparing, setComparing] = useState(false);

  useEffect(() => {
    let active = true;
    fetchPlaylists()
      .then((playlists) => {
        if (active) {
          setState({ status: 'loaded', playlists });
          if (playlists.length >= 2) {
            setLeftId(String(playlists[0].id));
            setRightId(String(playlists[1].id));
          } else if (playlists.length === 1) {
            setLeftId(String(playlists[0].id));
          }
        }
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

  async function onCompare(event: FormEvent) {
    event.preventDefault();
    setCompareError(null);
    setCompareResult(null);
    const left = Number(leftId);
    const right = Number(rightId);
    if (!Number.isFinite(left) || !Number.isFinite(right)) {
      setCompareError('Pick two playlists.');
      return;
    }
    if (left === right) {
      setCompareError('Choose two different playlists.');
      return;
    }
    setComparing(true);
    try {
      const result = await fetchPlaylistCompare(left, right);
      setCompareResult(result);
    } catch (err) {
      setCompareError(err instanceof Error ? err.message : 'Comparison failed');
    } finally {
      setComparing(false);
    }
  }

  return (
    <>
      <h1 className={page.heading}>Compare playlists</h1>
      <p className={page.lede}>
        Pick two active playlists. The winner is the one with the higher mean combined rating across
        its films (films without a combined score are skipped).
      </p>

      {state.status === 'loading' && (
        <p className={page.lede} role="status">
          Loading playlists&hellip;
        </p>
      )}

      {state.status === 'failed' && (
        <p className={page.error} role="alert">
          Could not load playlists: {state.message}
        </p>
      )}

      {state.status === 'loaded' && state.playlists.length < 2 && (
        <p className={page.lede}>Need at least two active playlists in the database to compare.</p>
      )}

      {state.status === 'loaded' && state.playlists.length >= 2 && (
        <form className={styles.form} onSubmit={onCompare}>
          <label className={styles.field}>
            First playlist
            <select
              className={styles.select}
              value={leftId}
              onChange={(event) => setLeftId(event.target.value)}
            >
              {state.playlists.map((playlist) => (
                <option key={playlist.id} value={playlist.id}>
                  {playlist.name} ({playlist.owner})
                </option>
              ))}
            </select>
          </label>
          <label className={styles.field}>
            Second playlist
            <select
              className={styles.select}
              value={rightId}
              onChange={(event) => setRightId(event.target.value)}
            >
              {state.playlists.map((playlist) => (
                <option key={playlist.id} value={playlist.id}>
                  {playlist.name} ({playlist.owner})
                </option>
              ))}
            </select>
          </label>
          <button className={styles.compareButton} type="submit" disabled={comparing}>
            {comparing ? 'Comparing…' : 'Compare'}
          </button>
        </form>
      )}

      {compareError && (
        <p className={page.error} role="alert">
          {compareError}
        </p>
      )}

      {compareResult && (
        <div className={styles.result}>
          <h2 className={styles.resultHeading}>Result</h2>
          <p className={styles.winnerBadge}>{winnerLabel(compareResult)}</p>
          <div className={styles.sideGrid}>
            <div className={styles.sideCard}>
              <p className={styles.sideName}>{compareResult.left.name}</p>
              <p className={styles.sideMeta}>
                {compareResult.left.filmCount} films · avg combined{' '}
                {formatAvg(compareResult.left.averageCombinedRating)} (
                {compareResult.left.scorableFilmCount} scored)
              </p>
            </div>
            <div className={styles.sideCard}>
              <p className={styles.sideName}>{compareResult.right.name}</p>
              <p className={styles.sideMeta}>
                {compareResult.right.filmCount} films · avg combined{' '}
                {formatAvg(compareResult.right.averageCombinedRating)} (
                {compareResult.right.scorableFilmCount} scored)
              </p>
            </div>
          </div>

          {compareResult.commonFilmCount > 0 && (
            <>
              <h3 className={styles.commonHeading}>
                Films in common ({compareResult.commonFilmCount})
              </h3>
              <FilmGrid compact>
                {compareResult.commonFilms.map((movie) => (
                  <MovieCard
                    key={movie.tmdbId}
                    {...movieCardProps(movie, false, () => {}, () =>
                      navigate(`/movies/${movie.tmdbId}`),
                    )}
                  />
                ))}
              </FilmGrid>
            </>
          )}
        </div>
      )}
    </>
  );
}
