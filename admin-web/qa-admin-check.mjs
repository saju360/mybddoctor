import { chromium } from 'playwright';
import fs from 'node:fs/promises';
import path from 'node:path';

const base = 'http://127.0.0.1:5173';
const phone = process.env.ADMIN_PHONE || '01700000001';
const password = process.env.ADMIN_PASSWORD || 'Admin@12345';
const routes = [
  '/', '/approvals', '/users', '/donors', '/blood-requests', '/blood-banks', '/blood-orgs',
  '/hospitals', '/doctors', '/clinics', '/ambulances', '/pharmacies', '/diagnostics',
  '/emergency', '/appointments', '/telemedicine', '/reminders', '/health-records',
  '/slides', '/walkthrough', '/notifications', '/ads-settings', '/settings'
];

const outDir = path.resolve('qa-screenshots-admin');
await fs.mkdir(outDir, { recursive: true });

async function runViewport(name, viewport) {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport });
  const page = await context.newPage();

  const events = [];
  let currentRoute = 'boot';

  page.on('console', msg => {
    if (msg.type() === 'error') {
      events.push({ kind: 'console', route: currentRoute, text: msg.text() });
    }
  });
  page.on('pageerror', err => {
    events.push({ kind: 'pageerror', route: currentRoute, text: String(err) });
  });
  page.on('requestfailed', req => {
    const f = req.failure();
    const url = req.url();
    if (url.includes('/api/') || url.includes('googleads') || url.includes('doubleclick') || url.includes('facebook.com')) {
      events.push({ kind: 'requestfailed', route: currentRoute, text: `${url} :: ${f?.errorText || 'failed'}` });
    }
  });

  const report = [];

  // login
  currentRoute = '/login';
  await page.goto(`${base}/login`, { waitUntil: 'domcontentloaded', timeout: 30000 });
  await page.fill('#phone', phone);
  await page.fill('#password', password);
  await page.click('button[type="submit"]');
  await page.waitForTimeout(1500);

  for (const route of routes) {
    currentRoute = route;
    const eventStart = events.length;
    let status = 'PASS';
    let detail = 'ok';

    try {
      await page.goto(`${base}${route}`, { waitUntil: 'domcontentloaded', timeout: 45000 });
      await page.waitForTimeout(1000);
      const title = (await page.locator('h1').first().textContent().catch(() => ''))?.trim() || '(no h1)';
      detail = `h1=${title}`;
      if (page.url().includes('/login')) {
        status = 'FAIL';
        detail = 'redirected_to_login';
      }
    } catch (e) {
      status = 'FAIL';
      detail = `nav_error: ${String(e.message || e)}`;
    }

    const shot = `${name}_${route === '/' ? 'dashboard' : route.replaceAll('/', '_')}.png`;
    try {
      await page.screenshot({ path: path.join(outDir, shot), fullPage: true });
    } catch {}

    const routeEvents = events.slice(eventStart);
    if (routeEvents.length > 0 && status === 'PASS') {
      status = 'WARN';
    }

    report.push({ route, status, detail, issues: routeEvents.map(e => `${e.kind}: ${e.text}`).slice(0, 8), screenshot: shot });
  }

  await browser.close();
  return { viewport: name, report };
}

const results = [];
results.push(await runViewport('desktop', { width: 1440, height: 900 }));
results.push(await runViewport('mobile', { width: 390, height: 844 }));

const outFile = path.resolve('qa-admin-report.json');
await fs.writeFile(outFile, JSON.stringify(results, null, 2));

console.log(`REPORT_FILE=${outFile}`);
console.log(`SCREENSHOT_DIR=${outDir}`);
