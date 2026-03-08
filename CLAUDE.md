# CLAUDE.md — Living Trust App

This file provides guidance for AI assistants (e.g., Claude Code) working in this repository.

---

## Project Overview

**Living Trust App** is a full-stack, multi-platform application for AI-powered living trust creation and management. It enables users to create, review, and manage legal living trust documents with AI guidance.

**Platforms:** iOS, Android (React Native via Expo), Web (react-native-web), Native Android (Kotlin/Jetpack Compose)

---

## Repository Structure

```
Living-Trust-App/
├── frontend/                # React Native (Expo) app — iOS, Android, Web
│   ├── App.tsx              # Root navigation setup
│   ├── main.tsx             # Expo entry point (registers App)
│   ├── src/screens/         # Screen components (6 screens)
│   ├── package.json         # Frontend dependencies
│   └── tsconfig.json        # TypeScript config (extends expo/tsconfig.base)
├── backend/                 # Express.js REST API server
│   ├── src/
│   │   ├── index.ts         # Server entry point (port 3001)
│   │   ├── routes/          # Route handlers (auth, trusts, documents, ai, users)
│   │   └── middleware/      # Custom Express middleware
│   ├── package.json         # Backend dependencies
│   └── tsconfig.json        # TypeScript config (target: ES2020, CommonJS)
├── android/                 # Native Kotlin/Jetpack Compose Android app
│   ├── app/src/main/java/com/livingtrust/app/
│   │   ├── data/            # Data layer: Retrofit, Room, repositories
│   │   ├── domain/          # Domain layer: use cases, entities
│   │   └── presentation/    # UI layer: ViewModels, Composables
│   ├── app/build.gradle.kts # Gradle build config
│   └── README.md            # Android-specific architecture docs
├── agents/                  # UX test automation scripts
│   └── ux-test-runner.js    # Playwright-based UX test runner
├── capture-screenshots.js   # Playwright screenshot capture script
├── package.json             # Root-level (Playwright only)
├── README.md                # Project overview and setup guide
├── AUDIT.md                 # Code audit report (Feb 17, 2026)
└── GITHUB-SETUP.md          # Git workflow instructions
```

---

## Technology Stack

### Frontend (React Native / Expo)
| Technology | Version | Purpose |
|---|---|---|
| React Native | 0.76.6 | Cross-platform mobile framework |
| Expo | ^54.0.33 | Build platform and development tooling |
| React | ^19.1.0 | UI library |
| React Navigation | ^7.0.0 (native-stack) | Screen routing and navigation |
| Axios | ^1.7.0 | HTTP client for API calls |
| TypeScript | ~5.3.0 | Type-safe JavaScript |
| react-native-web | ^0.19.13 | Web compatibility layer |

### Backend (Express.js)
| Technology | Version | Purpose |
|---|---|---|
| Express | ^4.21.0 | HTTP server framework |
| Mongoose | ^8.5.0 | MongoDB ODM |
| jsonwebtoken | ^9.0.2 | JWT authentication |
| bcryptjs | ^2.4.3 | Password hashing |
| OpenAI SDK | ^4.50.0 | GPT-4 AI integration |
| Helmet | ^7.1.0 | Security headers |
| Morgan | ^1.10.0 | HTTP request logging |
| TypeScript | ~5.3.0 | Type-safe JavaScript |

### Native Android (Kotlin)
| Technology | Version | Purpose |
|---|---|---|
| Kotlin | 2.0 | Primary language |
| Jetpack Compose + Material3 | Latest | Declarative UI |
| Hilt (Dagger2) | Latest | Dependency injection |
| Retrofit 2 + OkHttp 4 | Latest | Networking |
| Room | 2.6 | Local database |
| Kotlin Coroutines + Flow | Latest | Async programming |
| Navigation Compose | Latest | In-app navigation |
| DataStore Preferences | Latest | Auth token storage |
| Gradle | 8.5 (KTS) | Build tool |

---

## Development Workflows

### Frontend

```bash
cd frontend
npm install         # Install dependencies

npm start           # Start Expo development server
npm run android     # Launch on Android emulator/device
npm run ios         # Launch on iOS simulator/device
npm run web         # Launch in browser

npm run build:android   # Build Android APK
npm run build:ios       # Build iOS IPA
```

### Backend

```bash
cd backend
npm install         # Install dependencies

npm run dev         # Start dev server with hot-reload (ts-node-dev)
npm run build       # Compile TypeScript to dist/
npm start           # Run compiled JS (production)
npm run clean       # Remove dist/ directory
```

Backend runs on **port 3001** by default.

### Android (Native Kotlin)

```bash
cd android

./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
./gradlew test               # Run unit tests (no device needed)
./gradlew connectedAndroidTest   # Run instrumented tests (needs emulator/device)
```

Open in Android Studio: File → Open → `android/` directory.

### Playwright / Screenshot Automation

```bash
# Root level
npm install
node capture-screenshots.js        # Capture UI screenshots (375px mobile + 1280px desktop)
node agents/ux-test-runner.js      # Run 11 UX tests and generate JSON report
```

---

## Environment Variables

### Backend (`backend/.env`)

```
PORT=3001
MONGODB_URI=mongodb://localhost:27017/livingtrust
JWT_SECRET=your-secret-key-here
OPENAI_API_KEY=sk-...
```

> **Note:** `.env` files are git-ignored. Never commit secrets to the repository.

### Frontend

No `.env` file currently. The API base URL is set programmatically. For production, set via Expo's environment variable system.

### Android

The backend `BASE_URL` is set in `android/app/build.gradle.kts`:
- **Debug:** `http://10.0.2.2:3001/` (emulator → localhost mapping)
- **Release:** Override via build flavor or CI environment

---

## API Endpoints

The backend exposes a REST API under `/api/`:

| Endpoint | Description |
|---|---|
| `GET /health` | Health check |
| `POST /api/auth/register` | User registration |
| `POST /api/auth/login` | User login (returns JWT) |
| `GET /api/auth/profile` | Get current user profile |
| `GET/POST /api/trusts` | List / create living trusts |
| `GET/PUT/DELETE /api/trusts/:id` | Get / update / delete a trust |
| `GET/POST /api/documents` | List / upload documents |
| `DELETE /api/documents/:id` | Delete a document |
| `POST /api/ai` | Send AI chat message (GPT-4) |
| `POST /api/ai/analyze` | AI document analysis |
| `GET/PUT /api/users/:id` | Get / update user |

**Authentication:** JWT Bearer token (7-day expiry). Include in header: `Authorization: Bearer <token>`

> **Important:** The current backend uses **in-memory mock data arrays** — not a real database. MongoDB integration is planned but not yet connected.

---

## Application Screens

The React Native frontend has 6 screens configured in `App.tsx`:

| Screen | Route Name | Description |
|---|---|---|
| HomeScreen | `Home` | Landing page with action cards |
| TrustWizardScreen | `TrustWizard` | 5-step trust creation wizard |
| ReviewScreen | `Review` | Review trust before finalizing |
| AiAssistantScreen | `AiAssistant` | Chat interface with GPT-4 |
| DocumentsScreen | `Documents` | Document upload and management |
| SettingsScreen | `Settings` | App configuration |

Navigation header is dark blue (`#1a365d`), white title/icons.

---

## Code Conventions

### TypeScript (Frontend & Backend)
- **Variables/functions:** `camelCase`
- **Classes/interfaces/types:** `PascalCase`
- **Interfaces:** No `I` prefix (e.g., `UserData`, not `IUserData`)
- **Constants:** `SCREAMING_SNAKE_CASE`
- **TypeScript strict mode** is enabled in the backend; be explicit with types.

### React Native Components
- Use **functional components** with hooks (`useState`, `useEffect`)
- Styles via **`StyleSheet.create()`** — no inline style objects
- Primary color: `#1a365d` (dark blue)
- Form validation: show `Alert.alert()` for required fields
- Navigation via `useNavigation()` hook; use `RootStackParamList` type

### Backend Routes
- Follow MVC-like pattern: `routes/` → `controllers/` → `services/`
- Use `try/catch` in all async route handlers
- Return JSON with consistent structure: `{ success, data, message }`
- Use `express.Router()` for each route group

### Android (Kotlin)
- **Architecture:** Clean Architecture — data / domain / presentation layers
- **DI:** Hilt — annotate ViewModels with `@HiltViewModel`, services with `@Singleton`
- **State:** `StateFlow` + sealed `UiState` in ViewModels
- **Network:** Retrofit `suspend` functions wrapped in `Resource<T>` sealed class
- **UI:** Jetpack Compose `@Composable` functions; no XML layouts
- **Navigation:** Sealed class routes (e.g., `Screen.Home`, `Screen.TrustWizard`)

---

## Testing

### Android Unit Tests
Located in `android/app/src/test/`:
- `AuthViewModelTest.kt` — login, register, error handling (4 tests)
- `TrustViewModelTest.kt` — trust wizard step navigation
- `AiViewModelTest.kt` — AI assistant message flow
- `UseCaseValidationTest.kt` — business logic validation

Run with: `./gradlew test` (pure JVM, no emulator required)

### Playwright UX Tests
Located in `agents/ux-test-runner.js`:
- 11 UX tests covering button clicks and screen navigation
- Outputs `ux-test-report.json` with pass/fail metrics
- Targets: Home, Documents, Settings screens

Run with: `node agents/ux-test-runner.js` from the repo root.

### Backend Tests
No automated tests exist yet. Manual testing via HTTP client (e.g., Postman) or curl. Adding Jest is recommended.

### TypeScript Compilation Checks
```bash
cd frontend && npx tsc --noEmit   # Check for type errors (frontend)
cd backend && npm run build        # Compile and check for errors (backend)
```

> **Known issue:** Frontend `tsconfig.json` shows a warning about `expo/tsconfig.base.json` path resolution. This is a non-critical Expo quirk; the app compiles and runs correctly.

---

## Security Considerations

- **Helmet:** Applied to all backend routes for security headers
- **CORS:** Enabled for cross-origin requests
- **Passwords:** Hashed with `bcryptjs`, salt rounds = 10
- **JWT:** 7-day expiry; validate on all protected routes
- **Known gaps (from audit):**
  - No rate limiting on `/api/auth` endpoints (add before production)
  - In-memory database lacks persistence and access controls
  - OpenAI API key should be validated server-side before use

---

## Known Issues & Tech Debt

| Area | Issue | Priority |
|---|---|---|
| Backend | Uses mock in-memory arrays instead of real MongoDB | High |
| Backend | No rate limiting on auth endpoints | High |
| Backend | No structured logging system | Medium |
| Frontend | Navigation type casting uses `as any` in some places | Low |
| Frontend | `tsconfig.json` Expo base path warning | Low (non-critical) |
| CI/CD | No automated CI/CD pipeline | Medium |
| Backend | No unit tests | Medium |

---

## Git Workflow

This project uses a **feature branch workflow**:

1. Create a branch from `master`: `git checkout -b feature/your-feature-name`
2. Make changes, commit with clear messages
3. Push branch and create a pull request
4. Merge to `master` after review

See `GITHUB-SETUP.md` for detailed instructions.

---

## Important Files Reference

| File | Description |
|---|---|
| `frontend/App.tsx` | Navigation stack and screen registration |
| `frontend/src/screens/*.tsx` | All screen components |
| `backend/src/index.ts` | Express server setup and middleware chain |
| `backend/src/routes/` | API route handlers |
| `android/app/build.gradle.kts` | Android build config and dependencies |
| `android/app/src/main/java/com/livingtrust/app/` | All Kotlin source code |
| `android/README.md` | Detailed Android architecture documentation |
| `README.md` | Project setup and overview |
| `AUDIT.md` | Code quality audit report |
