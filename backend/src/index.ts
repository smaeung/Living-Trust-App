import express, { Application, Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import dotenv from 'dotenv';

// Routes
import authRoutes from './src/routes/authRoutes';
import trustRoutes from './src/routes/trustRoutes';
import documentRoutes from './src/routes/documentRoutes';
import aiRoutes from './src/routes/aiRoutes';
import userRoutes from './src/routes/userRoutes';

dotenv.config();

const app: Application = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(helmet());
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/trusts', trustRoutes);
app.use('/api/documents', documentRoutes);
app.use('/api/ai', aiRoutes);
app.use('/api/users', userRoutes);

// Health Check
app.get('/health', (req: Request, res: Response) => {
  res.json({ 
    status: 'ok', 
    message: 'Living Trust API is running',
    timestamp: new Date().toISOString() 
  });
});

// Error Handling
app.use((err: Error, req: Request, res: Response, next: NextFunction) => {
  console.error(err.stack);
  res.status(500).json({ 
    error: 'Something went wrong!',
    message: err.message 
  });
});

// 404 Handler
app.use((req: Request, res: Response) => {
  res.status(404).json({ error: 'Route not found' });
});

app.listen(PORT, () => {
  console.log(`
  ╔═══════════════════════════════════════════╗
  ║                                           ║
  ║   🛡️ Living Trust API Server             ║
  ║   🚀 Running on http://localhost:${PORT}    ║
  ║                                           ║
  ╚═══════════════════════════════════════════╝
  `);
});

export default app;
