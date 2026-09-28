import type { CatalogueFilters as Filters } from '../../utils/catalogueFilters';
import styles from './CatalogueFilters.module.css';

type CatalogueFiltersProps = {
  filters: Filters;
  genres: string[];
  matchCount: number;
  totalCount: number;
  onChange: (next: Filters) => void;
  onReset: () => void;
};

export function CatalogueFilters({
  filters,
  genres,
  matchCount,
  totalCount,
  onChange,
  onReset,
}: CatalogueFiltersProps) {
  function patch(partial: Partial<Filters>) {
    onChange({ ...filters, ...partial });
  }

  return (
    <section className={styles.panel} aria-label="Filter catalog">
      <div className={styles.row}>
        <label className={`${styles.field} ${styles.fieldWide}`}>
          Search title or overview
          <input
            className={styles.input}
            type="search"
            value={filters.search}
            onChange={(event) => patch({ search: event.target.value })}
            placeholder="e.g. Dune, Matrix…"
          />
        </label>
      </div>

      <div className={styles.row}>
        <label className={styles.field}>
          Genre
          <select
            className={styles.select}
            value={filters.genre}
            onChange={(event) => patch({ genre: event.target.value })}
          >
            <option value="">All genres</option>
            {genres.map((genre) => (
              <option key={genre} value={genre}>
                {genre}
              </option>
            ))}
          </select>
        </label>

        <label className={styles.field}>
          Year from
          <input
            className={styles.input}
            type="number"
            inputMode="numeric"
            min={1888}
            max={2100}
            value={filters.yearMin}
            onChange={(event) => patch({ yearMin: event.target.value })}
            placeholder="Any"
          />
        </label>

        <label className={styles.field}>
          Year to
          <input
            className={styles.input}
            type="number"
            inputMode="numeric"
            min={1888}
            max={2100}
            value={filters.yearMax}
            onChange={(event) => patch({ yearMax: event.target.value })}
            placeholder="Any"
          />
        </label>
      </div>

      <div className={styles.row}>
        <label className={styles.field}>
          Min combined rating
          <select
            className={styles.select}
            value={filters.minCombined}
            onChange={(event) => patch({ minCombined: event.target.value })}
          >
            <option value="">Any</option>
            <option value="6">6.0+</option>
            <option value="7">7.0+</option>
            <option value="8">8.0+</option>
            <option value="9">9.0+</option>
          </select>
        </label>

        <label className={styles.field}>
          Min TMDB rating
          <select
            className={styles.select}
            value={filters.minTmdb}
            onChange={(event) => patch({ minTmdb: event.target.value })}
          >
            <option value="">Any</option>
            <option value="6">6.0+</option>
            <option value="7">7.0+</option>
            <option value="8">8.0+</option>
            <option value="9">9.0+</option>
          </select>
        </label>

        <label className={styles.field}>
          Sort by
          <select
            className={styles.select}
            value={filters.sort}
            onChange={(event) => patch({ sort: event.target.value as Filters['sort'] })}
          >
            <option value="combined-desc">Combined rating (high first)</option>
            <option value="title-asc">Title (A–Z)</option>
            <option value="year-desc">Release year (newest)</option>
            <option value="year-asc">Release year (oldest)</option>
          </select>
        </label>

        <div className={styles.actions}>
          <button type="button" className={styles.resetButton} onClick={onReset}>
            Clear filters
          </button>
        </div>
      </div>

      <p className={styles.resultCount} role="status">
        Showing {matchCount} of {totalCount} films
      </p>
    </section>
  );
}
