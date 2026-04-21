import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        background: '#0d0e10',
        panel: '#181a1c',
        panelHigh: '#242629',
        panelLow: '#121316',
        primary: '#85adff',
        primaryDim: '#0070eb',
        secondary: '#5ccafc',
        danger: '#ff716c',
        warning: '#fab0ff',
        muted: '#ababad',
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui'],
      },
      boxShadow: {
        glow: '0 0 32px -12px rgba(133, 173, 255, 0.65)',
      },
    },
  },
  plugins: [],
} satisfies Config;
