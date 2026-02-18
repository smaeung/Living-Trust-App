# AI UX Tester Agent

## Purpose
Test and verify the Living Trust App UX/UI works correctly on web (mobile & desktop sizes).

## Quick Test Commands

### Start Web Server
```powershell
# Serve the built web app
cd frontend/dist
npx serve
# OR
npx http-server -p 8080
```

### Test URLs
| Device | URL | Viewport |
|--------|-----|----------|
| Mobile | http://localhost:8080 | 375×667 |
| Tablet | http://localhost:8080 | 768×1024 |
| Desktop | http://localhost:8080 | 1280×800 |

## Automated UX Testing

### Check 1: Page Loads
- [ ] Home screen renders
- [ ] Navigation works
- [ ] All 6 screens accessible

### Check 2: Responsive Design
- [ ] Mobile view (375px)
- [ ] Tablet view (768px)
- [ ] Desktop view (1280px)

### Check 3: UI Elements
- [ ] Buttons clickable
- [ ] Forms functional
- [ ] Navigation transitions

## Browser Testing Script

```javascript
// Puppeteer test example
const puppeteer = require('puppeteer');

async function testUX() {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();
  
  // Test mobile
  await page.setViewport({ width: 375, height: 667 });
  await page.goto('http://localhost:8080');
  
  // Check elements
  const title = await page.title();
  console.log('Page title:', title);
  
  await browser.close();
}
```

## Manual Verification Checklist

### Home Screen
- [ ] Welcome message displays
- [ ] Quick action buttons visible
- [ ] Navigation menu works

### Trust Wizard
- [ ] Step indicators visible
- [ ] Form inputs work
- [ ] Next/Back navigation

### AI Assistant
- [ ] Chat interface loads
- [ ] Quick questions appear
- [ ] Message input works

### Documents
- [ ] Document list displays
- [ ] Stats cards visible
- [ ] Action buttons work

### Settings
- [ ] Toggle switches work
- [ ] Profile section visible
- [ ] Logout button functional
