import { Eye, Lock, Mail, Trash2 } from 'lucide-react';
import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../services/api';

export function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('operator@smarttrash.local');
  const [password, setPassword] = useState('demo');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setMessage('');
    try {
      await login({ email, password });
      setMessage('Acesso mockado aprovado.');
      window.setTimeout(() => navigate('/dashboard'), 350);
    } catch {
      setMessage('Não foi possível acessar a API mockada.');
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
          <div className="mb-8 text-center">
            <h1 className="text-2xl font-black tracking-tight">Acesso operacional</h1>
            <p className="mt-2 text-xs font-semibold uppercase tracking-[0.16em] text-muted">Autenticação mockada</p>
          </div>

          <form className="space-y-5" onSubmit={handleSubmit}>
            <label className="block">
              <span className="mb-2 block text-xs font-bold uppercase tracking-[0.14em] text-muted">Email</span>
              <span className="relative block">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary" size={18} />
                <input
                  className="w-full rounded-lg border border-white/5 bg-black px-11 py-4 text-sm text-white outline-none transition focus:border-primary"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  type="email"
                />
              </span>
            </label>

            <label className="block">
              <span className="mb-2 block text-xs font-bold uppercase tracking-[0.14em] text-muted">Senha</span>
              <span className="relative block">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-secondary" size={18} />
                <input
                  className="w-full rounded-lg border border-white/5 bg-black px-11 py-4 pr-12 text-sm text-white outline-none transition focus:border-primary"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  type="password"
                />
                <Eye className="absolute right-4 top-1/2 -translate-y-1/2 text-muted" size={18} />
              </span>
            </label>

            <button
              className="w-full rounded-lg bg-gradient-to-r from-primaryDim to-primary px-6 py-4 font-black text-black transition hover:brightness-110 disabled:cursor-not-allowed disabled:opacity-70"
              disabled={loading}
              type="submit"
            >
              {loading ? 'Entrando...' : 'Entrar'}
            </button>
          </form>

          {message && <p className="mt-5 text-center text-sm font-semibold text-secondary">{message}</p>}
        </section>

        <p className="mt-6 text-center text-[11px] font-bold uppercase tracking-[0.18em] text-muted">
          Fase 1 · Leitura e visualização
        </p>
      </div>
    </main>
  );
}
