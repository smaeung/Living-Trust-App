const { chromium } = require('playwright');

async function captureScreenshots() {
  const browser = await chromium.launch({ headless: true });
  
  // Mobile view
  const mobileContext = await browser.newContext({
    viewport: { width: 375, height: 667 }
  });
  const mobilePage = await mobileContext.newPage();
  
  console.log('Loading app...');
  await mobilePage.goto('http://localhost:8081');
  await mobilePage.waitForLoadState('networkidle');
  
  console.log('Capturing Home Screen (Mobile)...');
  await mobilePage.screenshot({ path: 'screenshots/home-mobile.png', fullPage: true });
  
  // Desktop view
  console.log('Capturing Home Screen (Desktop)...');
  await mobilePage.setViewportSize({ width: 1280, height: 800 });
  await mobilePage.screenshot({ path: 'screenshots/home-desktop.png', fullPage: true });
  
  // Try navigate to Trust Wizard
  console.log('Navigating to Trust Wizard...');
  try {
    await mobilePage.goto('http://localhost:8081/#TrustWizard');
    await mobilePage.waitForTimeout(2000);
    await mobilePage.screenshot({ path: 'screenshots/trust-wizard-mobile.png', fullPage: true });
  } catch(e) {
    console.log('Could not navigate to TrustWizard');
  }
  
  await browser.close();
  console.log('All screenshots captured!');
}

captureScreenshots().catch(e => {
  console.error('Error:', e.message);
  process.exit(1);
});
