import { Router, Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';

const router = Router();

// Mock user database (replace with real DB)
const users: any[] = [];

router.post('/register', async (req: Request, res: Response) => {
  try {
    const { email, password, name } = req.body;
    
    // Check if user exists
    const existingUser = users.find(u => u.email === email);
    if (existingUser) {
      return res.status(400).json({ error: 'User already exists' });
    }
    
    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);
    
    // Create user
    const user = {
      id: Date.now().toString(),
      email,
      name,
      password: hashedPassword,
      createdAt: new Date()
    };
    
    users.push(user);
    
    // Generate token
    const token = jwt.sign(
      { userId: user.id, email: user.email },
      process.env.JWT_SECRET || 'secret-key',
      { expiresIn: '7d' }
    );
    
    res.status(201).json({
      message: 'User created successfully',
      token,
      user: { id: user.id, email: user.email, name: user.name }
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to register user' });
  }
});

router.post('/login', async (req: Request, res: Response) => {
  try {
    const { email, password } = req.body;
    
    // Find user
    const user = users.find(u => u.email === email);
    if (!user) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }
    
    // Check password
    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }
    
    // Generate token
    const token = jwt.sign(
      { userId: user.id, email: user.email },
      process.env.JWT_SECRET || 'secret-key',
      { expiresIn: '7d' }
    );
    
    res.json({
      message: 'Login successful',
      token,
      user: { id: user.id, email: user.email, name: user.name }
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to login' });
  }
});

router.get('/me', (req: Request, res: Response) => {
  // Get user from token (implement middleware in production)
  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return res.status(401).json({ error: 'No token provided' });
  }
  
  try {
    const token = authHeader.split(' ')[1];
    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'secret-key') as any;
    const user = users.find(u => u.id === decoded.userId);
    
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    res.json({ 
      user: { id: user.id, email: user.email, name: user.name } 
    });
  } catch (error) {
    res.status(401).json({ error: 'Invalid token' });
  }
});

export default router;
