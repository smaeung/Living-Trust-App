# Living Trust App

AI-powered, multi-platform application for creating, reviewing, and purchasing official Living Trust documents — with state-specific legal templates, Stripe payment, and watermark-free PDF download.

---

## What the App Does

| Capability | Description |
|---|---|
| Guided Trust Wizard | 5-step form: trust name, state, grantor, trustee, beneficiaries, assets |
| State Templates | State-specific legal documents for all 50 US states (12 dedicated + Uniform Trust Code fallback) |
| PDF Preview | Watermarked preview so the user can review the document before paying |
| Stripe Payment | Secure $29.99 one-time purchase |
| Official PDF Download | 24-hour token issued after payment — downloads unwatermarked, print-ready PDF |
| AI Lawyer Assistant | GPT-4 powered chat for legal Q&A |
| AI Document Review | Paste any trust text for AI scoring and recommendations |
| Document Management | Upload and manage trust-related files |

---

## Platforms

| Platform | Technology | Directory |
|---|---|---|
| iOS | React Native (Expo) | `frontend/` |
| Android | React Native (Expo) | `frontend/` |
| Web (PWA) | React Native Web | `frontend/` |
| Android Native | Kotlin / Jetpack Compose | `android/` |
| Backend API | Express.js / TypeScript | `backend/` |

---

## User Flow — PDF & Payment

```
Home Screen → "Create Trust"
        ↓
TrustWizard (5 steps)
  Step 1: Trust name + State (WA default) + Trust type
  Step 2: Grantor name + address
  Step 3: Beneficiaries
  Step 4: Successor Trustee
  Step 5: Assets + review summary
        ↓
PdfPreviewScreen
  ├─ State selector (12 dedicated templates, all 50 states)
  ├─ POST /api/pdf/preview-base64 → watermarked PDF
  ├─ Document structure preview (10 sections)
  ├─ Governing law citation
  └─ "Purchase for $29.99"
        ↓
PaymentScreen (Stripe)
  ├─ POST /api/payments/create-intent → PaymentIntent
  ├─ Card entry → POST /api/payments/confirm
  └─ Issues 24-hour download token
        ↓
Success → GET /api/pdf/download/:token
  → Clean PDF, no watermark, ready to print & sign
```

---

## Getting Started

### Prerequisites

- Node.js 18+
- npm 9+
- Expo CLI: `npm install -g expo-cli`
- Android Studio (for Kotlin native build)
- Stripe account (free test keys at stripe.com)

### Backend

```bash
cd backend
npm install
cp .env.example .env   # add your keys
npm run dev            # http://localhost:3001
```

### Frontend (React Native / Expo)

```bash
cd frontend
npm install
npm start              # Expo dev server
npm run android        # Android emulator
npm run ios            # iOS simulator
npm run web            # Browser (PWA)
```

### Native Android (Kotlin)

```bash
cd android
./gradlew assembleDebug        # Build debug APK
./gradlew test                 # Run all unit tests (no device needed)
./gradlew connectedAndroidTest # Instrumented tests (needs emulator)
```

Open in Android Studio: **File → Open → `android/`**

---

## Environment Variables

### `backend/.env`

```env
PORT=3001
MONGODB_URI=mongodb://localhost:27017/livingtrust
JWT_SECRET=your-secret-key-min-32-chars
OPENAI_API_KEY=sk-...

# Stripe — omit for dev mock mode (pi_mock_* accepted automatically)
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

> **Dev mode (no Stripe keys):** Backend returns a mock `pi_mock_*` PaymentIntent. The full flow runs end-to-end without a real Stripe account.

> **Test card:** `4242 4242 4242 4242` · Any future expiry · Any CVC

---

## Project Structure

```
Living-Trust-App/
├── frontend/                         # React Native / Expo
│   ├── App.tsx                       # Navigation (8 screens)
│   └── src/screens/
│       ├── HomeScreen.tsx
│       ├── TrustWizardScreen.tsx     # Wizard + state selector
│       ├── PdfPreviewScreen.tsx      # Watermarked preview + purchase
│       ├── PaymentScreen.tsx         # Stripe payment + download
│       ├── ReviewScreen.tsx          # AI document analysis
│       ├── AiAssistantScreen.tsx     # GPT-4 chat
│       ├── DocumentsScreen.tsx
│       └── SettingsScreen.tsx
│
├── backend/                          # Express.js API
│   └── src/
│       ├── index.ts                  # Server entry — port 3001
│       ├── routes/
│       │   ├── authRoutes.ts
│       │   ├── trustRoutes.ts
│       │   ├── documentRoutes.ts
│       │   ├── aiRoutes.ts
│       │   ├── userRoutes.ts
│       │   ├── pdfRoutes.ts          # PDF generation + watermark + download
│       │   └── paymentRoutes.ts      # Stripe intent + confirm + webhook
│       └── templates/
│           └── livingTrustTemplates.ts  # 50-state template engine
│
├── android/                          # Native Kotlin / Jetpack Compose
│   └── app/src/main/java/com/livingtrust/app/
│       ├── presentation/
│       │   ├── auth/                 # Login + Register
│       │   ├── home/
│       │   ├── trust/                # TrustWizard + ViewModel
│       │   ├── pdf/                  # PdfPreview + ViewModel
│       │   ├── payment/              # Payment + ViewModel
│       │   ├── ai/
│       │   ├── documents/
│       │   ├── settings/
│       │   └── navigation/           # NavGraph + Routes + NavDataStore
│       ├── domain/                   # Models, use cases, repository interfaces
│       ├── data/                     # Retrofit, Room, repositories
│       ├── di/                       # Hilt modules
│       └── util/                     # Resource<T>, TokenManager
│
├── agents/
│   └── ux-test-runner.js             # 11 Playwright UX tests
├── CLAUDE.md                         # AI assistant guidance
├── AUDIT.md                          # Code quality audit
└── README.md                         # This file
```

---

## API Reference

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login → JWT (7-day) |
| GET | `/api/auth/me` | Current user profile |

### PDF Generation

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/pdf/states` | List all supported states |
| POST | `/api/pdf/preview` | Watermarked PDF (binary stream) |
| POST | `/api/pdf/preview-base64` | Watermarked PDF (base64 JSON, for mobile) |
| POST | `/api/pdf/issue-download-token` | Issue 24-hour download token (call after payment) |
| GET | `/api/pdf/download/:token` | Official clean PDF (no watermark) |
| GET | `/api/pdf/download-base64/:token` | Official PDF as base64 (mobile) |

### Payments

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/payments/config` | Stripe publishable key + price |
| POST | `/api/payments/create-intent` | Create Stripe PaymentIntent |
| POST | `/api/payments/confirm` | Verify payment → issue download token |
| POST | `/api/payments/webhook` | Stripe webhook (raw body required) |
| GET | `/api/payments/status/:id` | Check payment status |

### Trusts, Documents, AI, Users

| Method | Endpoint | Description |
|---|---|---|
| GET/POST | `/api/trusts` | List / create trusts |
| GET/PUT/DELETE | `/api/trusts/:id` | Read / update / delete trust |
| GET/POST | `/api/documents` | List / upload documents |
| DELETE | `/api/documents/:id` | Delete document |
| POST | `/api/ai/chat` | AI lawyer chat (GPT-4) |
| POST | `/api/ai/analyze` | Document analysis + scoring |
| GET/PUT | `/api/users/:id` | User profile |
| GET | `/health` | Health check |

**Auth header:** `Authorization: Bearer <JWT>` on all protected routes.

---

## Living Trust Templates

Washington State (WA) is the **default** — shown first in all state selectors.

| State | Code | Governing Statute |
|---|---|---|
| **Washington** | **WA** | **RCW Chapter 11.98 (default)** |
| California | CA | Probate Code §§ 15000–19530 |
| Texas | TX | Property Code §§ 111.001–116.172 |
| Florida | FL | F.S. §§ 736.0101–736.1303 |
| New York | NY | EPTL §§ 7-1.1 et seq. |
| Illinois | IL | 760 ILCS 3/ (Illinois Trust Code) |
| Georgia | GA | O.C.G.A. §§ 53-12-1 et seq. |
| Pennsylvania | PA | 20 Pa. C.S. §§ 7701–7799.3 |
| Ohio | OH | O.R.C. §§ 5801.01–5811.03 |
| North Carolina | NC | G.S. §§ 36C-1-101 et seq. |
| Arizona | AZ | A.R.S. §§ 14-10101 et seq. |
| Nevada | NV | NRS §§ 163.001 et seq. |
| All other states | — | Uniform Trust Code (generic) |

Each document contains: Title page, Articles I–VI (declaration, revocability, trustee, lifetime distributions, distribution at death, general provisions), Execution page, Notarization block, Schedule A (assets).

---

## Android Security Controls

| Control | File | Details |
|---|---|---|
| Network Security Config | `res/xml/network_security_config.xml` | Cleartext HTTP allowed **only** to `10.0.2.2` / `localhost` (emulator); all other traffic enforces TLS |
| Backup Exclusions | `res/xml/data_extraction_rules.xml` | JWT token (`living_trust_prefs`) and Room DB excluded from cloud backup and device transfer |
| Secure Token Storage | `util/TokenManager.kt` | JWT stored in Jetpack DataStore (async, never blocks UI thread) |
| ProGuard / R8 | `proguard-rules.pro` | Stripe, Retrofit, Gson, Hilt, Room preserved; `Log.d/v/i` stripped in release builds |
| allowBackup | `AndroidManifest.xml` | `false` — prevents `adb backup` extraction of app data |
| Permissions | `AndroidManifest.xml` | Minimal: `INTERNET`, `READ_EXTERNAL_STORAGE` (≤ API 32), `READ_MEDIA_IMAGES` (API 33+) |

---

## Testing

### Android Unit Tests

```bash
cd android && ./gradlew test
```

| File | Coverage |
|---|---|
| `AuthViewModelTest.kt` | Login/register, error states, state flow (4 tests) |
| `TrustViewModelTest.kt` | Wizard navigation, beneficiary/asset CRUD, submit success/error (8 tests) |
| `AiViewModelTest.kt` | AI message flow |
| `UseCaseValidationTest.kt` | Business logic — blank fields, short passwords, trust name (6 tests) |
| `PaymentViewModelTest.kt` | Card/expiry/CVC formatters, 6 validation guards, retry, field updates (14 tests) |
| `PdfPreviewViewModelTest.kt` | WA default, state selection, ALL_TRUST_STATES integrity (9 tests) |

### Playwright UX Tests

```bash
npm install           # from repo root
node agents/ux-test-runner.js
# → outputs ux-test-report.json (11 tests: Home, Documents, Settings)
```

### TypeScript Build Check

```bash
cd backend && npm run build       # compiles TS, catches type errors
cd frontend && npx tsc --noEmit   # type-checks frontend
```

---

## Technology Stack

### Frontend
| Library | Version | Purpose |
|---|---|---|
| React Native | 0.76.6 | Cross-platform mobile |
| Expo | ^54.0.33 | Build tooling |
| React Navigation | ^7.0.0 | Screen routing |
| Axios | ^1.7.0 | HTTP client |
| TypeScript | ~5.3.0 | Type safety |

### Backend
| Library | Version | Purpose |
|---|---|---|
| Express | ^4.21.0 | HTTP server |
| PDFKit | latest | PDF generation + watermarking |
| Stripe | latest | Payment processing |
| jsonwebtoken | ^9.0.2 | JWT auth |
| bcryptjs | ^2.4.3 | Password hashing |
| OpenAI SDK | ^4.50.0 | GPT-4 AI |
| Helmet | ^7.1.0 | Security headers |
| Morgan | ^1.10.0 | HTTP logging |

### Native Android
| Library | Purpose |
|---|---|
| Kotlin 2.0 | Primary language |
| Jetpack Compose + Material3 | Declarative UI |
| Hilt (Dagger2) | Dependency injection |
| Retrofit 2 + OkHttp 4 | Networking |
| Room 2.6 | Local database |
| Kotlin Coroutines + Flow | Async state management |
| Stripe Android SDK 21.3.1 | Payment processing |
| DataStore Preferences | Secure JWT storage |
| Navigation Compose | In-app navigation |

---

## Legal Disclaimer

Documents generated by this application provide a legal framework based on publicly available trust statutes. They are for **educational and informational purposes only** and do not constitute legal advice. Always consult a licensed attorney in your state before executing any legal document.

---

## License

MIT

---

*Built to help people secure their legacy — simply, affordably, and across every platform.*
