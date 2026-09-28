import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { ensureCsrfCookie } from '../api/client';
import * as authApi from '../api/auth';

type AuthState =
  /** Session check still running; public pages must not wait on this. */
  | { status: 'checking' }
  | { status: 'anonymous' }
  | { status: 'signed-in'; username: string; displayName: string | null };

type AuthContextValue = {
  state: AuthState;
  refresh: () => Promise<void>;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: 'checking' });

  const refresh = useCallback(async () => {
    try {
      const user = await authApi.fetchCurrentUser();
      setState(
        user
          ? { status: 'signed-in', username: user.username, displayName: user.displayName }
          : { status: 'anonymous' },
      );
    } catch {
      setState({ status: 'anonymous' });
    }
  }, []);

  useEffect(() => {
    void (async () => {
      await ensureCsrfCookie();
      await refresh();
    })();
  }, [refresh]);

  const login = useCallback(
    async (username: string, password: string) => {
      const user = await authApi.login(username, password);
      setState({
        status: 'signed-in',
        username: user.username,
        displayName: user.displayName,
      });
    },
    [],
  );

  const register = useCallback(
    async (username: string, password: string) => {
      await authApi.register(username, password);
      await login(username, password);
    },
    [login],
  );

  const logout = useCallback(async () => {
    await authApi.logout();
    setState({ status: 'anonymous' });
  }, []);

  const value = useMemo(
    () => ({ state, refresh, login, register, logout }),
    [state, refresh, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
