import styles from './MovieCard.module.css';

export type MovieCardProps = {
  title: string;
  /** Null for films TMDB has no release date for. */
  year: number | null;
  posterUrl: string | null;
  /** Null when TMDB has no votes. Never send 0 to mean "unrated". */
  tmdbAverage: number | null;
  tmdbVotes: number;
  /** Null when neither TMDB nor local users have voted. */
  combinedScore: number | null;
  combinedVotes: number;
  inPlaylist: boolean;
  onTogglePlaylist: () => void;
};

function formatVotes(votes: number): string {
  return votes.toLocaleString('en-GB');
}

export function MovieCard({
  title,
  year,
  posterUrl,
  tmdbAverage,
  tmdbVotes,
  combinedScore,
  combinedVotes,
  inPlaylist,
  onTogglePlaylist,
}: MovieCardProps) {
  const label = year ? `${title} (${year})` : title;

  return (
    <article className={styles.card}>
      <img
        className={styles.poster}
        src={posterUrl ?? ''}
        alt={posterUrl ? `Poster for ${label}` : ''}
        loading="lazy"
      />

      <button
        type="button"
        className={`${styles.starButton} ${inPlaylist ? styles.starButtonActive : ''}`}
        onClick={onTogglePlaylist}
        aria-pressed={inPlaylist}
        aria-label={inPlaylist ? `Remove ${label} from playlist` : `Add ${label} to playlist`}
      >
        {inPlaylist ? '\u2605' : '\u2606'}
      </button>

      <div className={styles.body}>
        <h3 className={styles.title}>{title}</h3>
        <span className={styles.year}>{year ?? 'Year unknown'}</span>

        <div className={styles.ratings}>
          {tmdbAverage === null || tmdbVotes === 0 ? (
            <span className={`${styles.badge} ${styles.badgeNoVotes}`}>TMDB: no votes</span>
          ) : (
            <span className={`${styles.badge} ${styles.badgeTmdb}`}>
              TMDB {tmdbAverage.toFixed(1)} &middot; {formatVotes(tmdbVotes)} votes
            </span>
          )}

          {combinedScore === null ? (
            <span className={`${styles.badge} ${styles.badgeNoVotes}`}>Not enough information</span>
          ) : (
            <span className={`${styles.badge} ${styles.badgeCombined}`}>
              Combined {combinedScore.toFixed(1)} &middot; {formatVotes(combinedVotes)} votes
            </span>
          )}
        </div>
      </div>
    </article>
  );
}
