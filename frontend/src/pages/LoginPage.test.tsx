import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { LoginPage } from './LoginPage';

vi.mock('../services/api', () => ({
  login: vi.fn().mockResolvedValue({ token: 'mock-token', user: { name: 'Operator', email: 'operator@smarttrash.local', role: 'OPERATOR' } }),
}));

describe('LoginPage', () => {
  it('submits credentials to the mocked login service', async () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    );

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'operator@smarttrash.local' } });
    fireEvent.change(screen.getByLabelText('Senha'), { target: { value: 'demo' } });
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }));

    await waitFor(() => {
      expect(screen.getByText('Acesso mockado aprovado.')).toBeInTheDocument();
    });
  });
});
