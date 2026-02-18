const { chromium } = require('playwright');

const UX_TESTS = [
  // Home Screen
  { name: 'Home - Load', test: async (page) => { await page.goto('http://localhost:8081'); } },
  { name: 'Home - Create Trust button', test: async (page) => { await page.click('text=Create Trust'); } },
  
  // Documents Screen
  { name: 'Documents - Load', test: async (page) => { await page.goto('http://localhost:8081'); await page.waitForTimeout(500); } },
  { name: 'Documents - Upload button', test: async (page) => { await page.click('text=Upload'); } },
  { name: 'Documents - Link button', test: async (page) => { await page.click('text=Link'); } },
  
  // Settings Screen
  { name: 'Settings - Load', test: async (page) => { await page.goto('http://localhost:8081/Settings'); } },
  { name: 'Settings - Edit Profile', test: async (page) => { await page.click('text=Edit Profile'); } },
  { name: 'Settings - Change Password', test: async (page) => { await page.click('text=Change Password'); } },
  { name: 'Settings - Notifications toggle', test: async (page) => { await page.click('text=Push Notifications'); } },
  { name: 'Settings - Dark Mode toggle', test: async (page) => { await page.click('text=Dark Mode'); } },
  { name: 'Settings - Language', test: async (page) => { await page.click('text=Language'); } },
  { name: 'Settings - Clear Cache', test: async (page) => { await page.click('text=Clear Cache'); } },
  { name: 'Settings - Terms', test: async (page) => { await page.click('text=Terms of Service'); } },
  { name: 'Settings - Privacy', test: async (page) => { await page.click('text=Privacy Policy'); } },
  { name: 'Settings - Disclaimer', test: async (page) => { await page.click('text=Disclaimer'); } },
  { name: 'Settings - Logout', test: async (page) => { await page.click('text=Logout'); } },
];

async function runUXTests() {
  console.log('🚀 Starting UX Tests...\n');
  
  let browser;
  let passed = 0;
  let failed = 0;
  const results = [];
  
  try {
    // Start local server
    const { spawn } = require('child_process');
    console.log('📡 Starting web server...');
    
    browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({ viewport: { width: 375, height: 667 } });
    const page = await context.newPage();
    
    // Capture console errors
    const errors = [];
    page.on('console', msg => {
      if (msg.type() === 'error') errors.push(msg.text());
    });
    
    for (const test of UX_TESTS) {
      try {
        console.log(`Testing: ${test.name}...`);
        await test.test(page);
        await page.waitForTimeout(500);
        console.log(`  ✅ PASS\n`);
        passed++;
        results.push({ test: test.name, status: 'PASS' });
      } catch (e) {
        console.log(`  ❌ FAIL: ${e.message}\n`);
        failed++;
        results.push({ test: test.name, status: 'FAIL', error: e.message });
      }
    }
    
    // Print summary
    console.log('\n' + '='.repeat(50));
    console.log('📊 UX TEST SUMMARY');
    console.log('='.repeat(50));
    console.log(`✅ Passed: ${passed}`);
    console.log(`❌ Failed: ${failed}`);
    console.log(`📈 Success Rate: ${Math.round((passed/(passed+failed))*100)}%`);
    console.log('='.repeat(50));
    
    if (errors.length > 0) {
      console.log('\n⚠️ Console Errors:');
      errors.forEach(e => console.log(`  - ${e}`));
    }
    
    // Save report
    const report = {
      timestamp: new Date().toISOString(),
      passed,
      failed,
      successRate: Math.round((passed/(passed+failed))*100),
      results,
      errors
    };
    
    const fs = require('fs');
    fs.writeFileSync('ux-test-report.json', JSON.stringify(report, null, 2));
    console.log('\n📄 Report saved to ux-test-report.json');
    
  } catch (e) {
    console.error('❌ Test runner error:', e.message);
  } finally {
    if (browser) await browser.close();
  }
}

runUXTests();
