import { Router, Request, Response } from 'express';

const router = Router();

// Mock trust data
const trusts: any[] = [];

// Create a new trust
router.post('/', (req: Request, res: Response) => {
  try {
    const trust = {
      id: Date.now().toString(),
      ...req.body,
      status: 'draft',
      createdAt: new Date(),
      updatedAt: new Date()
    };
    
    trusts.push(trust);
    
    res.status(201).json({
      message: 'Trust created successfully',
      trust
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to create trust' });
  }
});

// Get all trusts for user
router.get('/', (req: Request, res: Response) => {
  res.json({ trusts });
});

// Get single trust
router.get('/:id', (req: Request, res: Response) => {
  const trust = trusts.find(t => t.id === req.params.id);
  
  if (!trust) {
    return res.status(404).json({ error: 'Trust not found' });
  }
  
  res.json({ trust });
});

// Update trust
router.put('/:id', (req: Request, res: Response) => {
  const index = trusts.findIndex(t => t.id === req.params.id);
  
  if (index === -1) {
    return res.status(404).json({ error: 'Trust not found' });
  }
  
  trusts[index] = {
    ...trusts[index],
    ...req.body,
    updatedAt: new Date()
  };
  
  res.json({
    message: 'Trust updated successfully',
    trust: trusts[index]
  });
});

// Delete trust
router.delete('/:id', (req: Request, res: Response) => {
  const index = trusts.findIndex(t => t.id === req.params.id);
  
  if (index === -1) {
    return res.status(404).json({ error: 'Trust not found' });
  }
  
  trusts.splice(index, 1);
  
  res.json({ message: 'Trust deleted successfully' });
});

export default router;
