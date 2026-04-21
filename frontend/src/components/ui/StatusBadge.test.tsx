import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { StatusBadge } from './StatusBadge';

describe('StatusBadge', () => {
  it('renders a readable label for each backend status', () => {
    render(
      <div>
        <StatusBadge status="EMPTY" />
        <StatusBadge status="ATTENTION" />
        <StatusBadge status="FULL" />
      </div>,
    );

    expect(screen.getByText('Vazia')).toBeInTheDocument();
    expect(screen.getByText('Atenção')).toBeInTheDocument();
    expect(screen.getByText('Cheia')).toBeInTheDocument();
  });
});
