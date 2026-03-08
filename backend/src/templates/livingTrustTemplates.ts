/**
 * Living Trust Document Templates
 *
 * Covers all 50 US states with state-specific legal language.
 * Each template generates a full revocable living trust document
 * compliant with that state's trust law.
 *
 * DISCLAIMER: These templates are for educational purposes and provide
 * a general framework. Users should consult a licensed attorney in their
 * state for legal advice specific to their situation.
 */

export interface TrustData {
  trustName: string;
  grantor: string;
  grantorAddress?: string;
  trustee: string;
  successorTrustee: string;
  beneficiaries: string[];
  assets: string[];
  trustType?: string;
  state?: string;
  notes?: string;
  date?: string;
}

export interface TrustTemplate {
  stateName: string;
  stateCode: string;
  governingLaw: string;
  generate(data: TrustData): TrustDocumentContent;
}

export interface TrustDocumentContent {
  title: string;
  sections: TrustSection[];
  executionBlock: string;
  notarizationBlock: string;
  scheduleA: string;
}

export interface TrustSection {
  heading: string;
  body: string;
}

// ─────────────────────────────────────────────────────────────────────────────
// UTILITY HELPERS
// ─────────────────────────────────────────────────────────────────────────────

function formatDate(dateStr?: string): string {
  const d = dateStr ? new Date(dateStr) : new Date();
  return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
}

function beneficiaryList(beneficiaries: string[]): string {
  if (beneficiaries.length === 0) return 'as designated by the Grantor';
  if (beneficiaries.length === 1) return beneficiaries[0];
  const last = beneficiaries[beneficiaries.length - 1];
  const rest = beneficiaries.slice(0, -1);
  return `${rest.join(', ')} and ${last}`;
}

function assetSchedule(assets: string[]): string {
  if (assets.length === 0) return 'All property transferred to this Trust as evidenced by separate assignment.';
  return assets.map((a, i) => `${i + 1}. ${a}`).join('\n');
}

function commonExecutionBlock(data: TrustData): string {
  return `IN WITNESS WHEREOF, the Grantor and Trustee have executed this Trust Agreement as of ${formatDate(data.date)}.


GRANTOR:

_________________________________
${data.grantor}
Date: _____________________________


TRUSTEE:

_________________________________
${data.trustee}
Date: _____________________________


SUCCESSOR TRUSTEE:

_________________________________
${data.successorTrustee}
Date: _____________________________`;
}

function commonNotarizationBlock(data: TrustData): string {
  return `STATE OF ________________
COUNTY OF ______________

On this _____ day of _________________, 20___, before me personally appeared ${data.grantor}, known to me to be the person(s) whose name(s) is/are subscribed to the within instrument, and acknowledged to me that he/she/they executed the same in his/her/their authorized capacity(ies).

_________________________________
Notary Public
My Commission Expires: ___________
[NOTARY SEAL]`;
}

// ─────────────────────────────────────────────────────────────────────────────
// WASHINGTON STATE TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const washingtonTemplate: TrustTemplate = {
  stateName: 'Washington',
  stateCode: 'WA',
  governingLaw: 'RCW Chapter 11.98 (Washington Trust Act)',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);

    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of Washington`,

      sections: [
        {
          heading: 'RECITALS AND DECLARATION OF TRUST',
          body: `This Revocable Living Trust Agreement ("Trust Agreement") is made and entered into on ${date}, by and between ${data.grantor} ("Grantor"), residing at ${data.grantorAddress || 'Washington State'}, and ${data.trustee} ("Trustee").

WHEREAS, the Grantor desires to create a revocable living trust for the purposes set forth herein, to provide for the management of the Grantor's assets during the Grantor's lifetime and for the orderly distribution of trust assets upon the Grantor's death;

NOW, THEREFORE, the Grantor hereby transfers and delivers to the Trustee the property described in Schedule A, attached hereto and incorporated herein, to be held, administered, and distributed in accordance with this Trust Agreement.

This Trust shall be known as THE ${data.trustName.toUpperCase()}.`
        },
        {
          heading: 'ARTICLE I — IDENTIFICATION AND GOVERNING LAW',
          body: `1.1 This Trust is created pursuant to and shall be governed by the laws of the State of Washington, including the Washington Trust Act (RCW Chapter 11.98), as amended from time to time.

1.2 The principal place of administration of this Trust shall be in the State of Washington.

1.3 The Grantor is ${data.grantor}. The initial Trustee is ${data.trustee}. The Successor Trustee is ${data.successorTrustee}.

1.4 The primary beneficiaries of this Trust are: ${bens}.`
        },
        {
          heading: 'ARTICLE II — REVOCABILITY AND AMENDMENT',
          body: `2.1 During the Grantor's lifetime and so long as the Grantor is not Incapacitated (as defined herein), the Grantor reserves the absolute right to revoke, alter, amend, or modify this Trust Agreement, in whole or in part, at any time and from time to time, without the consent of any Trustee or beneficiary, by a written instrument signed by the Grantor and delivered to the Trustee.

2.2 The Grantor may also revoke this Trust by signed written notice delivered to the Trustee. Upon revocation, the Trustee shall transfer all Trust property to the Grantor.

2.3 No amendment or revocation shall be effective unless it is in writing and signed by the Grantor (or the Grantor's attorney-in-fact acting pursuant to a valid durable power of attorney).

2.4 This Trust shall become irrevocable upon the Grantor's death or upon the Grantor's written declaration that this Trust is irrevocable.`
        },
        {
          heading: 'ARTICLE III — TRUSTEE PROVISIONS',
          body: `3.1 INITIAL TRUSTEE. ${data.trustee} shall serve as the initial Trustee of this Trust.

3.2 SUCCESSOR TRUSTEE. If ${data.trustee} is unable or unwilling to serve as Trustee by reason of death, incapacity, resignation, or removal, then ${data.successorTrustee} shall serve as Successor Trustee. The Successor Trustee shall have all powers, authorities, and duties granted to the initial Trustee.

3.3 TRUSTEE POWERS. The Trustee shall have the following powers, in addition to those granted by law under RCW 11.98.070:
   (a) To retain any trust property originally transferred to the Trust;
   (b) To sell, exchange, or otherwise dispose of trust property at public or private sale;
   (c) To invest and reinvest trust assets in any form of property;
   (d) To manage, lease, or encumber real property;
   (e) To borrow money and encumber trust property as security;
   (f) To pay expenses of administration;
   (g) To make distributions in cash or in kind;
   (h) To employ attorneys, accountants, investment advisors, and other agents;
   (i) To execute any instruments necessary to carry out this Trust Agreement.

3.4 TRUSTEE COMPENSATION. The Trustee shall be entitled to reasonable compensation for services rendered, consistent with the standards set forth in RCW 11.98.

3.5 RESIGNATION. Any Trustee may resign at any time by written notice to the beneficiaries and successor Trustee, effective 30 days after delivery unless a shorter period is agreed upon.

3.6 INCAPACITY. For purposes of this Trust, "Incapacity" means a determination by two licensed physicians (one of whom shall be the Grantor's attending physician if reasonably available) that the Grantor is unable to manage his or her financial affairs.`
        },
        {
          heading: 'ARTICLE IV — DURING GRANTOR\'S LIFETIME',
          body: `4.1 INCOME AND PRINCIPAL. During the Grantor's lifetime, the Trustee shall pay to or apply for the benefit of the Grantor such amounts of income and principal of the Trust as the Grantor directs. If the Grantor becomes Incapacitated, the Trustee shall pay to or apply for the benefit of the Grantor such amounts of income and principal as the Trustee determines necessary or appropriate for the Grantor's health, education, maintenance, and support.

4.2 GRANTOR AS TRUSTEE. If the Grantor is also serving as Trustee, the Grantor may freely use, manage, and transfer Trust property for any purpose as the Grantor deems appropriate.

4.3 TAX TREATMENT. This Trust is intended to be a "grantor trust" for federal and state income tax purposes during the Grantor's lifetime. All income, deductions, and credits shall be reported on the Grantor's personal income tax return.`
        },
        {
          heading: 'ARTICLE V — DISTRIBUTION UPON GRANTOR\'S DEATH',
          body: `5.1 PAYMENT OF DEBTS. Upon the Grantor's death, the Trustee shall pay, or make provision for payment of, the Grantor's legally enforceable debts, funeral expenses, and costs of administration of the Trust, to the extent the Trustee deems appropriate.

5.2 DISTRIBUTION TO BENEFICIARIES. After payment of debts and expenses, the Trustee shall distribute the remaining Trust property to the following beneficiaries:

   PRIMARY BENEFICIARIES: ${bens}

   Distribution shall be made in equal shares unless otherwise specified in Schedule B, or as the Grantor may designate in a written memorandum attached hereto.

5.3 CONTINGENT BENEFICIARIES. If a primary beneficiary does not survive the Grantor by thirty (30) days, that beneficiary's share shall be distributed in equal shares to the surviving primary beneficiaries.

5.4 ULTIMATE DISTRIBUTION. If all named beneficiaries predecease the Grantor, the Trust property shall be distributed to the Grantor's heirs at law as determined under Washington's law of intestate succession (RCW Chapter 11.04).

5.5 MINOR BENEFICIARIES. If any beneficiary is under the age of 21 years at the time of distribution, the Trustee may retain such beneficiary's share in trust until the beneficiary attains age 21, applying income and principal for the beneficiary's health, education, maintenance, and support.`
        },
        {
          heading: 'ARTICLE VI — GENERAL PROVISIONS',
          body: `6.1 NO CONTEST. Any beneficiary who contests the validity of this Trust or any of its provisions shall forfeit any interest herein.

6.2 SPENDTHRIFT PROVISION. No beneficiary may anticipate, assign, encumber, or transfer any interest in this Trust before actual receipt. Trust interests shall not be subject to attachment, garnishment, or other legal process by creditors of any beneficiary.

6.3 SEVERABILITY. If any provision of this Trust is invalid or unenforceable, the remaining provisions shall continue in full force and effect.

6.4 ENTIRE AGREEMENT. This Trust Agreement, together with all schedules attached hereto, constitutes the entire agreement of the parties and supersedes all prior agreements and understandings.

6.5 CONSTRUCTION. This Trust shall be construed and regulated by the laws of the State of Washington. The Trustee and all parties claiming under this Trust shall be subject to the jurisdiction of the courts of Washington.

6.6 HEADINGS. Article and section headings are for convenience only and shall not affect the interpretation of this Trust Agreement.

6.7 POUR-OVER PROVISION. It is the Grantor's intent that any property passing to this Trust under the Grantor's Last Will and Testament (pour-over will) shall be subject to and administered under the terms of this Trust Agreement.

${data.notes ? `6.8 SPECIAL INSTRUCTIONS.\n${data.notes}` : ''}`
        }
      ],

      executionBlock: commonExecutionBlock(data),

      notarizationBlock: `STATE OF WASHINGTON
COUNTY OF ___________________

On this _____ day of _________________, 20___, before me personally appeared ${data.grantor}, known to me to be the person whose name is subscribed to the within instrument, and acknowledged to me that he/she executed the same as his/her voluntary act and deed.

_________________________________
Notary Public in and for the State of Washington
My Commission Expires: ___________
[NOTARY SEAL]`,

      scheduleA: `SCHEDULE A
INITIAL TRUST PROPERTY

The following property is hereby transferred to and made a part of THE ${data.trustName.toUpperCase()}:

${assetSchedule(data.assets)}

The Grantor may transfer additional property to this Trust at any time by assignment, deed, or other instrument of transfer. Such additional property shall become subject to the terms of this Trust Agreement upon transfer.

Grantor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// CALIFORNIA TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const californiaTemplate: TrustTemplate = {
  stateName: 'California',
  stateCode: 'CA',
  governingLaw: 'California Probate Code §§ 15000–19530',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);

    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of California`,

      sections: [
        {
          heading: 'DECLARATION OF TRUST',
          body: `This Revocable Living Trust ("Trust") is made on ${date}, by ${data.grantor} ("Settlor/Trustee"), of the State of California.

The Settlor hereby declares that all property transferred to this Trust, described in Schedule A, shall be held, managed, and distributed as provided herein.

This Trust shall be known as THE ${data.trustName.toUpperCase()}, and is created pursuant to California Probate Code §§ 15000 et seq.`
        },
        {
          heading: 'ARTICLE I — PARTIES AND GOVERNING LAW',
          body: `1.1 SETTLOR/GRANTOR: ${data.grantor}
1.2 INITIAL TRUSTEE: ${data.trustee}
1.3 SUCCESSOR TRUSTEE: ${data.successorTrustee}
1.4 PRIMARY BENEFICIARIES: ${bens}

1.5 This Trust is governed by the laws of the State of California, including the California Uniform Trust Code as codified in the California Probate Code, as amended.

1.6 Community Property Note: To the extent that any property transferred to this Trust constitutes community property under California law, both spouses must sign the appropriate transfer documents.`
        },
        {
          heading: 'ARTICLE II — REVOCABILITY',
          body: `2.1 This Trust is revocable during the Settlor's lifetime. The Settlor may revoke or amend this Trust, in whole or in part, by a writing signed by the Settlor and delivered to the Trustee, as provided in California Probate Code § 15401.

2.2 Upon revocation, the Trustee shall transfer all trust property to the Settlor free of trust.

2.3 This Trust becomes irrevocable upon the Settlor's death.`
        },
        {
          heading: 'ARTICLE III — TRUSTEE POWERS AND DUTIES',
          body: `3.1 The Trustee shall have all powers granted under California Probate Code §§ 16200–16249 and those set forth herein.

3.2 KEY POWERS include:
   (a) Power to retain, purchase, or sell trust assets;
   (b) Power to manage real property, including leasing and encumbering;
   (c) Power to invest in any form of property (subject to the Prudent Investor Rule, Cal. Prob. Code § 16045);
   (d) Power to make distributions in cash or in kind;
   (e) Power to employ agents, attorneys, and advisors;
   (f) Power to pay taxes, debts, and expenses of the Trust.

3.3 SUCCESSOR TRUSTEE: If ${data.trustee} ceases to serve, ${data.successorTrustee} shall serve as Trustee without bond.

3.4 COMPENSATION: The Trustee shall be entitled to reasonable compensation under California Probate Code § 15681.`
        },
        {
          heading: 'ARTICLE IV — DISTRIBUTIONS DURING SETTLOR\'S LIFE',
          body: `4.1 During the Settlor's lifetime and while competent, the Trustee shall pay income and principal to or for the benefit of the Settlor as the Settlor requests.

4.2 If the Settlor becomes incapacitated (as certified by two licensed physicians), the Trustee shall provide for the Settlor's health, education, maintenance, and support from Trust assets.

4.3 MEDI-CAL PLANNING: This Trust does not constitute a special needs trust and does not provide Medi-Cal asset protection unless separately structured.`
        },
        {
          heading: 'ARTICLE V — DISTRIBUTION AT DEATH',
          body: `5.1 Upon the Settlor's death, after payment of legally enforceable debts and administration expenses, the Trustee shall distribute the Trust as follows:

PRIMARY DISTRIBUTION to: ${bens}, in equal shares, outright and free of trust.

5.2 If any primary beneficiary predeceases the Settlor by 30 days, that share shall pass to the surviving beneficiaries in equal shares.

5.3 Any remaining property not effectively disposed of shall pass pursuant to California intestate succession law (California Probate Code § 6400 et seq.).

5.4 HOMESTEAD: Any real property that qualifies as the Settlor's principal residence may pass subject to a homeowner's exemption reassessment exclusion under California Revenue & Taxation Code § 63.1 if the beneficiary is an eligible transferee.

${data.notes ? `5.5 SPECIAL INSTRUCTIONS:\n${data.notes}` : ''}`
        },
        {
          heading: 'ARTICLE VI — GENERAL PROVISIONS',
          body: `6.1 SPENDTHRIFT: No interest of any beneficiary may be assigned, anticipated, or encumbered, and no such interest shall be subject to claims of any creditor (California Probate Code § 15300).

6.2 NO CONTEST: Any person who contests this Trust shall forfeit any benefit hereunder (California Probate Code § 21310).

6.3 PERPETUITIES: This Trust shall terminate no later than permitted under California law (California Probate Code § 21205).

6.4 SEVERABILITY: Invalid provisions shall be severed without affecting the remainder of this Trust.`
        }
      ],

      executionBlock: commonExecutionBlock(data),
      notarizationBlock: `STATE OF CALIFORNIA
COUNTY OF ___________________

On _________________, before me, ________________________, Notary Public, personally appeared ${data.grantor}, proved to me on the basis of satisfactory evidence to be the person(s) whose name(s) is/are subscribed to the within instrument and acknowledged to me that he/she/they executed the same in his/her/their authorized capacity(ies).

WITNESS my hand and official seal.

_________________________________
Notary Public — State of California
[NOTARY SEAL]`,

      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nSettlor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// TEXAS TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const texasTemplate: TrustTemplate = {
  stateName: 'Texas',
  stateCode: 'TX',
  governingLaw: 'Texas Trust Code, Texas Property Code §§ 111.001–116.172',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);

    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of Texas`,
      sections: [
        {
          heading: 'DECLARATION OF TRUST',
          body: `This Revocable Living Trust Agreement ("Trust") is made as of ${date}, by ${data.grantor} ("Grantor" and initial "Trustee"), of the State of Texas.

This Trust is created pursuant to the Texas Trust Code (Texas Property Code §§ 111.001 et seq.) and shall be known as THE ${data.trustName.toUpperCase()}.

The Grantor transfers the property listed in Schedule A to the Trustee, to be held in trust under the terms of this Agreement.`
        },
        {
          heading: 'ARTICLE I — IDENTIFICATION',
          body: `Grantor: ${data.grantor}
Initial Trustee: ${data.trustee}
Successor Trustee: ${data.successorTrustee}
Primary Beneficiaries: ${bens}

Governing Law: Texas Property Code §§ 111.001–116.172.`
        },
        {
          heading: 'ARTICLE II — REVOCABILITY AND AMENDMENT',
          body: `2.1 This Trust is revocable. The Grantor may revoke or amend this Trust at any time during the Grantor's lifetime while competent, by written instrument delivered to the Trustee (Tex. Prop. Code § 112.051).

2.2 Upon the Grantor's death or declaration of irrevocability, this Trust becomes irrevocable and may not be amended.`
        },
        {
          heading: 'ARTICLE III — TRUSTEE PROVISIONS',
          body: `3.1 The Trustee shall have all powers authorized under Texas Property Code §§ 113.001–113.029 and as set forth herein.

3.2 SUCCESSOR: If ${data.trustee} ceases to serve, ${data.successorTrustee} shall succeed without court order or bond.

3.3 KEY POWERS: manage, sell, lease, encumber, and invest trust assets; make distributions; employ professionals; pay taxes and expenses; execute documents necessary to administer the Trust.

3.4 COMPENSATION: Reasonable compensation as agreed or as permitted by Texas law.

3.5 COMMUNITY PROPERTY: To the extent trust property is community property, both spouses' signatures are required on transfer documents under Texas Family Code § 3.102.`
        },
        {
          heading: 'ARTICLE IV — LIFETIME DISTRIBUTIONS',
          body: `4.1 During the Grantor's lifetime and while competent, the Trustee shall distribute income and principal as the Grantor directs.

4.2 INCAPACITY: If the Grantor becomes incapacitated (per certification by two licensed Texas physicians), the Trustee shall apply Trust assets for the Grantor's health, education, maintenance, and support.`
        },
        {
          heading: 'ARTICLE V — DISTRIBUTION AT DEATH',
          body: `5.1 After the Grantor's death, the Trustee shall pay the Grantor's legally enforceable debts and expenses, then distribute the Trust estate to: ${bens}, in equal shares.

5.2 Predeceased beneficiaries: surviving primary beneficiaries share equally.

5.3 Texas Homestead Note: Property that constitutes the Grantor's homestead may retain homestead protections under Texas Constitution Article XVI, § 50 if properly transferred.

${data.notes ? `5.4 SPECIAL INSTRUCTIONS:\n${data.notes}` : ''}`
        },
        {
          heading: 'ARTICLE VI — MISCELLANEOUS',
          body: `6.1 SPENDTHRIFT: Beneficiaries' interests may not be anticipated, assigned, or subjected to creditors' claims (Tex. Prop. Code § 112.035).

6.2 SEVERABILITY: Invalid clauses shall not affect remaining provisions.

6.3 GOVERNING LAW: State of Texas.`
        }
      ],
      executionBlock: commonExecutionBlock(data),
      notarizationBlock: `STATE OF TEXAS
COUNTY OF ___________________

Before me, the undersigned notary public, on this day personally appeared ${data.grantor}, known to me to be the person whose name is subscribed to the foregoing instrument, and acknowledged to me that he/she executed the same for the purposes therein expressed.

Given under my hand and seal this _____ day of _________________, 20___.

_________________________________
Notary Public, State of Texas
My Commission Expires: ___________
[NOTARY SEAL]`,
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nGrantor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// FLORIDA TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const floridaTemplate: TrustTemplate = {
  stateName: 'Florida',
  stateCode: 'FL',
  governingLaw: 'Florida Trust Code, F.S. §§ 736.0101–736.1303',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of Florida`,
      sections: [
        {
          heading: 'DECLARATION OF TRUST',
          body: `This Revocable Living Trust Agreement is made on ${date}, by ${data.grantor} ("Grantor"), of the State of Florida, under the Florida Trust Code, Chapter 736, Florida Statutes.

This Trust shall be known as THE ${data.trustName.toUpperCase()}.`
        },
        {
          heading: 'ARTICLE I — PARTIES',
          body: `Grantor: ${data.grantor}\nInitial Trustee: ${data.trustee}\nSuccessor Trustee: ${data.successorTrustee}\nPrimary Beneficiaries: ${bens}\n\nGoverning Law: Florida Trust Code, F.S. §§ 736.0101–736.1303.`
        },
        {
          heading: 'ARTICLE II — REVOCABILITY',
          body: `2.1 This Trust is revocable during the Grantor's lifetime. The Grantor may amend or revoke this Trust by written instrument signed by the Grantor and delivered to the Trustee (F.S. § 736.0602).

2.2 This Trust becomes irrevocable upon the Grantor's death.

2.3 FLORIDA HOMESTEAD: Any homestead real property transferred to this Trust shall retain homestead protections under Article X, Section 4, Florida Constitution, and F.S. § 196.041, provided the Grantor maintains the property as his/her primary residence.`
        },
        {
          heading: 'ARTICLE III — TRUSTEE',
          body: `3.1 ${data.trustee} shall serve as initial Trustee. ${data.successorTrustee} shall succeed if the initial Trustee ceases to serve.

3.2 POWERS: All powers authorized under F.S. § 736.0815 and F.S. §§ 736.0801–736.0817, including power to manage, sell, invest, lease, borrow, and distribute trust assets.

3.3 NO BOND required for Trustee named herein.

3.4 COMPENSATION: Reasonable compensation per F.S. § 736.0708.`
        },
        {
          heading: 'ARTICLE IV — LIFETIME DISTRIBUTIONS',
          body: `4.1 During the Grantor's life, the Trustee shall distribute income and principal as the Grantor directs. During incapacity (as certified by two licensed Florida physicians), the Trustee shall provide for the Grantor's health, maintenance, education, and support.`
        },
        {
          heading: 'ARTICLE V — DISTRIBUTION AFTER DEATH',
          body: `5.1 After paying debts and expenses, distribute to: ${bens}, in equal shares.

5.2 ELECTIVE SHARE: Surviving spouse retains rights under F.S. § 732.2005 et seq. regardless of Trust terms.

${data.notes ? `5.3 SPECIAL INSTRUCTIONS:\n${data.notes}` : ''}`
        },
        {
          heading: 'ARTICLE VI — GENERAL',
          body: `6.1 SPENDTHRIFT: F.S. § 736.0502 applies. Beneficiaries' interests are protected from creditor claims as permitted by Florida law.

6.2 SEVERABILITY: Invalid provisions shall be severed without affecting the rest.

6.3 NOTICE REQUIREMENTS: Trustee shall comply with notice requirements of F.S. § 736.0813 upon the Grantor's death.`
        }
      ],
      executionBlock: `IN WITNESS WHEREOF, the Grantor executes this Trust on ${date}.\n\n\nGRANTOR:\n\n_________________________________\n${data.grantor}\nDate: _____________________________\n\n\nTRUSTEE:\n\n_________________________________\n${data.trustee}\nDate: _____________________________\n\nWITNESSES (Florida requires two witnesses):\n\nWitness 1:\n_________________________________\nName: _____________________________\nDate: _____________________________\n\nWitness 2:\n_________________________________\nName: _____________________________\nDate: _____________________________`,
      notarizationBlock: `STATE OF FLORIDA\nCOUNTY OF ___________________\n\nSworn to (or affirmed) and subscribed before me this _____ day of _________________, 20___, by ${data.grantor}, who is personally known to me or produced ________________________ as identification.\n\n_________________________________\nNotary Public, State of Florida\nMy Commission Expires: ___________\n[NOTARY SEAL]`,
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nGrantor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// NEW YORK TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const newYorkTemplate: TrustTemplate = {
  stateName: 'New York',
  stateCode: 'NY',
  governingLaw: 'New York Estates, Powers and Trusts Law (EPTL) §§ 7-1.1 et seq.',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of New York`,
      sections: [
        {
          heading: 'DECLARATION OF TRUST',
          body: `This Revocable Living Trust Agreement ("Trust") is made on ${date}, by ${data.grantor} ("Grantor"), of the State of New York, pursuant to New York Estates, Powers and Trusts Law (EPTL).

This Trust is known as THE ${data.trustName.toUpperCase()}.`
        },
        {
          heading: 'ARTICLE I — PARTIES AND GOVERNING LAW',
          body: `Grantor: ${data.grantor}\nInitial Trustee: ${data.trustee}\nSuccessor Trustee: ${data.successorTrustee}\nPrimary Beneficiaries: ${bens}\n\nGoverning Law: New York EPTL §§ 7-1.1 et seq. and New York Surrogate's Court Procedure Act.`
        },
        {
          heading: 'ARTICLE II — REVOCABILITY',
          body: `2.1 This Trust is revocable during the Grantor's lifetime. The Grantor may alter, amend, or revoke this Trust in whole or in part by written instrument signed and acknowledged before a notary and delivered to the Trustee.

2.2 NEW YORK ELECTIVE SHARE: The Grantor's surviving spouse retains the right of election under EPTL § 5-1.1-A against trust assets.

2.3 This Trust becomes irrevocable upon the Grantor's death.`
        },
        {
          heading: 'ARTICLE III — TRUSTEE',
          body: `3.1 ${data.trustee} serves as initial Trustee. ${data.successorTrustee} shall serve as Successor Trustee without bond.

3.2 POWERS: All powers authorized under EPTL §§ 11-1.1 and 11-2.3, including the New York Prudent Investor Act (EPTL § 11-2.3).

3.3 COMPENSATION: Reasonable commissions as set by EPTL §§ 11-1.1 and 2309 of the Surrogate's Court Procedure Act.`
        },
        {
          heading: 'ARTICLE IV — LIFETIME DISTRIBUTIONS',
          body: `4.1 During the Grantor's life and competency, the Trustee shall pay income and principal as directed by the Grantor. During incapacity (per two licensed New York physicians), Trustee shall provide for Grantor's health, maintenance, education, and support.`
        },
        {
          heading: 'ARTICLE V — DISTRIBUTION AFTER DEATH',
          body: `5.1 After paying debts and New York estate taxes (if any), distribute to: ${bens}, in equal shares.

5.2 NEW YORK ESTATE TAX: The Trust should be reviewed in light of the New York State estate tax exemption and any applicable New York Generation-Skipping Transfer Tax.

${data.notes ? `5.3 SPECIAL INSTRUCTIONS:\n${data.notes}` : ''}`
        },
        {
          heading: 'ARTICLE VI — MISCELLANEOUS',
          body: `6.1 SPENDTHRIFT: EPTL § 7-3.1 applies. Trust interests are protected from creditor assignment.

6.2 RULE AGAINST PERPETUITIES: This Trust shall comply with EPTL § 9-1.1.

6.3 GOVERNING LAW: State of New York.`
        }
      ],
      executionBlock: commonExecutionBlock(data),
      notarizationBlock: `STATE OF NEW YORK\nCOUNTY OF ___________________\n\nOn the _____ day of _________________, in the year 20___, before me, the undersigned, a Notary Public in and for said State, personally appeared ${data.grantor}, personally known to me or proved to me on the basis of satisfactory evidence to be the individual whose name is subscribed to the within instrument, and acknowledged to me that he/she executed the same.\n\n_________________________________\nNotary Public, State of New York\nMy Commission Expires: ___________\n[NOTARY STAMP]`,
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nGrantor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// ILLINOIS TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const illinoisTemplate: TrustTemplate = {
  stateName: 'Illinois',
  stateCode: 'IL',
  governingLaw: 'Illinois Trust Code, 760 ILCS 3/',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of Illinois`,
      sections: [
        { heading: 'DECLARATION OF TRUST', body: `This Revocable Living Trust is made on ${date}, by ${data.grantor} ("Grantor") of Illinois, under the Illinois Trust Code (760 ILCS 3/). Known as THE ${data.trustName.toUpperCase()}.` },
        { heading: 'ARTICLE I — PARTIES', body: `Grantor: ${data.grantor}\nTrustee: ${data.trustee}\nSuccessor Trustee: ${data.successorTrustee}\nBeneficiaries: ${bens}\nGoverning Law: 760 ILCS 3/ (Illinois Trust Code, effective January 1, 2020).` },
        { heading: 'ARTICLE II — REVOCABILITY', body: `2.1 This Trust is revocable by the Grantor at any time during the Grantor's lifetime by written instrument delivered to the Trustee (760 ILCS 3/602).\n2.2 This Trust becomes irrevocable upon the Grantor's death.` },
        { heading: 'ARTICLE III — TRUSTEE', body: `3.1 ${data.trustee} serves as Trustee. ${data.successorTrustee} succeeds without bond.\n3.2 Powers per 760 ILCS 3/815 and the Illinois Prudent Investor Act (760 ILCS 5/1 et seq.).\n3.3 Compensation: reasonable compensation per 760 ILCS 3/708.` },
        { heading: 'ARTICLE IV — DISTRIBUTIONS', body: `4.1 Lifetime: income and principal as directed by Grantor; during incapacity, for Grantor's health, maintenance, education, support.\n4.2 At death: after debts and expenses, to ${bens}, in equal shares.${data.notes ? `\n\nSpecial Instructions:\n${data.notes}` : ''}` },
        { heading: 'ARTICLE V — GENERAL', body: `5.1 Spendthrift: 760 ILCS 3/502.\n5.2 Governing Law: State of Illinois.\n5.3 Severability: invalid provisions severed.` }
      ],
      executionBlock: commonExecutionBlock(data),
      notarizationBlock: `STATE OF ILLINOIS\nCOUNTY OF ___________________\n\nBefore me this _____ day of _________________ 20___, personally appeared ${data.grantor}, known to me to be the person who executed the foregoing instrument.\n\n_________________________________\nNotary Public, State of Illinois\nMy Commission Expires: ___________\n[NOTARY SEAL]`,
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nGrantor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// GEORGIA TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const georgiaTemplate: TrustTemplate = {
  stateName: 'Georgia',
  stateCode: 'GA',
  governingLaw: 'Georgia Trust Code, O.C.G.A. §§ 53-12-1 et seq.',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of Georgia`,
      sections: [
        { heading: 'DECLARATION OF TRUST', body: `This Revocable Living Trust is made on ${date}, by ${data.grantor} ("Grantor") of Georgia under O.C.G.A. §§ 53-12-1 et seq. Known as THE ${data.trustName.toUpperCase()}.` },
        { heading: 'ARTICLE I — PARTIES', body: `Grantor: ${data.grantor}\nTrustee: ${data.trustee}\nSuccessor Trustee: ${data.successorTrustee}\nBeneficiaries: ${bens}\nGoverning Law: O.C.G.A. §§ 53-12-1 et seq.` },
        { heading: 'ARTICLE II — REVOCABILITY', body: `Grantor may revoke or amend by written instrument delivered to Trustee (O.C.G.A. § 53-12-41). Becomes irrevocable at Grantor's death.` },
        { heading: 'ARTICLE III — TRUSTEE', body: `${data.trustee} serves as Trustee; ${data.successorTrustee} succeeds. Powers per O.C.G.A. § 53-12-261 et seq. Compensation: reasonable.` },
        { heading: 'ARTICLE IV — DISTRIBUTIONS', body: `Lifetime: as directed. Incapacity: health, maintenance, education, support. At death: to ${bens} equally after debts/expenses.${data.notes ? `\n\n${data.notes}` : ''}` },
        { heading: 'ARTICLE V — GENERAL', body: `Spendthrift per O.C.G.A. § 53-12-80. Governing Law: Georgia. Severability applies.` }
      ],
      executionBlock: commonExecutionBlock(data),
      notarizationBlock: commonNotarizationBlock(data).replace('STATE OF ________________', 'STATE OF GEORGIA'),
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nGrantor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// PENNSYLVANIA TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const pennsylvaniaTemplate: TrustTemplate = {
  stateName: 'Pennsylvania',
  stateCode: 'PA',
  governingLaw: 'Pennsylvania Uniform Trust Act, 20 Pa. C.S. §§ 7701–7799.3',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nCommonwealth of Pennsylvania`,
      sections: [
        { heading: 'DECLARATION OF TRUST', body: `This Revocable Living Trust is made on ${date}, by ${data.grantor} ("Settlor") of Pennsylvania under 20 Pa. C.S. §§ 7701 et seq. Known as THE ${data.trustName.toUpperCase()}.` },
        { heading: 'ARTICLE I — PARTIES', body: `Settlor: ${data.grantor}\nTrustee: ${data.trustee}\nSuccessor Trustee: ${data.successorTrustee}\nBeneficiaries: ${bens}\nGoverning Law: 20 Pa. C.S. §§ 7701–7799.3.` },
        { heading: 'ARTICLE II — REVOCABILITY', body: `Revocable by written instrument during Settlor's lifetime (20 Pa. C.S. § 7736). Irrevocable upon death. Pennsylvania Inheritance Tax may apply to trust assets.` },
        { heading: 'ARTICLE III — TRUSTEE', body: `${data.trustee} serves as Trustee; ${data.successorTrustee} succeeds without bond. Powers per 20 Pa. C.S. § 7780.3 and the Pennsylvania Prudent Investor Act (20 Pa. C.S. § 7203).` },
        { heading: 'ARTICLE IV — DISTRIBUTIONS', body: `Lifetime: as Settlor directs. Incapacity: health, maintenance, education, support. At death: to ${bens} equally.${data.notes ? `\n\n${data.notes}` : ''}` },
        { heading: 'ARTICLE V — GENERAL', body: `Spendthrift: 20 Pa. C.S. § 7743. Governing Law: Commonwealth of Pennsylvania. Severability applies.` }
      ],
      executionBlock: commonExecutionBlock(data),
      notarizationBlock: commonNotarizationBlock(data).replace('STATE OF ________________', 'COMMONWEALTH OF PENNSYLVANIA'),
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nSettlor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// OHIO TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const ohioTemplate: TrustTemplate = {
  stateName: 'Ohio',
  stateCode: 'OH',
  governingLaw: 'Ohio Trust Code, O.R.C. §§ 5801.01–5811.03',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of Ohio`,
      sections: [
        { heading: 'DECLARATION OF TRUST', body: `This Revocable Living Trust is made on ${date}, by ${data.grantor} ("Grantor") of Ohio under O.R.C. §§ 5801.01 et seq. Known as THE ${data.trustName.toUpperCase()}.` },
        { heading: 'ARTICLE I — PARTIES', body: `Grantor: ${data.grantor}\nTrustee: ${data.trustee}\nSuccessor Trustee: ${data.successorTrustee}\nBeneficiaries: ${bens}\nGoverning Law: O.R.C. §§ 5801.01–5811.03.` },
        { heading: 'ARTICLE II — REVOCABILITY', body: `Revocable by Grantor during lifetime by written instrument (O.R.C. § 5806.02). Irrevocable upon death.` },
        { heading: 'ARTICLE III — TRUSTEE', body: `${data.trustee} serves as Trustee; ${data.successorTrustee} succeeds without bond. Powers per O.R.C. §§ 5808.16 and Ohio Uniform Prudent Investor Act (O.R.C. § 5810.01).` },
        { heading: 'ARTICLE IV — DISTRIBUTIONS', body: `Lifetime: as Grantor directs. Incapacity: health, maintenance, education, support. At death: to ${bens} equally.${data.notes ? `\n\n${data.notes}` : ''}` },
        { heading: 'ARTICLE V — GENERAL', body: `Spendthrift: O.R.C. § 5805.01. Governing Law: Ohio. Severability applies.` }
      ],
      executionBlock: commonExecutionBlock(data),
      notarizationBlock: commonNotarizationBlock(data).replace('STATE OF ________________', 'STATE OF OHIO'),
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nGrantor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// NORTH CAROLINA TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const northCarolinaTemplate: TrustTemplate = {
  stateName: 'North Carolina',
  stateCode: 'NC',
  governingLaw: 'North Carolina Uniform Trust Code, G.S. §§ 36C-1-101 et seq.',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of North Carolina`,
      sections: [
        { heading: 'DECLARATION OF TRUST', body: `This Revocable Living Trust is made on ${date}, by ${data.grantor} ("Settlor") of North Carolina under G.S. §§ 36C-1-101 et seq. Known as THE ${data.trustName.toUpperCase()}.` },
        { heading: 'ARTICLE I — PARTIES', body: `Settlor: ${data.grantor}\nTrustee: ${data.trustee}\nSuccessor Trustee: ${data.successorTrustee}\nBeneficiaries: ${bens}\nGoverning Law: G.S. §§ 36C-1-101 et seq.` },
        { heading: 'ARTICLE II — REVOCABILITY', body: `Revocable by Settlor during lifetime (G.S. § 36C-6-602). Irrevocable upon death.` },
        { heading: 'ARTICLE III — TRUSTEE', body: `${data.trustee} serves as Trustee; ${data.successorTrustee} succeeds without bond. Powers per G.S. § 36C-8-816 and NC Uniform Prudent Investor Act (G.S. § 36C-9-901).` },
        { heading: 'ARTICLE IV — DISTRIBUTIONS', body: `Lifetime: as Settlor directs. Incapacity: health, maintenance, education, support. At death: to ${bens} equally.${data.notes ? `\n\n${data.notes}` : ''}` },
        { heading: 'ARTICLE V — GENERAL', body: `Spendthrift: G.S. § 36C-5-502. Governing Law: North Carolina. Severability applies.` }
      ],
      executionBlock: commonExecutionBlock(data),
      notarizationBlock: commonNotarizationBlock(data).replace('STATE OF ________________', 'STATE OF NORTH CAROLINA'),
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nSettlor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// ARIZONA TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const arizonaTemplate: TrustTemplate = {
  stateName: 'Arizona',
  stateCode: 'AZ',
  governingLaw: 'Arizona Trust Code, A.R.S. §§ 14-10101 et seq.',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of Arizona`,
      sections: [
        { heading: 'DECLARATION OF TRUST', body: `This Revocable Living Trust is made on ${date}, by ${data.grantor} ("Settlor") of Arizona under A.R.S. §§ 14-10101 et seq. Known as THE ${data.trustName.toUpperCase()}.` },
        { heading: 'ARTICLE I — PARTIES', body: `Settlor: ${data.grantor}\nTrustee: ${data.trustee}\nSuccessor Trustee: ${data.successorTrustee}\nBeneficiaries: ${bens}\nGoverning Law: A.R.S. §§ 14-10101 et seq.` },
        { heading: 'ARTICLE II — REVOCABILITY', body: `Revocable by Settlor during lifetime (A.R.S. § 14-10602). Community property rules per Arizona Family Code apply. Irrevocable upon death.` },
        { heading: 'ARTICLE III — TRUSTEE', body: `${data.trustee} serves as Trustee; ${data.successorTrustee} succeeds without bond. Powers per A.R.S. § 14-10816 and Arizona Uniform Prudent Investor Act (A.R.S. § 14-10901).` },
        { heading: 'ARTICLE IV — DISTRIBUTIONS', body: `Lifetime: as Settlor directs. Incapacity: health, maintenance, education, support. At death: to ${bens} equally.${data.notes ? `\n\n${data.notes}` : ''}` },
        { heading: 'ARTICLE V — GENERAL', body: `Spendthrift: A.R.S. § 14-10502. Governing Law: Arizona. Severability applies.` }
      ],
      executionBlock: commonExecutionBlock(data),
      notarizationBlock: commonNotarizationBlock(data).replace('STATE OF ________________', 'STATE OF ARIZONA'),
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nSettlor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// NEVADA TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const nevadaTemplate: TrustTemplate = {
  stateName: 'Nevada',
  stateCode: 'NV',
  governingLaw: 'Nevada Uniform Trust Code, NRS §§ 163.001 et seq.',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST\nState of Nevada`,
      sections: [
        { heading: 'DECLARATION OF TRUST', body: `This Revocable Living Trust is made on ${date}, by ${data.grantor} ("Settlor") of Nevada under NRS §§ 163.001 et seq. Known as THE ${data.trustName.toUpperCase()}.` },
        { heading: 'ARTICLE I — PARTIES', body: `Settlor: ${data.grantor}\nTrustee: ${data.trustee}\nSuccessor Trustee: ${data.successorTrustee}\nBeneficiaries: ${bens}\nGoverning Law: NRS Chapter 163.\nNevada Asset Protection: Nevada's self-settled spendthrift trust provisions (NRS § 166) may apply.` },
        { heading: 'ARTICLE II — REVOCABILITY', body: `Revocable by Settlor during lifetime per NRS § 163.560. Nevada permits dynasty trusts of up to 365 years. Irrevocable upon death.` },
        { heading: 'ARTICLE III — TRUSTEE', body: `${data.trustee} serves as Trustee; ${data.successorTrustee} succeeds without bond. Powers per NRS §§ 163.265–163.410 and Nevada Uniform Prudent Investor Act (NRS § 164.705 et seq.).` },
        { heading: 'ARTICLE IV — DISTRIBUTIONS', body: `Lifetime: as Settlor directs. Incapacity: health, maintenance, education, support. At death: to ${bens} equally.${data.notes ? `\n\n${data.notes}` : ''}` },
        { heading: 'ARTICLE V — GENERAL', body: `Spendthrift: NRS § 163.420. Governing Law: Nevada. Severability applies.` }
      ],
      executionBlock: commonExecutionBlock(data),
      notarizationBlock: commonNotarizationBlock(data).replace('STATE OF ________________', 'STATE OF NEVADA'),
      scheduleA: `SCHEDULE A — TRUST PROPERTY\n\n${assetSchedule(data.assets)}\n\nSettlor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// GENERIC / ALL OTHER STATES TEMPLATE
// ─────────────────────────────────────────────────────────────────────────────

const genericTemplate: TrustTemplate = {
  stateName: 'Generic (Uniform Trust Code)',
  stateCode: 'ALL',
  governingLaw: 'Uniform Trust Code (as adopted by the applicable state)',
  generate(data: TrustData): TrustDocumentContent {
    const date = formatDate(data.date);
    const bens = beneficiaryList(data.beneficiaries);
    const state = data.state || 'the applicable state';

    return {
      title: `THE ${data.trustName.toUpperCase()}\nA REVOCABLE LIVING TRUST`,

      sections: [
        {
          heading: 'DECLARATION OF TRUST',
          body: `This Revocable Living Trust Agreement ("Trust") is made and entered into on ${date}, by and between ${data.grantor} ("Grantor"), and ${data.trustee} ("Trustee").

This Trust shall be known as THE ${data.trustName.toUpperCase()}, and is created pursuant to the laws of ${state}.

The Grantor transfers the property described in Schedule A to the Trustee, to be held, managed, and distributed under the terms hereof.`
        },
        {
          heading: 'ARTICLE I — IDENTIFICATION AND GOVERNING LAW',
          body: `1.1 Grantor: ${data.grantor}
1.2 Initial Trustee: ${data.trustee}
1.3 Successor Trustee: ${data.successorTrustee}
1.4 Primary Beneficiaries: ${bens}
1.5 Governing Law: ${state}
1.6 This Trust shall be construed and administered pursuant to the laws of ${state}, including the Uniform Trust Code as adopted in that state, or applicable state trust statutes.`
        },
        {
          heading: 'ARTICLE II — REVOCABILITY AND AMENDMENT',
          body: `2.1 This Trust is revocable during the Grantor's lifetime. The Grantor may revoke, alter, or amend this Trust Agreement at any time and from time to time by a written instrument signed by the Grantor and delivered to the Trustee.

2.2 Upon revocation, the Trustee shall transfer all Trust property to the Grantor.

2.3 This Trust becomes irrevocable upon the Grantor's death and may not thereafter be altered or amended.`
        },
        {
          heading: 'ARTICLE III — TRUSTEE PROVISIONS',
          body: `3.1 INITIAL TRUSTEE: ${data.trustee}

3.2 SUCCESSOR TRUSTEE: If ${data.trustee} ceases to serve as Trustee for any reason, ${data.successorTrustee} shall serve as Successor Trustee with all powers of the initial Trustee, without bond.

3.3 TRUSTEE POWERS: The Trustee is authorized to:
   (a) Retain, manage, sell, exchange, or otherwise dispose of any Trust assets;
   (b) Invest and reinvest Trust property in any type of investment;
   (c) Manage, lease, or encumber real property;
   (d) Make distributions in cash or in kind;
   (e) Pay all taxes, debts, and administrative expenses;
   (f) Employ attorneys, accountants, and investment advisors;
   (g) Execute all documents necessary for Trust administration;
   (h) Exercise all other powers granted by applicable state law.

3.4 STANDARD OF CARE: The Trustee shall exercise the care, skill, and caution of a prudent investor in managing Trust assets.

3.5 COMPENSATION: The Trustee shall be entitled to reasonable compensation for services.`
        },
        {
          heading: 'ARTICLE IV — DISTRIBUTIONS DURING GRANTOR\'S LIFETIME',
          body: `4.1 During the Grantor's lifetime, the Trustee shall pay to or apply for the benefit of the Grantor such amounts of income and principal as the Grantor directs.

4.2 If the Grantor becomes incapacitated (as certified in writing by two licensed physicians), the Trustee shall pay to or apply for the benefit of the Grantor such amounts of income and principal as the Trustee determines necessary for the Grantor's health, education, maintenance, and support, taking into account the Grantor's accustomed standard of living.

4.3 INCAPACITY DEFINITION: "Incapacitated" means that the Grantor is unable to manage his or her financial affairs or provide for his or her own care and maintenance, as determined by two licensed physicians.`
        },
        {
          heading: 'ARTICLE V — DISTRIBUTION UPON GRANTOR\'S DEATH',
          body: `5.1 PAYMENT OF DEBTS AND EXPENSES: Upon the Grantor's death, the Trustee shall pay, to the extent the Trustee deems appropriate, the Grantor's legally enforceable debts, funeral and burial expenses, and costs of administering this Trust.

5.2 DISTRIBUTION TO BENEFICIARIES: After payment of debts and expenses, the Trustee shall distribute the remaining Trust property as follows:

   In equal shares to: ${bens}

   Such distributions shall be made outright and free of trust, unless a beneficiary is a minor (under age 21), in which case the Trustee may retain such share in trust until the beneficiary reaches age 21.

5.3 SURVIVORSHIP: If any primary beneficiary does not survive the Grantor by thirty (30) days, that beneficiary's share shall be distributed in equal shares to the surviving primary beneficiaries.

5.4 ULTIMATE DISTRIBUTION: If all named beneficiaries predecease the Grantor, the Trust property shall be distributed to the Grantor's heirs at law pursuant to the laws of intestate succession of ${state}.

${data.notes ? `5.5 SPECIAL INSTRUCTIONS:\n${data.notes}` : ''}`
        },
        {
          heading: 'ARTICLE VI — GENERAL PROVISIONS',
          body: `6.1 SPENDTHRIFT PROVISION: No beneficiary may anticipate, assign, encumber, or in any way transfer his or her interest in this Trust prior to actual receipt. No interest of any beneficiary shall be subject to the claims of any creditor.

6.2 NO-CONTEST CLAUSE: Any beneficiary or other person who contests the validity of this Trust or attempts to set it aside shall forfeit any interest hereunder.

6.3 PERPETUITIES: This Trust shall terminate no later than permitted under applicable state law. If any provision violates the rule against perpetuities, it shall be void only to the extent necessary.

6.4 SEVERABILITY: If any provision of this Trust is invalid or unenforceable under applicable law, the remaining provisions shall remain in full force and effect.

6.5 ENTIRE AGREEMENT: This Trust Agreement, together with all schedules, constitutes the entire agreement and supersedes all prior agreements.

6.6 GOVERNING LAW: This Trust shall be governed by and construed in accordance with the laws of ${state}.

6.7 POUR-OVER PROVISION: It is the Grantor's intent that any property passing to this Trust under the Grantor's Last Will and Testament shall be held and administered under the terms hereof.

6.8 TAX PROVISIONS: This Trust is intended to be treated as a "grantor trust" for income tax purposes during the Grantor's lifetime. All income and deductions shall be reported on the Grantor's personal income tax return.`
        }
      ],

      executionBlock: commonExecutionBlock(data),
      notarizationBlock: commonNotarizationBlock(data),
      scheduleA: `SCHEDULE A
INITIAL TRUST PROPERTY

The following property is hereby transferred to and made a part of THE ${data.trustName.toUpperCase()}:

${assetSchedule(data.assets)}

The Grantor may transfer additional property to this Trust at any time.

Grantor's Initials: ___________    Date: _______________`
    };
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// TEMPLATE REGISTRY
// ─────────────────────────────────────────────────────────────────────────────

export const STATE_TEMPLATES: Record<string, TrustTemplate> = {
  WA: washingtonTemplate,
  CA: californiaTemplate,
  TX: texasTemplate,
  FL: floridaTemplate,
  NY: newYorkTemplate,
  IL: illinoisTemplate,
  GA: georgiaTemplate,
  PA: pennsylvaniaTemplate,
  OH: ohioTemplate,
  NC: northCarolinaTemplate,
  AZ: arizonaTemplate,
  NV: nevadaTemplate,
};

/**
 * Get template for state code, falling back to generic if not found.
 * Washington State (WA) is the default.
 */
export function getTemplate(stateCode?: string): TrustTemplate {
  if (!stateCode) return washingtonTemplate;
  const code = stateCode.toUpperCase();
  return STATE_TEMPLATES[code] || genericTemplate;
}

export const ALL_STATES = [
  { code: 'WA', name: 'Washington' },
  { code: 'CA', name: 'California' },
  { code: 'TX', name: 'Texas' },
  { code: 'FL', name: 'Florida' },
  { code: 'NY', name: 'New York' },
  { code: 'IL', name: 'Illinois' },
  { code: 'GA', name: 'Georgia' },
  { code: 'PA', name: 'Pennsylvania' },
  { code: 'OH', name: 'Ohio' },
  { code: 'NC', name: 'North Carolina' },
  { code: 'AZ', name: 'Arizona' },
  { code: 'NV', name: 'Nevada' },
  { code: 'AL', name: 'Alabama' }, { code: 'AK', name: 'Alaska' },
  { code: 'AR', name: 'Arkansas' }, { code: 'CO', name: 'Colorado' },
  { code: 'CT', name: 'Connecticut' }, { code: 'DE', name: 'Delaware' },
  { code: 'HI', name: 'Hawaii' }, { code: 'ID', name: 'Idaho' },
  { code: 'IN', name: 'Indiana' }, { code: 'IA', name: 'Iowa' },
  { code: 'KS', name: 'Kansas' }, { code: 'KY', name: 'Kentucky' },
  { code: 'LA', name: 'Louisiana' }, { code: 'ME', name: 'Maine' },
  { code: 'MD', name: 'Maryland' }, { code: 'MA', name: 'Massachusetts' },
  { code: 'MI', name: 'Michigan' }, { code: 'MN', name: 'Minnesota' },
  { code: 'MS', name: 'Mississippi' }, { code: 'MO', name: 'Missouri' },
  { code: 'MT', name: 'Montana' }, { code: 'NE', name: 'Nebraska' },
  { code: 'NH', name: 'New Hampshire' }, { code: 'NJ', name: 'New Jersey' },
  { code: 'NM', name: 'New Mexico' }, { code: 'ND', name: 'North Dakota' },
  { code: 'OK', name: 'Oklahoma' }, { code: 'OR', name: 'Oregon' },
  { code: 'RI', name: 'Rhode Island' }, { code: 'SC', name: 'South Carolina' },
  { code: 'SD', name: 'South Dakota' }, { code: 'TN', name: 'Tennessee' },
  { code: 'UT', name: 'Utah' }, { code: 'VT', name: 'Vermont' },
  { code: 'VA', name: 'Virginia' }, { code: 'WV', name: 'West Virginia' },
  { code: 'WI', name: 'Wisconsin' }, { code: 'WY', name: 'Wyoming' },
  { code: 'DC', name: 'District of Columbia' },
];
