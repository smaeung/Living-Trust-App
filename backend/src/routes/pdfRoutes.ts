import { Router, Request, Response } from 'express';
import PDFDocument from 'pdfkit';
import { getTemplate, TrustData, TrustDocumentContent, ALL_STATES } from '../templates/livingTrustTemplates';
import { downloadTokens, issueDownloadToken } from '../tokenStore';

const router = Router();

// ─────────────────────────────────────────────────────────────────────────────
// PDF BUILDER — shared logic for watermarked and clean PDFs
// ─────────────────────────────────────────────────────────────────────────────

function buildPdf(
  trustData: TrustData,
  content: TrustDocumentContent,
  watermark: boolean
): Buffer {
  return new Promise<Buffer>((resolve, reject) => {
    const doc = new PDFDocument({ size: 'LETTER', margins: { top: 72, bottom: 72, left: 72, right: 72 } });
    const chunks: Buffer[] = [];

    doc.on('data', (chunk: Buffer) => chunks.push(chunk));
    doc.on('end', () => resolve(Buffer.concat(chunks)));
    doc.on('error', reject);

    const PRIMARY = '#1a365d';
    const GRAY = '#718096';
    const PAGE_WIDTH = doc.page.width;
    const PAGE_HEIGHT = doc.page.height;
    const CONTENT_WIDTH = PAGE_WIDTH - 144; // minus left+right margins

    // ── Helper: add watermark to current page ──
    const addWatermark = () => {
      if (!watermark) return;
      doc.save();
      doc.rotate(-45, { origin: [PAGE_WIDTH / 2, PAGE_HEIGHT / 2] });
      doc.fontSize(52)
        .fillColor('#e2e8f0')
        .opacity(0.55)
        .text('PREVIEW — NOT FOR LEGAL USE', 0, PAGE_HEIGHT / 2 - 60, {
          align: 'center',
          width: PAGE_WIDTH,
        });
      doc.opacity(0.35).fontSize(24).text('livingtrust.app', 0, PAGE_HEIGHT / 2 + 10, {
        align: 'center',
        width: PAGE_WIDTH,
      });
      doc.restore();
      doc.opacity(1);
    };

    // ── TITLE PAGE ──
    // Header bar
    doc.rect(0, 0, PAGE_WIDTH, 6).fill(PRIMARY);

    doc.moveDown(3);
    doc.fontSize(10).fillColor(GRAY).text('LEGAL DOCUMENT', { align: 'center' });
    doc.moveDown(1);

    // Trust title (split on newlines)
    const titleLines = content.title.split('\n');
    doc.fontSize(20).fillColor(PRIMARY).font('Helvetica-Bold');
    titleLines.forEach((line, i) => {
      doc.text(line, { align: 'center' });
      if (i < titleLines.length - 1) doc.moveDown(0.3);
    });

    doc.moveDown(2);
    doc.moveTo(72, doc.y).lineTo(PAGE_WIDTH - 72, doc.y).stroke(PRIMARY);
    doc.moveDown(1);

    // Trust summary card
    doc.fontSize(11).fillColor('#2d3748').font('Helvetica-Bold').text('DOCUMENT SUMMARY', 72, doc.y);
    doc.moveDown(0.5);
    doc.fontSize(10).font('Helvetica').fillColor('#4a5568');
    doc.text(`Grantor: ${trustData.grantor}`, 72);
    doc.text(`Trustee: ${trustData.trustee}`, 72);
    doc.text(`Successor Trustee: ${trustData.successorTrustee}`, 72);
    doc.text(`Beneficiaries: ${trustData.beneficiaries.join(', ') || 'As designated'}`, 72);
    doc.text(`Date: ${new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}`, 72);
    doc.text(`Governing Law: ${getTemplate(trustData.state).governingLaw}`, 72);

    doc.moveDown(2);

    // Disclaimer
    doc.rect(72, doc.y, CONTENT_WIDTH, 60).fill('#fff5f5');
    doc.fontSize(8).fillColor('#c53030').font('Helvetica-Bold')
      .text('LEGAL DISCLAIMER', 78, doc.y - 56);
    doc.font('Helvetica').fontSize(8).fillColor('#c53030')
      .text(
        'This document is generated for informational purposes only and does not constitute legal advice. ' +
        'Please consult a licensed attorney in your state before executing this document.',
        78, doc.y - 40, { width: CONTENT_WIDTH - 12 }
      );

    if (watermark) addWatermark();

    // ── BODY SECTIONS ──
    content.sections.forEach((section) => {
      doc.addPage();

      // Running header
      doc.fontSize(8).fillColor(GRAY).font('Helvetica')
        .text(content.title.split('\n')[0], 72, 30, { width: CONTENT_WIDTH, align: 'left' });
      doc.text(new Date().toLocaleDateString(), 72, 30, { width: CONTENT_WIDTH, align: 'right' });
      doc.moveTo(72, 48).lineTo(PAGE_WIDTH - 72, 48).stroke('#e2e8f0');

      doc.moveDown(2);

      // Section heading
      doc.fontSize(13).fillColor(PRIMARY).font('Helvetica-Bold')
        .text(section.heading, { width: CONTENT_WIDTH });
      doc.moveDown(0.5);
      doc.moveTo(72, doc.y).lineTo(PAGE_WIDTH - 72, doc.y).stroke('#bee3f8');
      doc.moveDown(0.8);

      // Section body
      doc.fontSize(10).fillColor('#2d3748').font('Helvetica')
        .text(section.body, { width: CONTENT_WIDTH, lineGap: 4 });

      if (watermark) addWatermark();
    });

    // ── EXECUTION PAGE ──
    doc.addPage();
    doc.fontSize(8).fillColor(GRAY).text(content.title.split('\n')[0], 72, 30, { width: CONTENT_WIDTH });
    doc.text('EXECUTION', 72, 30, { width: CONTENT_WIDTH, align: 'right' });
    doc.moveTo(72, 48).lineTo(PAGE_WIDTH - 72, 48).stroke('#e2e8f0');

    doc.moveDown(2);
    doc.fontSize(13).fillColor(PRIMARY).font('Helvetica-Bold').text('EXECUTION AND SIGNATURES', { width: CONTENT_WIDTH });
    doc.moveDown(0.5);
    doc.moveTo(72, doc.y).lineTo(PAGE_WIDTH - 72, doc.y).stroke('#bee3f8');
    doc.moveDown(0.8);
    doc.fontSize(10).fillColor('#2d3748').font('Helvetica')
      .text(content.executionBlock, { width: CONTENT_WIDTH, lineGap: 6 });

    if (watermark) addWatermark();

    // ── NOTARIZATION PAGE ──
    doc.addPage();
    doc.fontSize(8).fillColor(GRAY).text(content.title.split('\n')[0], 72, 30, { width: CONTENT_WIDTH });
    doc.text('NOTARIZATION', 72, 30, { width: CONTENT_WIDTH, align: 'right' });
    doc.moveTo(72, 48).lineTo(PAGE_WIDTH - 72, 48).stroke('#e2e8f0');

    doc.moveDown(2);
    doc.fontSize(13).fillColor(PRIMARY).font('Helvetica-Bold').text('NOTARIZATION', { width: CONTENT_WIDTH });
    doc.moveDown(0.5);
    doc.moveTo(72, doc.y).lineTo(PAGE_WIDTH - 72, doc.y).stroke('#bee3f8');
    doc.moveDown(0.8);
    doc.fontSize(10).fillColor('#2d3748').font('Helvetica')
      .text(content.notarizationBlock, { width: CONTENT_WIDTH, lineGap: 6 });

    if (watermark) addWatermark();

    // ── SCHEDULE A PAGE ──
    doc.addPage();
    doc.fontSize(8).fillColor(GRAY).text(content.title.split('\n')[0], 72, 30, { width: CONTENT_WIDTH });
    doc.text('SCHEDULE A', 72, 30, { width: CONTENT_WIDTH, align: 'right' });
    doc.moveTo(72, 48).lineTo(PAGE_WIDTH - 72, 48).stroke('#e2e8f0');

    doc.moveDown(2);
    doc.fontSize(13).fillColor(PRIMARY).font('Helvetica-Bold').text('SCHEDULE A — TRUST PROPERTY', { width: CONTENT_WIDTH });
    doc.moveDown(0.5);
    doc.moveTo(72, doc.y).lineTo(PAGE_WIDTH - 72, doc.y).stroke('#bee3f8');
    doc.moveDown(0.8);
    doc.fontSize(10).fillColor('#2d3748').font('Helvetica')
      .text(content.scheduleA, { width: CONTENT_WIDTH, lineGap: 4 });

    if (watermark) addWatermark();

    // ── FOOTER on all pages ──
    const totalPages = (doc as any)._pageBuffer?.length || 1;
    let pageNum = 1;
    doc.on('pageAdded', () => { pageNum++; });

    doc.rect(0, PAGE_HEIGHT - 36, PAGE_WIDTH, 36).fill('#f7fafc');
    doc.fontSize(8).fillColor(GRAY).font('Helvetica')
      .text(
        watermark
          ? 'PREVIEW DOCUMENT — Purchase to download the official version without watermark | livingtrust.app'
          : `${content.title.split('\n')[0]} | Generated by Living Trust App | livingtrust.app`,
        72,
        PAGE_HEIGHT - 24,
        { width: CONTENT_WIDTH, align: 'center' }
      );

    doc.end();
  }) as unknown as Buffer;
}

// ─────────────────────────────────────────────────────────────────────────────
// ROUTES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * GET /api/pdf/states
 * Returns the list of all supported states.
 */
router.get('/states', (_req: Request, res: Response) => {
  res.json({ success: true, states: ALL_STATES });
});

/**
 * POST /api/pdf/preview
 * Generates a watermarked PDF preview of the living trust.
 * Body: TrustData
 * Returns: PDF as binary (Content-Type: application/pdf)
 */
router.post('/preview', async (req: Request, res: Response) => {
  try {
    const trustData: TrustData = req.body;

    if (!trustData.trustName || !trustData.grantor || !trustData.trustee || !trustData.successorTrustee) {
      return res.status(400).json({ error: 'Missing required trust fields: trustName, grantor, trustee, successorTrustee' });
    }

    const template = getTemplate(trustData.state);
    const content = template.generate(trustData);
    const pdfBuffer = await (buildPdf(trustData, content, true) as unknown as Promise<Buffer>);

    res.setHeader('Content-Type', 'application/pdf');
    res.setHeader('Content-Disposition', `inline; filename="${trustData.trustName.replace(/\s+/g, '_')}_PREVIEW.pdf"`);
    res.setHeader('Content-Length', pdfBuffer.length);
    res.send(pdfBuffer);
  } catch (err: any) {
    console.error('PDF preview error:', err);
    res.status(500).json({ error: 'Failed to generate PDF preview', message: err.message });
  }
});

/**
 * POST /api/pdf/preview-base64
 * Same as /preview but returns base64-encoded PDF for mobile clients.
 */
router.post('/preview-base64', async (req: Request, res: Response) => {
  try {
    const trustData: TrustData = req.body;

    if (!trustData.trustName || !trustData.grantor || !trustData.trustee || !trustData.successorTrustee) {
      return res.status(400).json({ error: 'Missing required trust fields' });
    }

    const template = getTemplate(trustData.state);
    const content = template.generate(trustData);
    const pdfBuffer = await (buildPdf(trustData, content, true) as unknown as Promise<Buffer>);

    res.json({
      success: true,
      fileName: `${trustData.trustName.replace(/\s+/g, '_')}_PREVIEW.pdf`,
      mimeType: 'application/pdf',
      base64: pdfBuffer.toString('base64'),
      pageCount: 'multiple',
      isWatermarked: true,
      state: getTemplate(trustData.state).stateName,
      governingLaw: getTemplate(trustData.state).governingLaw,
    });
  } catch (err: any) {
    console.error('PDF preview-base64 error:', err);
    res.status(500).json({ error: 'Failed to generate PDF preview', message: err.message });
  }
});

/**
 * POST /api/pdf/issue-download-token
 * Called by the payment service after successful payment.
 * Issues a time-limited download token for the clean PDF.
 * Body: { trustData, paymentIntentId }
 */
router.post('/issue-download-token', (req: Request, res: Response) => {
  try {
    const { trustData, paymentIntentId } = req.body;

    if (!trustData || !paymentIntentId) {
      return res.status(400).json({ error: 'trustData and paymentIntentId required' });
    }

    const { downloadToken, expiresAt } = issueDownloadToken(trustData, paymentIntentId);

    res.json({
      success: true,
      downloadToken,
      expiresAt,
      message: 'Token valid for 24 hours. Use GET /api/pdf/download/:token to download.',
    });
  } catch (err: any) {
    res.status(500).json({ error: 'Failed to issue download token', message: err.message });
  }
});

/**
 * GET /api/pdf/download/:token
 * Downloads the clean (unwatermarked) PDF using a valid token.
 */
router.get('/download/:token', async (req: Request, res: Response) => {
  try {
    const { token } = req.params;
    const entry = downloadTokens.get(token);

    if (!entry) {
      return res.status(404).json({ error: 'Invalid or expired download token. Please complete payment first.' });
    }

    if (Date.now() > entry.expiresAt) {
      downloadTokens.delete(token);
      return res.status(410).json({ error: 'Download token has expired. Please contact support.' });
    }

    const template = getTemplate(entry.trustData.state);
    const content = template.generate(entry.trustData);
    const pdfBuffer = await (buildPdf(entry.trustData, content, false) as unknown as Promise<Buffer>);

    // Allow single-use download (remove token after download)
    // Uncomment below to enforce single-use:
    // downloadTokens.delete(token);

    res.setHeader('Content-Type', 'application/pdf');
    res.setHeader(
      'Content-Disposition',
      `attachment; filename="${entry.trustData.trustName.replace(/\s+/g, '_')}_LivingTrust_OFFICIAL.pdf"`
    );
    res.setHeader('Content-Length', pdfBuffer.length);
    res.send(pdfBuffer);
  } catch (err: any) {
    console.error('PDF download error:', err);
    res.status(500).json({ error: 'Failed to generate PDF', message: err.message });
  }
});

/**
 * GET /api/pdf/download-base64/:token
 * Same as download but returns base64 for mobile clients.
 */
router.get('/download-base64/:token', async (req: Request, res: Response) => {
  try {
    const { token } = req.params;
    const entry = downloadTokens.get(token);

    if (!entry) {
      return res.status(404).json({ error: 'Invalid or expired download token.' });
    }
    if (Date.now() > entry.expiresAt) {
      downloadTokens.delete(token);
      return res.status(410).json({ error: 'Download token has expired.' });
    }

    const template = getTemplate(entry.trustData.state);
    const content = template.generate(entry.trustData);
    const pdfBuffer = await (buildPdf(entry.trustData, content, false) as unknown as Promise<Buffer>);

    res.json({
      success: true,
      fileName: `${entry.trustData.trustName.replace(/\s+/g, '_')}_LivingTrust_OFFICIAL.pdf`,
      mimeType: 'application/pdf',
      base64: pdfBuffer.toString('base64'),
      isWatermarked: false,
      state: template.stateName,
    });
  } catch (err: any) {
    res.status(500).json({ error: 'Failed to generate PDF', message: err.message });
  }
});

export default router;
