import { Router, Request, Response } from 'express';

const router = Router();

// Get user profile
router.get('/profile', (req: Request, res: Response) => {
  // In production, get from database
  res.json({
    user: {
      id: '1',
      name: 'Demo User',
      email: 'demo@example.com',
      createdAt: new Date()
    }
  });
});

// Update user profile
router.put('/profile', (req: Request, res: Response) => {
  res.json({
    message: 'Profile updated successfully',
    user: req.body
  });
});

export default router;
