import { useEffect, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchMovie, fetchMyRating, saveMyRating, type MovieDetail } from '../../api/catalogue';
import { useAuth } from '../../contexts/AuthContext';
import { useStarred } from '../../contexts/StarredContext';
import page from '../../layouts/Page.module.css';
import styles from './MovieDetailPage.module.css';

type LoadState =
  | { status: 'loading' }
  | { status: 'loaded'; movie: MovieDetail }
  | { status: 'failed'; message: string };

function formatVotes(votes: number): string {
  return votes.toLocaleString('en-US');
}

function formatRuntime(minutes: number | null): string | null {
  if (minutes == null || minutes <= 0) {
    return null;
  }
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hours === 0) {
    return `${mins} min`;
  }
  return mins > 0 ? `${hours} h ${mins} min` : `${hours} h`;
}

export function MovieDetailPage() {
  const { tmdbId: tmdbIdParam } = useParams();
  const tmdbId = Number(tmdbIdParam);
  const { state: authState } = useAuth();
  const { isStarred, toggleStarred } = useStarred();
  const [state, setState] = useState<LoadState>({ status: 'loading' });
  const [myStars, setMyStars] = useState<number | ''>('');
  const [ratingMessage, setRatingMessage] = useState<string | null>(null);
  const [savingRating, setSavingRating] = useState(false);

  useEffect(() => {
    if (!Number.isFinite(tmdbId)) {
      setState({ status: 'failed', message: 'Invalid film id' });
      return;
    }

    let active = true;
    setState({ status: 'loading' });

    fetchMovie(tmdbId)
      .then((movie) => {
        if (active) setState({ status: 'loaded', movie });
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
  }, [tmdbId]);

  useEffect(() => {
    if (authState.status !== 'signed-in' || !Number.isFinite(tmdbId)) {
      setMyStars('');
      return;
    }
    let active = true;
    fetchMyRating(tmdbId)
      .then((payload) => {
        if (active) {
          setMyStars(payload.stars ?? '');
        }
      })
      .catch(() => {
        if (active) setMyStars('');
      });
    return () => {
      active = false;
    };
  }, [authState.status, tmdbId]);

  async function onSaveRating(event: FormEvent) {
    event.preventDefault();
    if (authState.status !== 'signed-in' || myStars === '') {
      return;
    }
    setSavingRating(true);
    setRatingMessage(null);
    try {
      await saveMyRating(tmdbId, Number(myStars));
      setRatingMessage('Rating saved.');
      const movie = await fetchMovie(tmdbId);
      setState({ status: 'loaded', movie });
    } catch (err) {
      setRatingMessage(err instanceof Error ? err.message : 'Could not save rating');
    } finally {
      setSavingRating(false);
    }
  }

  const displayTitle =
    state.status === 'loaded' && state.movie.title
      ? state.movie.releaseYear
        ? `${state.movie.title} (${state.movie.releaseYear})`
        : state.movie.title
      : null;

  return (
    <>
      <Link className={styles.backLink} to="/">
        &larr; Back to home
      </Link>

      {state.status === 'loading' && (
        <p className={page.lede} role="status">
          Loading film details&hellip;
        </p>
      )}

      {state.status === 'failed' && (
        <p className={page.error} role="alert">
          Could not load this film: {state.message}
        </p>
      )}

      {state.status === 'loaded' && (
        <article className={styles.layout}>
          <img
            className={styles.poster}
            src={state.movie.posterUrl ?? ''}
            alt={state.movie.posterUrl ? `Poster for ${state.movie.title ?? 'film'}` : ''}
          />

          <div>
            <h1 className={styles.title}>{displayTitle ?? 'Untitled'}</h1>

            <p className={styles.meta}>
              {[
                state.movie.originalTitle &&
                state.movie.originalTitle !== state.movie.title &&
                `Original title: ${state.movie.originalTitle}`,
                formatRuntime(state.movie.runtimeMinutes),
              ]
                .filter(Boolean)
                .join(' · ')}
            </p>

            {state.movie.genres.length > 0 && (
              <ul className={styles.genreList} aria-label="Genres">
                {state.movie.genres.map((genre) => (
                  <li key={genre} className={styles.genre}>
                    {genre}
                  </li>
                ))}
              </ul>
            )}

            {(state.movie.spokenLanguages?.length ?? 0) > 0 && (
              <p className={styles.languages}>
                <span className={styles.languagesLabel}>Languages spoken</span>
                {': '}
                {state.movie.spokenLanguages.join(', ')}
              </p>
            )}

            <div className={styles.ratings}>
              {state.movie.tmdbVoteAverage === null || state.movie.tmdbVoteCount === 0 ? (
                <span className={`${styles.badge} ${styles.badgeMuted}`}>TMDB: no votes</span>
              ) : (
                <span className={`${styles.badge} ${styles.badgeTmdb}`}>
                  TMDB {Number(state.movie.tmdbVoteAverage).toFixed(1)} ·{' '}
                  {formatVotes(state.movie.tmdbVoteCount)} votes
                </span>
              )}

              {state.movie.localVoteAverage === null || state.movie.localVoteCount === 0 ? (
                <span className={`${styles.badge} ${styles.badgeMuted}`}>No local ratings yet</span>
              ) : (
                <span className={`${styles.badge} ${styles.badgeLocal}`}>
                  MovieUniverse {Number(state.movie.localVoteAverage).toFixed(1)} ·{' '}
                  {formatVotes(state.movie.localVoteCount)} ratings
                </span>
              )}

              {state.movie.combinedRating === null ? (
                <span className={`${styles.badge} ${styles.badgeMuted}`}>Combined score unavailable</span>
              ) : (
                <span className={`${styles.badge} ${styles.badgeCombined}`}>
                  Combined {Number(state.movie.combinedRating).toFixed(1)}
                </span>
              )}
            </div>

            {state.movie.overview ? (
              <p className={styles.overview}>{state.movie.overview}</p>
            ) : (
              <p className={styles.overview}>No overview from TMDB yet.</p>
            )}

            {authState.status === 'signed-in' && (
              <div className={styles.userActions}>
                <button
                  type="button"
                  className={`${styles.starAction} ${isStarred(tmdbId) ? styles.starActionActive : ''}`}
                  onClick={() => void toggleStarred(tmdbId)}
                  aria-pressed={isStarred(tmdbId)}
                >
                  {isStarred(tmdbId) ? '★ In Starred picks' : '☆ Add to Starred picks'}
                </button>

                <form className={styles.rateForm} onSubmit={onSaveRating}>
                  <label className={styles.rateLabel}>
                    Your rating (1–10)
                    <select
                      className={styles.rateSelect}
                      value={myStars}
                      onChange={(event) =>
                        setMyStars(event.target.value === '' ? '' : Number(event.target.value))
                      }
                    >
                      <option value="">Not rated</option>
                      {Array.from({ length: 10 }, (_, index) => index + 1).map((value) => (
                        <option key={value} value={value}>
                          {value}
                        </option>
                      ))}
                    </select>
                  </label>
                  <button className={styles.rateButton} type="submit" disabled={savingRating || myStars === ''}>
                    {savingRating ? 'Saving…' : 'Save rating'}
                  </button>
                </form>
                {ratingMessage && (
                  <p className={styles.rateMessage} role="status">
                    {ratingMessage}
                  </p>
                )}
              </div>
            )}
          </div>
        </article>
      )}
    </>
  );
}
