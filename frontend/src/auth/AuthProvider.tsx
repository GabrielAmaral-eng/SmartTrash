import type { Session, User } from '@supabase/supabase-js';
import { createContext, ReactNode, useContext, useEffect, useMemo, useState } from 'react';
import type { Profile } from '../types/api';
import { fetchCurrentProfile } from '../services/api';
import { supabase } from '../services/supabase';

interface SignUpResult {
  hasSession: boolean;
}

interface AuthContextValue {
  loading: boolean;
  profile: Profile | null;
  session: Session | null;
  signIn: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  signUp: (email: string, password: string, fullName: string) => Promise<SignUpResult>;
  user: User | null;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function syncSession(currentSession: Session | null) {
      if (!active) {
        return;
      }

      setSession(currentSession);
      if (!currentSession?.user) {
        setProfile(null);
        setLoading(false);
        return;
      }

      await loadProfile();
      if (active) {
        setLoading(false);
      }
    }

    supabase.auth.getSession().then(({ data }) => syncSession(data.session));

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, nextSession) => {
      void syncSession(nextSession);
    });

    return () => {
      active = false;
      subscription.unsubscribe();
    };
  }, []);

  async function loadProfile() {
    setProfile(await fetchCurrentProfile());
  }

  async function signIn(email: string, password: string) {
    const { error } = await supabase.auth.signInWithPassword({ email, password });

    if (error) {
      throw error;
    }
  }

  async function signUp(email: string, password: string, fullName: string): Promise<SignUpResult> {
    const { data, error } = await supabase.auth.signUp({
      email,
      password,
      options: {
        data: {
          full_name: fullName.trim() || email.split('@')[0],
        },
      },
    });

    if (error) {
      throw error;
    }

    return { hasSession: Boolean(data.session) };
  }

  async function signOut() {
    const { error } = await supabase.auth.signOut();

    if (error) {
      throw error;
    }

    setProfile(null);
    setSession(null);
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      loading,
      profile,
      session,
      signIn,
      signOut,
      signUp,
      user: session?.user ?? null,
    }),
    [loading, profile, session],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }

  return context;
}
