import type { ReactNode } from 'react';
import styles from './AppShell.module.css';

type AppShellProps = {
  children: ReactNode;
};

export function AppShell({ children }: AppShellProps) {
  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <div className={styles.headerInner}>
          <span className={styles.wordmark}>MovieUniverse Hub</span>
          <nav className={styles.nav} aria-label="Main">
            <a className={`${styles.navLink} ${styles.navLinkCurrent}`} href="/" aria-current="page">
              Catalogue
            </a>
            {/* Full-page hrefs 404 in the Vite SPA until routing exists (Block D). */}
            <span className={styles.navLinkSoon} title="Coming in Block D">
              Playlists
            </span>
            <span className={styles.navLinkSoon} title="Coming in Block D">
              About
            </span>
          </nav>
        </div>
      </header>

      <main className={styles.main}>{children}</main>

      <footer className={styles.footer}>
        Film data from TMDB. This product uses the TMDB API but is not endorsed
        or certified by TMDB.
      </footer>
    </div>
  );
}
