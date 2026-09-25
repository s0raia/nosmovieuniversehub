import { useState } from 'react';
import { AppShell } from './components/AppShell/AppShell';
import { MovieCard } from './components/MovieCard/MovieCard';
import styles from './App.module.css';

/*
  Placeholder data only, so the aesthetic and the rating states are visible
  before the API exists. The three entries deliberately cover the cases the
  briefing calls out: a well-voted film, a film TMDB has no votes for, and one
  of the duplicate-title pairs from the seed.
*/
const PREVIEW = [
  {
    title: 'Inception',
    year: 2010,
    posterUrl: null,
    tmdbAverage: 8.4,
    tmdbVotes: 38000,
    combinedScore: 8.3,
    combinedVotes: 38003,
  },
  {
    title: 'Dune',
    year: 1984,
    posterUrl: null,
    tmdbAverage: 6.3,
    tmdbVotes: 1900,
    combinedScore: 6.4,
    combinedVotes: 1901,
  },
  {
    title: 'Some Obscure Film',
    year: null,
    posterUrl: null,
    tmdbAverage: null,
    tmdbVotes: 0,
    combinedScore: null,
    combinedVotes: 0,
  },
];

export default function App() {
  const [starred, setStarred] = useState<Set<string>>(new Set());

  const toggle = (title: string) =>
    setStarred((prev) => {
      const next = new Set(prev);
      if (next.has(title)) {
        next.delete(title);
      } else {
        next.add(title);
      }
      return next;
    });

  return (
    <AppShell>
      <h1 className={styles.heading}>Search</h1>
      <p className={styles.lede}>
        UI preview with placeholder data. The API is not wired up yet.
      </p>

      <div className={styles.grid}>
        {PREVIEW.map((movie) => (
          <MovieCard
            key={movie.title}
            {...movie}
            inPlaylist={starred.has(movie.title)}
            onTogglePlaylist={() => toggle(movie.title)}
          />
        ))}
      </div>
    </AppShell>
  );
}
