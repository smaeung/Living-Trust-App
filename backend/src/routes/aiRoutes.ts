import { Router, Request, Response } from 'express';
import OpenAI from 'openai';

const router = Router();

// Initialize OpenAI (set OPENAI_API_KEY in .env)
const openai = process.env.OPENAI_API_KEY 
  ? new OpenAI({ apiKey: process.env.OPENAI_API_KEY })
  : null;

// AI Chat endpoint
router.post('/chat', async (req: Request, res: Response) => {
  try {
    const { message, context } = req.body;
    
    if (!message) {
      return res.status(400).json({ error: 'Message is required' });
    }
    
    // If no OpenAI key, return mock response
    if (!openai) {
      return res.json({
        response: getMockAIResponse(message),
        sources: []
      });
    }
    
    const completion = await openai.chat.completions.create({
      model: 'gpt-4',
      messages: [
        {
          role: 'system',
          content: `You are an AI Lawyer Assistant specializing in Living Trusts and Estate Planning. 
          
Your role is to:
- Help users understand Living Trusts
- Explain legal concepts in simple terms
- Provide general legal information (NOT legal advice)
- Guide users through creating a Living Trust
- Review and analyze Trust documents

Important disclaimers:
- Always remind users this is NOT legal advice
- Recommend consulting with a qualified attorney for complex situations
- Stay within bounds of general information`
        },
        {
          role: 'user',
          content: message
        }
      ],
      temperature: 0.7,
      max_tokens: 1000
    });
    
    res.json({
      response: completion.choices[0]?.message?.content || 'I apologize, I could not generate a response.',
      sources: []
    });
  } catch (error) {
    console.error('AI Chat Error:', error);
    res.status(500).json({ error: 'Failed to get AI response' });
  }
});

// Document Analysis endpoint
router.post('/analyze', async (req: Request, res: Response) => {
  try {
    const { documentText } = req.body;
    
    if (!documentText) {
      return res.status(400).json({ error: 'Document text is required' });
    }
    
    // If no OpenAI key, return mock analysis
    if (!openai) {
      return res.json(getMockAnalysis());
    }
    
    const completion = await openai.chat.completions.create({
      model: 'gpt-4',
      messages: [
        {
          role: 'system',
          content: `You are an AI document analyzer specializing in Living Trusts. 
          
Analyze the provided Living Trust document and return a JSON response with:
1. score (0-100) - overall quality score
2. issues (array) - problems found with severity and suggestion
3. recommendations (array) - improvements needed
4. summary (string) - brief overall assessment

Return ONLY valid JSON, no other text.`
        },
        {
          role: 'user',
          content: documentText
        }
      ],
      temperature: 0.3
    });
    
    const response = completion.choices[0]?.message?.content;
    const analysis = JSON.parse(response || '{}');
    
    res.json(analysis);
  } catch (error) {
    console.error('AI Analysis Error:', error);
    res.status(500).json({ error: 'Failed to analyze document' });
  }
});

// Mock AI responses (when no OpenAI key)
function getMockAIResponse(question: string): string {
  const q = question.toLowerCase();
  
  if (q.includes('what is living trust')) {
    return `A **Living Trust** is a legal document that:\n\n
- **Holds your assets** during your lifetime
- **Distributes to beneficiaries** after passing
- **Avoids probate** (court process)\n- **Maintains privacy** (not public)\n\nWould you like help creating one?`;
  }
  
  if (q.includes('revocable') || q.includes('irrevocable')) {
    return `**Revocable Trust:**\n- Change anytime\n- You keep control\n- No tax benefits\n\n**Irrevocable Trust:**\n- Hard to change\n- May reduce taxes\n- Asset protection\n\nMost people start with revocable!`;
  }
  
  if (q.includes('cost') || q.includes('price')) {
    return `**Typical Costs:**\n\n| Option | Cost |\n|--------|------|\n| DIY | $99-299 |\n| Attorney | $1,000-3,000 |\n\nOur app can help you get started for less!`;
  }
  
  return `Great question! I'm here to help with your Living Trust needs.\n\n
Try asking:\n- "What is a Living Trust?"\n- "Revocable vs Irrevocable?"\n- "How much does it cost?"`;
}

function getMockAnalysis() {
  return {
    score: 85,
    issues: [
      { severity: 'medium', text: 'Missing successor trustee signature line', suggestion: 'Add signature line for successor trustee' },
      { severity: 'low', text: 'Beneficiary designation could be more specific', suggestion: 'Add more details about distribution' }
    ],
    recommendations: [
      'Add proper notarization statement',
      'Include tax identification number',
      'Consider adding disability provisions'
    ],
    summary: 'Your Living Trust is well-structured with minor areas for improvement.'
  };
}

export default router;
