import { Platform } from 'react-native';

/**
 * API base URL — resolved in priority order:
 *  1. EXPO_PUBLIC_API_BASE_URL environment variable (required for production builds)
 *  2. Platform-appropriate localhost fallback (local development only)
 *
 * Set EXPO_PUBLIC_API_BASE_URL in your .env file or CI/CD environment.
 * See frontend/.env.example for details.
 */
const DEV_FALLBACK = Platform.select({
  android: 'http://10.0.2.2:3001', // Android emulator → host machine localhost
  default: 'http://localhost:3001',
})!;

if (!process.env.EXPO_PUBLIC_API_BASE_URL && __DEV__) {
  console.warn(
    '[config] EXPO_PUBLIC_API_BASE_URL is not set. ' +
    `Using local dev fallback: ${DEV_FALLBACK}. ` +
    'Set this variable before building for production.'
  );
}

export const API_BASE: string =
  process.env.EXPO_PUBLIC_API_BASE_URL ?? DEV_FALLBACK;
