# Living Trust App

AI-powered mobile app to help people build, overview, and review Living Trusts with an AI lawyer assistant.

## 🎯 Purpose

The **Living Trust App** helps users:
- ✅ Create a Living Trust with guided wizard
- ✅ Review existing Trust documents with AI analysis
- ✅ Get legal guidance from AI lawyer assistant
- ✅ Store and manage Trust documents securely

## 📱 Platform

- **iOS:** React Native (iOS build ready)
- **Android:** React Native (Android build ready)
- **Backend:** Node.js Microservices API

## 🛠 Tech Stack

### Frontend
- React Native (Expo)
- TypeScript
- React Navigation
- Axios (API calls)

### Backend
- Node.js + Express
- TypeScript
- MongoDB (or PostgreSQL with Supabase)
- JWT Authentication
- OpenAI API (for AI lawyer features)

## 📂 Project Structure

```
Living-Trust-App/
├── frontend/          # React Native app
│   ├── src/
│   │   ├── screens/   # App screens
│   │   ├── components/ # Reusable components
│   │   ├── services/  # API services
│   │   ├── types/     # TypeScript types
│   │   └── utils/    # Utility functions
│   ├── App.tsx       # Main app entry
│   └── package.json
│
├── backend/           # Node.js API
│   ├── src/
│   │   ├── routes/   # API routes
│   │   ├── controllers/ # Business logic
│   │   ├── models/   # Database models
│   │   ├── services/ # Services
│   │   └── middleware/ # Middleware
│   ├── index.ts      # Server entry
│   └── package.json
│
└── README.md         # This file
```

## 🚀 Getting Started

### Prerequisites
- Node.js 18+
- npm or yarn
- Expo CLI
- MongoDB (local or Atlas)

### Frontend Setup
```bash
cd frontend
npm install
npx expo start
```

### Backend Setup
```bash
cd backend
npm install
npm run dev
```

## 🔧 Environment Variables

### Backend (.env)
```
PORT=3001
MONGODB_URI=mongodb://localhost:27017/living-trust
JWT_SECRET=your-secret-key
OPENAI_API_KEY=sk-...
```

## 📄 License

MIT

## 👤 Author

- Sungho Maeung

---

*Built with ❤️ for helping people secure their legacy*
