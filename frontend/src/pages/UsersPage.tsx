import { ShieldCheck, UserCog } from 'lucide-react';
import { useEffect, useState } from 'react';
import { SectionPanel } from '../components/ui/SectionPanel';
import { fetchUsers, updateUserRole } from '../services/api';
import type { Profile, UserRole } from '../types/api';

const roleLabels: Record<UserRole, string> = {
  SUPER_ADMIN: 'Super-Admin',
  ADMIN: 'Admin',
  OPERATOR: 'Operador',
  VIEWER: 'Visualizador',
};

const editableRoles: UserRole[] = ['ADMIN', 'OPERATOR', 'VIEWER'];

export function UsersPage() {
  const [users, setUsers] = useState<Profile[]>([]);
  const [error, setError] = useState('');
  const [savingUserId, setSavingUserId] = useState('');

  useEffect(() => {
    fetchUsers()
      .then((response) => setUsers(response.users))
      .catch(() => setError('Nao foi possivel carregar usuarios. Apenas Super-Admin pode acessar esta area.'));
  }, []);

  async function handleRoleChange(user: Profile, role: UserRole) {
    setSavingUserId(user.id);
    setError('');
    try {
      const updated = await updateUserRole(user.id, { role });
      setUsers((current) => current.map((item) => (item.id === updated.id ? updated : item)));
    } catch {
      setError('Nao foi possivel atualizar o perfil deste usuario.');
    } finally {
      setSavingUserId('');
    }
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-col justify-between gap-4 md:flex-row md:items-end">
        <div>
          <h1 className="text-4xl font-black tracking-tight text-white">Usuários</h1>
        </div>
        <div className="flex items-center gap-2 rounded-lg border border-white/5 bg-panel/70 px-4 py-3 text-xs font-bold uppercase tracking-[0.14em] text-muted">
          <ShieldCheck size={16} className="text-secondary" />
          Super-Admin
        </div>
      </header>

      {error && <p className="rounded-lg bg-danger/10 p-4 text-danger">{error}</p>}

      <SectionPanel title="Controle de acesso">
        <div className="grid gap-4">
          {users.map((user) => (
            <article key={user.id} className="rounded-lg border border-white/5 bg-black/30 p-5">
              <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                <div className="flex min-w-0 items-start gap-4">
                  <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-primaryDim/15 text-primary">
                    <UserCog size={20} />
                  </div>
                  <div className="min-w-0">
                    <h2 className="truncate text-lg font-black text-white">{user.fullName ?? user.email}</h2>
                    <p className="mt-1 truncate text-sm text-muted">{user.email}</p>
                  </div>
                </div>

                {user.role === 'SUPER_ADMIN' ? (
                  <span className="rounded-lg border border-primary/25 bg-primaryDim/10 px-4 py-2 text-sm font-black text-primary">
                    {roleLabels[user.role]}
                  </span>
                ) : (
                  <label className="block min-w-52">
                    <span className="mb-2 block text-xs font-bold uppercase tracking-[0.14em] text-muted">Perfil</span>
                    <select
                      className="w-full rounded-lg border border-white/5 bg-panel px-4 py-3 text-sm font-bold text-white outline-none transition focus:border-primary"
                      disabled={savingUserId === user.id}
                      value={user.role}
                      onChange={(event) => handleRoleChange(user, event.target.value as UserRole)}
                    >
                      {editableRoles.map((role) => (
                        <option key={role} value={role}>
                          {roleLabels[role]}
                        </option>
                      ))}
                    </select>
                  </label>
                )}
              </div>
            </article>
          ))}

          {!users.length && !error && <p className="rounded-lg bg-black/30 p-5 text-sm text-muted">Carregando usuarios...</p>}
        </div>
      </SectionPanel>
    </div>
  );
}
