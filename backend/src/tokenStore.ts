/**
 * tokenStore.ts
 *
 * Shared in-memory store for download tokens.
 * Imported by both pdfRoutes and paymentRoutes so they share the same Map
 * without any internal HTTP calls.
 *
 * Replace with Redis or a database table before going to production.
 */

import crypto from 'crypto';
import { TrustData } from './templates/livingTrustTemplates';

interface TokenEntry {
  trustData: TrustData;
  expiresAt: number;
}

export const downloadTokens = new Map<string, TokenEntry>();

const TOKEN_TTL_MS = 24 * 60 * 60 * 1000; // 24 hours

/**
 * Creates a secure random download token, stores it with the trust data,
 * and returns the token + expiry timestamp.
 */
export function issueDownloadToken(
  trustData: TrustData,
  _paymentIntentId: string
): { downloadToken: string; expiresAt: string } {
  const token = crypto.randomBytes(32).toString('hex');
  const expiresAt = Date.now() + TOKEN_TTL_MS;
  downloadTokens.set(token, { trustData, expiresAt });
  return { downloadToken: token, expiresAt: new Date(expiresAt).toISOString() };
}
