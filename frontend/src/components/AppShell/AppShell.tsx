import type { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import styles from './AppShell.module.css';

type AppShellProps = {
  children: ReactNode;
};

function navClass({ isActive }: { isActive: boolean }) {
  return isActive ? `${styles.navLink} ${styles.navLinkCurrent}` : styles.navLink;
}

export function AppShell({ children }: AppShellProps) {
  const { state, logout } = useAuth();

  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <div className={styles.headerInner}>
          <NavLink className={styles.wordmarkLink} to="/">
            <span className={styles.wordmark}>MovieUniverse Hub</span>
          </NavLink>

          <nav className={styles.nav} aria-label="Main">
            <NavLink className={navClass} to="/" end>
              Home
            </NavLink>
            <NavLink className={navClass} to="/catalogue">
              Catalog
            </NavLink>
            <NavLink className={navClass} to="/playlists">
              Playlists
            </NavLink>
            <NavLink className={navClass} to="/compare">
              Compare Playlists!
            </NavLink>

            {state.status === 'signed-in' ? (
              <>
                <span
                  className={styles.userBadge}
                  aria-label={`Logged in as ${state.displayName ?? state.username}`}
                >
                  {state.displayName ?? state.username}
                </span>
                <button
                  type="button"
                  className={styles.navButton}
                  onClick={() => void logout()}
                >
                  Log out
                </button>
              </>
            ) : (
              <NavLink className={navClass} to="/account">
                Log in
              </NavLink>
            )}
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
