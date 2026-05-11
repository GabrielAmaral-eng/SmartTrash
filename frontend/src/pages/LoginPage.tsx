import { Eye, EyeOff, Lock, Mail, Trash2, UserRound } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';

type AuthMode = 'sign-in' | 'sign-up';

interface RouteState {
  from?: string;
}

const SIGN_UP_RATE_LIMIT_KEY = 'smart-trash:sign-up-rate-limit-until';
const SIGN_UP_RATE_LIMIT_MS = 5 * 60 * 1000;

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { loading: authLoading, session, signIn, signUp } = useAuth();
  const [mode, setMode] = useState<AuthMode>('sign-in');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [signUpRateLimitedUntil, setSignUpRateLimitedUntil] = useState(() => storedSignUpRateLimit());

  const redirectTo = (location.state as RouteState | null)?.from ?? '/dashboard';
  const signUpRateLimited = Date.now() < signUpRateLimitedUntil;

  useEffect(() => {
    if (!authLoading && session) {
      navigate(redirectTo, { replace: true });
    }
  }, [authLoading, navigate, redirectTo, session]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      if (mode === 'sign-in') {
        await signIn(email, password);
        setMessage('Acesso liberado pelo Supabase.');
        window.setTimeout(() => navigate(redirectTo, { replace: true }), 250);
        return;
      }

      if (signUpRateLimited) {
        setMessage('Criacao de conta pausada por limite do Supabase. Aguarde alguns minutos ou entre com uma conta ja criada.');
        return;
      }

      const result = await signUp(email, password, fullName);
      setMessage(result.hasSession ? 'Conta criada e sessao iniciada.' : 'Conta criada. Confirme o email antes de entrar.');
      if (result.hasSession) {
        window.setTimeout(() => navigate(redirectTo, { replace: true }), 250);
      }
    } catch (error) {
      if (isRateLimitError(error)) {
        setSignUpRateLimitedUntil(pauseSignUp());
      }
      setMessage(authErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-5 py-10 text-white">
      <div className="w-full max-w-md">
        <div className="mb-8 flex justify-center">
          <div className="flex items-center gap-3 text-primary">
            <Trash2 size={28} />
            <span className="text-2xl font-black tracking-tight">Smart Trash</span>
          </div>
        </div>

        <section className="rounded-lg border border-white/5 bg-panel/80 p-8 shadow-glow backdrop-blur">
          <div className="mb-7 text-center">
            <h1 className="text-2xl font-black tracking-tight">Acesso operacional</h1>
            <p className="mt-2 text-xs font-semibold uppercase tracking-[0.16em] text-muted">Supabase Auth</p>
          </div>

          <div className="mb-6 grid grid-cols-2 rounded-lg border border-white/5 bg-black p-1">
            <button
              className={`rounded-md px-4 py-2 text-sm font-black transition ${
                mode === 'sign-in' ? 'bg-primary text-background' : 'text-muted hover:text-white'
              }`}
              onClick={() => {
                setMode('sign-in');
                setMessage('');
              }}
              type="button"
            >
              Entrar
            </button>
            <button
              className={`rounded-md px-4 py-2 text-sm font-black transition ${
                mode === 'sign-up' ? 'bg-primary text-background' : 'text-muted hover:text-white'
              }`}
              onClick={() => {
                setMode('sign-up');
                setMessage(
                  signUpRateLimited
                    ? 'Criacao de conta pausada por limite do Supabase. Aguarde alguns minutos ou entre com uma conta ja criada.'
                    : '',
                );
              }}
              type="button"
            >
              Criar conta
            </button>
          </div>

          <form className="space-y-5" onSubmit={handleSubmit}>
            {mode === 'sign-up' && (
              <label className="block">
                <span className="mb-2 block text-xs font-bold uppercase tracking-[0.14em] text-muted">Nome</span>
                <span className="relative block">
                  <UserRound className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary" size={18} />
                  <input
                    className="w-full rounded-lg border border-white/5 bg-black px-11 py-4 text-sm text-white outline-none transition focus:border-primary"
                    value={fullName}
                    onChange={(event) => setFullName(event.target.value)}
                    placeholder="Operador Smart Trash"
                    type="text"
                  />
                </span>
              </label>
            )}

            <label className="block">
              <span className="mb-2 block text-xs font-bold uppercase tracking-[0.14em] text-muted">Email</span>
              <span className="relative block">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary" size={18} />
                <input
                  autoComplete="email"
                  className="w-full rounded-lg border border-white/5 bg-black px-11 py-4 text-sm text-white outline-none transition focus:border-primary"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  required
                  type="email"
                />
              </span>
            </label>

            <label className="block">
              <span className="mb-2 block text-xs font-bold uppercase tracking-[0.14em] text-muted">Senha</span>
              <span className="relative block">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary" size={18} />
                <input
                  autoComplete={mode === 'sign-in' ? 'current-password' : 'new-password'}
                  className="w-full rounded-lg border border-white/5 bg-black px-11 py-4 pr-12 text-sm text-white outline-none transition focus:border-primary"
                  value={password}
                  minLength={6}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                  type={showPassword ? 'text' : 'password'}
                />
                <button
                  aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
                  className="absolute right-3 top-1/2 flex h-9 w-9 -translate-y-1/2 items-center justify-center rounded-md text-muted transition hover:bg-white/5 hover:text-white"
                  onClick={() => setShowPassword((current) => !current)}
                  type="button"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </span>
            </label>

            <button
              className="w-full rounded-lg bg-gradient-to-r from-primaryDim to-primary px-6 py-4 font-black text-black transition hover:brightness-110 disabled:cursor-not-allowed disabled:opacity-70"
              disabled={loading || authLoading || (mode === 'sign-up' && signUpRateLimited)}
              type="submit"
            >
              {loading ? 'Processando...' : mode === 'sign-in' ? 'Entrar' : 'Criar conta'}
            </button>
          </form>

          {message && <p className="mt-5 text-center text-sm font-semibold text-secondary">{message}</p>}
        </section>

        <p className="mt-6 text-center text-[11px] font-bold uppercase tracking-[0.18em] text-muted">
          Banco conectado - acesso seguro
        </p>
      </div>
    </main>
  );
}

function authErrorMessage(error: unknown) {
  if (!(error instanceof Error)) {
    return 'Nao foi possivel autenticar.';
  }

  const message = error.message.toLowerCase();

  if (message.includes('invalid login credentials')) {
    return 'Email ou senha invalidos.';
  }

  if (isRateLimitError(error)) {
    return 'Muitas tentativas de cadastro em pouco tempo. Aguarde alguns minutos ou entre com uma conta ja criada.';
  }

  return error.message;
}

function isRateLimitError(error: unknown) {
  return error instanceof Error && rateLimitMessage(error.message);
}

function rateLimitMessage(message: string) {
  const normalized = message.toLowerCase();
  return normalized.includes('too many requests') || normalized.includes('rate limit') || normalized.includes('429');
}

function storedSignUpRateLimit() {
  const value = window.localStorage.getItem(SIGN_UP_RATE_LIMIT_KEY);
  return value ? Number(value) : 0;
}

function pauseSignUp() {
  const until = Date.now() + SIGN_UP_RATE_LIMIT_MS;
  window.localStorage.setItem(SIGN_UP_RATE_LIMIT_KEY, String(until));
  return until;
}
