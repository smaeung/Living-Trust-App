import { Router, Request, Response } from 'express';

const router = Router();

// Mock documents
const documents: any[] = [];

// Upload document
router.post('/', (req: Request, res: Response) => {
  try {
    const document = {
      id: Date.now().toString(),
      ...req.body,
      uploadedAt: new Date()
    };
    
    documents.push(document);
    
    res.status(201).json({
      message: 'Document uploaded successfully',
      document
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to upload document' });
  }
});

// Get all documents
router.get('/', (req: Request, res: Response) => {
  res.json({ documents });
});

// Get single document
router.get('/:id', (req: Request, res: Response) => {
  const document = documents.find(d => d.id === req.params.id);
  
  if (!document) {
    return res.status(404).json({ error: 'Document not found' });
  }
  
  res.json({ document });
});

// Delete document
router.delete('/:id', (req: Request, res: Response) => {
  const index = documents.findIndex(d => d.id === req.params.id);
  
  if (index === -1) {
    return res.status(404).json({ error: 'Document not found' });
  }
  
  documents.splice(index, 1);
  
  res.json({ message: 'Document deleted successfully' });
});

export default router;
