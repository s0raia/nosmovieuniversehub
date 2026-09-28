import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import page from '../../layouts/Page.module.css';
import styles from './AuthAccountPage.module.css';

type Mode = 'sign-in' | 'register';

function resolveMode(pathname: string, params: URLSearchParams): Mode {
  if (pathname === '/register' || params.get('mode') === 'register') {
    return 'register';
  }
  return 'sign-in';
}

export function AuthAccountPage() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const mode = resolveMode(location.pathname, searchParams);

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setPasswordConfirm] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function switchMode(next: Mode) {
    setError(null);
    setSearchParams(next === 'register' ? { mode: 'register' } : {}, { replace: true });
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    if (mode === 'register') {
      if (password !== confirm) {
        setError('Passwords do not match');
        return;
      }
      if (password.length < 6) {
        setError('Password must be at least 6 characters');
        return;
      }
    }

    setSubmitting(true);
    try {
      if (mode === 'sign-in') {
        await login(username.trim(), password);
      } else {
        await register(username.trim(), password);
      }
      navigate('/playlists');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong');
    } finally {
      setSubmitting(false);
    }
  }

  const isSignIn = mode === 'sign-in';

  return (
    <>
      <h1 className={page.heading}>{isSignIn ? 'Log in' : 'Create account'}</h1>
      <p className={page.lede}>
        {isSignIn
          ? 'Use a demo account (ana, bruno, carla, mock-01, and others) or one you registered.'
          : 'Pick a username and password. You can open your playlists right after.'}
      </p>

      <div className={styles.modeSwitch} role="tablist" aria-label="Account action">
        <button
          type="button"
          role="tab"
          aria-selected={isSignIn}
          className={`${styles.modeButton} ${isSignIn ? styles.modeButtonActive : ''}`}
          onClick={() => switchMode('sign-in')}
        >
          Log in
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={!isSignIn}
          className={`${styles.modeButton} ${!isSignIn ? styles.modeButtonActive : ''}`}
          onClick={() => switchMode('register')}
        >
          Sign up
        </button>
      </div>

      <form className={page.form} onSubmit={onSubmit}>
        <label className={page.label}>
          Username
          <input
            className={page.input}
            name="username"
            autoComplete="username"
            required
            minLength={isSignIn ? 1 : 2}
            value={username}
            onChange={(event) => setUsername(event.target.value)}
          />
        </label>

        <label className={page.label}>
          Password
          <input
            className={page.input}
            name="password"
            type="password"
            autoComplete={isSignIn ? 'current-password' : 'new-password'}
            required
            minLength={isSignIn ? 1 : 6}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>

        {!isSignIn && (
          <label className={page.label}>
            Confirm password
            <input
              className={page.input}
              name="confirm"
              type="password"
              autoComplete="new-password"
              required
              minLength={6}
              value={confirm}
              onChange={(event) => setPasswordConfirm(event.target.value)}
            />
          </label>
        )}

        {error && (
          <p className={page.formError} role="alert">
            {error}
          </p>
        )}

        <button className={page.submitButton} type="submit" disabled={submitting}>
          {submitting
            ? isSignIn
              ? 'Logging in…'
              : 'Creating account…'
            : isSignIn
              ? 'Log in'
              : 'Create account'}
        </button>
      </form>

      <p className={page.linkRow}>
        {isSignIn ? (
          <>
            New here?{' '}
            <button type="button" className={styles.inlineLink} onClick={() => switchMode('register')}>
              Create an account
            </button>
          </>
        ) : (
          <>
            Already have an account?{' '}
            <button type="button" className={styles.inlineLink} onClick={() => switchMode('sign-in')}>
              Log in
            </button>
          </>
        )}{' '}
        or <Link className={page.textLink} to="/">back to home</Link>
      </p>
    </>
  );
}
