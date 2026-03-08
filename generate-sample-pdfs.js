#!/usr/bin/env node
/**
 * generate-sample-pdfs.js
 *
 * Standalone script that generates two sample living trust PDFs:
 *   1. sample-trust-watermarked.pdf  — watermarked preview (as shown before payment)
 *   2. sample-trust-official.pdf     — clean official copy (as downloaded after payment)
 *
 * Uses the same PDFKit logic as the backend /api/pdf routes.
 * Run from the repo root:
 *   node generate-sample-pdfs.js
 */

'use strict';

const PDFDocument = require('./backend/node_modules/pdfkit');
const fs = require('fs');
const path = require('path');

// ── Sample trust data ──────────────────────────────────────────────────────
const sampleTrust = {
  trustName:        'Smith Family Revocable Living Trust',
  grantor:          'John Robert Smith',
  grantorAddress:   '1234 Maple Avenue, Seattle, WA 98101',
  trustee:          'John Robert Smith',
  successorTrustee: 'Jane Elizabeth Smith',
  beneficiaries:    ['Alice Marie Smith', 'Robert James Smith'],
  assets:           [
    'Real property located at 1234 Maple Avenue, Seattle, WA 98101',
    'All financial accounts held at First National Bank',
    'All personal property, household furnishings, and effects',
    '2022 Toyota Camry, VIN: 4T1BF1FK0CU123456',
  ],
  state:            'WA',
  trustType:        'Revocable',
  dateCreated:      new Date().toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric',
  }),
};

// ── PDF content sections ───────────────────────────────────────────────────
function buildContent(trust) {
  return [
    {
      title: 'ARTICLE I — DECLARATION OF TRUST',
      body: `I, ${trust.grantor}, residing at ${trust.grantorAddress}, hereby declare that I have transferred and delivered to the Trustee the property described in Schedule A attached hereto. All such property, together with any other property hereafter transferred to the Trustee, is referred to as the "Trust Estate" and shall be held, administered, and distributed by the Trustee pursuant to this Declaration of Trust.`,
    },
    {
      title: 'ARTICLE II — NAME AND REVOCABILITY',
      body: `This trust shall be known as the "${trust.trustName}" (hereinafter "Trust"). This Trust is revocable and may be amended, restated, or revoked by the Grantor at any time during the Grantor's lifetime by written instrument delivered to the Trustee. This Trust shall become irrevocable upon the death or incapacity of the Grantor. This Trust is established pursuant to the laws of the State of Washington, specifically RCW Chapter 11.98.`,
    },
    {
      title: 'ARTICLE III — TRUSTEE PROVISIONS',
      body: `The initial Trustee of this Trust shall be ${trust.trustee}. If the initial Trustee is unable or unwilling to serve, ${trust.successorTrustee} shall serve as Successor Trustee. The Trustee shall have all powers granted by Washington law (RCW 11.98.070), including but not limited to: the power to invest and reinvest trust assets; sell, exchange, or encumber property; pay expenses; and distribute income and principal as provided herein.`,
    },
    {
      title: 'ARTICLE IV — DISTRIBUTIONS DURING GRANTOR\'S LIFETIME',
      body: `During the Grantor's lifetime, the Trustee shall distribute to or for the benefit of the Grantor such amounts of net income and principal as the Grantor shall direct. In the event of the Grantor's incapacity, the Trustee shall distribute such amounts as are reasonably necessary for the Grantor's health, education, maintenance, and support, considering all other resources available to the Grantor.`,
    },
    {
      title: 'ARTICLE V — DISTRIBUTION UPON DEATH',
      body: `Upon the death of the Grantor, after payment of debts, expenses, and taxes, the Trustee shall distribute the remaining Trust Estate to the following beneficiaries in equal shares:\n\n${trust.beneficiaries.map((b, i) => `  ${i + 1}. ${b}`).join('\n')}\n\nIf any beneficiary predeceases the Grantor, that beneficiary's share shall pass to their lineal descendants per stirpes, or if none, shall be divided equally among the surviving beneficiaries.`,
    },
    {
      title: 'ARTICLE VI — GENERAL PROVISIONS',
      body: `This Trust shall be governed by and construed under the laws of the State of Washington. The invalidity of any provision shall not affect the validity of the remaining provisions. This Trust shall be binding upon and inure to the benefit of all parties and their successors. The masculine gender includes the feminine and vice versa; the singular includes the plural.`,
    },
  ];
}

// ── Core PDF builder ───────────────────────────────────────────────────────
function buildPdf(trust, content, addWatermark) {
  return new Promise((resolve, reject) => {
    const doc = new PDFDocument({ margin: 72, size: 'LETTER' });
    const chunks = [];
    doc.on('data', chunk => chunks.push(chunk));
    doc.on('end', () => resolve(Buffer.concat(chunks)));
    doc.on('error', reject);

    const NAVY   = '#1a365d';
    const GRAY   = '#4a5568';
    const LGRAY  = '#718096';
    const BORDER = '#e2e8f0';

    // ── Watermark helper ──────────────────────────────────────────────────
    function stampWatermark() {
      if (!addWatermark) return;
      const cx = doc.page.width  / 2;
      const cy = doc.page.height / 2;
      doc.save();
      doc.translate(cx, cy).rotate(-45);
      doc.fontSize(52).fillColor('#c0c0c0').opacity(0.18)
         .text('PREVIEW — NOT FOR LEGAL USE', -260, -26, { width: 520, align: 'center' });
      doc.restore();
      doc.opacity(1);
    }

    // ── Title page ────────────────────────────────────────────────────────
    stampWatermark();

    doc.rect(0, 0, doc.page.width, 12).fill(NAVY);
    doc.moveDown(2);
    doc.fontSize(26).fillColor(NAVY).font('Helvetica-Bold')
       .text(trust.trustName, { align: 'center' });
    doc.moveDown(0.5);
    doc.fontSize(14).fillColor(GRAY).font('Helvetica')
       .text('A Revocable Living Trust', { align: 'center' });
    doc.moveDown(2);

    // Info box
    doc.rect(72, doc.y, doc.page.width - 144, 160).stroke(BORDER);
    const boxTop = doc.y + 16;
    doc.fontSize(11).fillColor(GRAY);
    const rows = [
      ['Grantor (Settlor):', trust.grantor],
      ['Initial Trustee:',  trust.trustee],
      ['Successor Trustee:', trust.successorTrustee],
      ['Governing Law:',    'Washington State — RCW Chapter 11.98'],
      ['Date Prepared:',    trust.dateCreated],
      ['Trust Type:',       'Revocable Living Trust'],
    ];
    rows.forEach(([label, value], i) => {
      doc.font('Helvetica-Bold').text(label, 88, boxTop + i * 22, { continued: true, width: 160 });
      doc.font('Helvetica').text(' ' + value, { width: doc.page.width - 280 });
    });

    doc.moveDown(10);
    doc.fontSize(9).fillColor(LGRAY).font('Helvetica')
       .text(
         addWatermark
           ? 'PREVIEW COPY — This document is for review purposes only. Purchase to receive the official, legally formatted document.'
           : 'OFFICIAL COPY — This document has been prepared using state-specific legal templates. Consult a licensed attorney before execution.',
         { align: 'center' }
       );

    // ── Article pages ─────────────────────────────────────────────────────
    content.forEach(section => {
      doc.addPage();
      stampWatermark();

      doc.rect(72, 72, doc.page.width - 144, 4).fill(NAVY);
      doc.moveDown(1);
      doc.fontSize(13).fillColor(NAVY).font('Helvetica-Bold')
         .text(section.title, 72, 90);
      doc.moveDown(0.8);
      doc.fontSize(11).fillColor(GRAY).font('Helvetica')
         .text(section.body, { align: 'justify', lineGap: 4 });
    });

    // ── Execution page ────────────────────────────────────────────────────
    doc.addPage();
    stampWatermark();

    doc.rect(72, 72, doc.page.width - 144, 4).fill(NAVY);
    doc.fontSize(13).fillColor(NAVY).font('Helvetica-Bold')
       .text('EXECUTION AND ACKNOWLEDGMENT', 72, 90);
    doc.moveDown(1);
    doc.fontSize(11).fillColor(GRAY).font('Helvetica').text(
      `IN WITNESS WHEREOF, I, ${trust.grantor}, as Grantor and Trustee of the ${trust.trustName}, have executed this Declaration of Trust on the date first written above.`,
      { align: 'justify' }
    );
    doc.moveDown(3);

    // Grantor signature block
    doc.font('Helvetica-Bold').text('GRANTOR / TRUSTEE:');
    doc.moveDown(2);
    doc.moveTo(72, doc.y).lineTo(320, doc.y).stroke(GRAY);
    doc.fontSize(9).fillColor(LGRAY).font('Helvetica')
       .text(`${trust.grantor}, Grantor and Trustee`, 72, doc.y + 4);
    doc.moveDown(3);

    // Notary block
    doc.rect(72, doc.y, doc.page.width - 144, 140).stroke(BORDER);
    const ny = doc.y + 12;
    doc.fontSize(10).fillColor(NAVY).font('Helvetica-Bold')
       .text('NOTARY ACKNOWLEDGMENT — STATE OF WASHINGTON', 88, ny, { align: 'center', width: doc.page.width - 176 });
    doc.fontSize(9).fillColor(GRAY).font('Helvetica')
       .text(
         `On this _____ day of _____________, _______, before me, a Notary Public in and for the State of Washington, personally appeared ${trust.grantor}, known to me to be the person whose name is subscribed to this instrument, and acknowledged that they executed the same.`,
         88, ny + 22, { width: doc.page.width - 176 }
       );
    doc.moveDown(2);
    doc.text('Notary Public: _______________________________    Commission Expires: ____________', 88, ny + 86);

    // ── Schedule A ────────────────────────────────────────────────────────
    doc.addPage();
    stampWatermark();

    doc.rect(72, 72, doc.page.width - 144, 4).fill(NAVY);
    doc.fontSize(13).fillColor(NAVY).font('Helvetica-Bold')
       .text('SCHEDULE A — TRUST ASSETS', 72, 90);
    doc.moveDown(1);
    doc.fontSize(11).fillColor(GRAY).font('Helvetica')
       .text(`The following assets are transferred to the ${trust.trustName}:`);
    doc.moveDown(0.8);
    trust.assets.forEach((asset, i) => {
      doc.font('Helvetica-Bold').text(`${i + 1}.`, 72, doc.y, { continued: true, width: 20 });
      doc.font('Helvetica').text(`  ${asset}`, { width: doc.page.width - 164 });
      doc.moveDown(0.4);
    });
    doc.moveDown(2);
    doc.fontSize(9).fillColor(LGRAY)
       .text('Additional assets may be added to this Trust by the Grantor at any time by written amendment or by designation in a will or other instrument.', { align: 'justify' });

    // ── Footer on every page ──────────────────────────────────────────────
    const pageRange = doc.bufferedPageRange();
    for (let i = pageRange.start; i < pageRange.start + pageRange.count; i++) {
      doc.switchToPage(i);
      const footerY = doc.page.height - 40;
      doc.rect(0, footerY - 4, doc.page.width, 1).fill(BORDER);
      doc.fontSize(8).fillColor(LGRAY).font('Helvetica')
         .text(
           `${trust.trustName}  ·  Page ${i + 1} of ${pageRange.count}  ·  ${addWatermark ? 'PREVIEW COPY' : 'OFFICIAL COPY'}  ·  Prepared ${trust.dateCreated}`,
           72, footerY, { align: 'center', width: doc.page.width - 144 }
         );
    }

    doc.end();
  });
}

// ── Main ───────────────────────────────────────────────────────────────────
async function main() {
  const content = buildContent(sampleTrust);
  const outDir  = path.join(__dirname, 'sample-pdfs');
  fs.mkdirSync(outDir, { recursive: true });

  console.log('Generating sample PDFs...\n');

  // 1. Watermarked preview
  const watermarkedBuf = await buildPdf(sampleTrust, content, true);
  const watermarkedPath = path.join(outDir, 'sample-trust-watermarked.pdf');
  fs.writeFileSync(watermarkedPath, watermarkedBuf);
  console.log(`✓ Watermarked preview: ${watermarkedPath} (${(watermarkedBuf.length / 1024).toFixed(1)} KB)`);

  // 2. Official clean copy
  const officialBuf = await buildPdf(sampleTrust, content, false);
  const officialPath = path.join(outDir, 'sample-trust-official.pdf');
  fs.writeFileSync(officialPath, officialBuf);
  console.log(`✓ Official clean copy: ${officialPath} (${(officialBuf.length / 1024).toFixed(1)} KB)`);

  console.log('\nDone. PDFs written to sample-pdfs/');
}

main().catch(err => { console.error('Error:', err.message); process.exit(1); });
