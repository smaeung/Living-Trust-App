import { Platform } from 'react-native';

/**
 * API base URL — resolved in priority order:
 *  1. EXPO_PUBLIC_API_BASE_URL environment variable (set for production builds)
 *  2. Platform default for local development:
 *     - Android emulator: 10.0.2.2 routes to the host machine's localhost
 *     - iOS simulator / web: localhost
 *
 * Set EXPO_PUBLIC_API_BASE_URL in your .env file or CI environment before
 * building for production. Never hardcode production URLs here.
 */
export const API_BASE: string =
  process.env.EXPO_PUBLIC_API_BASE_URL ??
  Platform.select({
    android: 'http://10.0.2.2:3001',
    default: 'http://localhost:3001',
  })!;
