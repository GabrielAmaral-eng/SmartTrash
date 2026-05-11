import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { LoginPage } from './LoginPage';

const authMocks = vi.hoisted(() => ({
  signIn: vi.fn(),
  signUp: vi.fn(),
}));

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    authLoading: false,
    loading: false,
    session: null,
    signIn: authMocks.signIn,
    signUp: authMocks.signUp,
  }),
}));

describe('LoginPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    authMocks.signIn.mockReset();
    authMocks.signUp.mockReset();
  });

  it('submits credentials to Supabase Auth', async () => {
    authMocks.signIn.mockResolvedValue(undefined);

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    );

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'operator@smarttrash.local' } });
    fireEvent.change(screen.getByLabelText('Senha'), { target: { value: 'demo123' } });
    fireEvent.click(screen.getAllByRole('button', { name: 'Entrar' })[1]);

    await waitFor(() => {
      expect(authMocks.signIn).toHaveBeenCalledWith('operator@smarttrash.local', 'demo123');
      expect(screen.getByText('Acesso liberado pelo Supabase.')).toBeInTheDocument();
    });
  });

  it('creates a new Supabase Auth account', async () => {
    authMocks.signUp.mockResolvedValue({ hasSession: false });

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    );

    fireEvent.click(screen.getAllByRole('button', { name: 'Criar conta' })[0]);
    fireEvent.change(screen.getByLabelText('Nome'), { target: { value: 'Operador Smart Trash' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'new@smarttrash.local' } });
    fireEvent.change(screen.getByLabelText('Senha'), { target: { value: 'demo123' } });
    fireEvent.click(screen.getAllByRole('button', { name: 'Criar conta' })[1]);

    await waitFor(() => {
      expect(authMocks.signUp).toHaveBeenCalledWith('new@smarttrash.local', 'demo123', 'Operador Smart Trash');
      expect(screen.getByText('Conta criada. Confirme o email antes de entrar.')).toBeInTheDocument();
    });
  });

  it('shows a friendly message when Supabase blocks signup attempts', async () => {
    authMocks.signUp.mockRejectedValue(new Error('Too Many Requests'));

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    );

    fireEvent.click(screen.getAllByRole('button', { name: 'Criar conta' })[0]);
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'new@smarttrash.local' } });
    fireEvent.change(screen.getByLabelText('Senha'), { target: { value: 'demo123' } });
    fireEvent.click(screen.getAllByRole('button', { name: 'Criar conta' })[1]);

    await waitFor(() => {
      expect(screen.getByText('Muitas tentativas de cadastro em pouco tempo. Aguarde alguns minutos ou entre com uma conta ja criada.')).toBeInTheDocument();
    });

    fireEvent.click(screen.getAllByRole('button', { name: 'Criar conta' })[1]);
    expect(authMocks.signUp).toHaveBeenCalledTimes(1);
  });
});
