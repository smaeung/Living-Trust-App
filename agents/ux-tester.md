# AI UX Tester Agent

## Purpose
Automatically verify all UX interactions in the Living Trust App work correctly.

## Test Coverage

### ✅ Home Screen
- [ ] App loads without errors
- [ ] "Create Trust" button navigates to TrustWizard
- [ ] "AI Assistant" button navigates to AI Assistant
- [ ] "Documents" button navigates to Documents
- [ ] "Settings" button navigates to Settings

### ✅ Trust Wizard Screen
- [ ] Step 1: Trust Name input works
- [ ] Step 1: Trust Type selection works
- [ ] Next button validates required fields
- [ ] Back button navigates to previous step
- [ ] Step indicator shows correct progress
- [ ] Final "Create Now" shows confirmation popup
- [ ] Success popup appears after creation

### ✅ Documents Screen
- [ ] Documents list displays
- [ ] "New Document" navigates to TrustWizard
- [ ] "Upload" shows upload options
- [ ] "Link" opens link modal
- [ ] View button shows document details
- [ ] Download button triggers download
- [ ] Delete button removes document
- [ ] Stats cards show counts

### ✅ Settings Screen
- [ ] Edit Profile opens popup
- [ ] Change Password opens popup
- [ ] Push Notifications toggle works
- [ ] Email Updates toggle works
- [ ] Biometric Login toggle works
- [ ] Dark Mode toggle works
- [ ] Language selector opens popup
- [ ] Clear Cache opens popup
- [ ] Terms of Service shows popup
- [ ] Privacy Policy shows popup
- [ ] Disclaimer shows popup
- [ ] Rate App shows popup
- [ ] Send Feedback shows popup
- [ ] Logout button works
- [ ] Delete Account button works

### ✅ AI Assistant Screen
- [ ] Chat interface loads
- [ ] Quick question buttons work
- [ ] Message input works
- [ ] Send button sends message

## Test Commands

### Run Full UX Test
```bash
cd frontend
npx expo start --web
# Then run playwright test
```

### Manual UX Checklist
Print and verify each item above.

## Bug Reporting
If any test fails, document:
1. Screen name
2. Expected behavior
3. Actual behavior
4. Steps to reproduce
