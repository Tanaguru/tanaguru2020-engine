# Changelog

## 1.0.0

- Rest API
- 4 types of audit
- User, contract and project management

## 1.0.2

- Endpoint healthcheck , new key in common properties : management.endpoint.health.show-details, in order to show
  details or not
- Rest : New key "admin.mail.whenblocked" (Allow send an email to admin when user is blocked)
- JSON export of audits and pages
- Send email at the end of the audit (site, scenario, group of pages)

## 1.0.3

- Fix contract modification : could change owner to get multiple contract on 1 user
- Admin and SuperAdmin can unlock/lock users
- Translation of the mail sent when the user is blocked
- Endpoint : audit logs filtered by date and/or level
- Pagination for contracts, users, references, projects
- Fix mail at the end of audit (was sending to all contract users)
- Endpoint stop audit for async audit (other profiles will throw a bad request exception)
- Add configuration key to enable or not robots.txt respect (auditrunner.crawler.follow-robots)
- Optimize TestResult getAllTestResultByReference endpoint
- Add a parameter on createUser endpoint to create a contract
- Add statistics page for super-admin and config key (statistics.fixedDelay)
- Upgrade logging
- Fix audit and user deletion

## 1.0.4
- Fix modify project
- Wcag updated with the latest version
- Engine updated to work with the latest version of the rgaa
- Correction of discrepancies between engine and web extension
- Endpoint : web extension version used
- Fix site audit : duplicate audited web page
- Update management of contrast tests
- Fix endpoint : global test result for all pages (synthesis page)
- Fix endpoint : source code value added to the result test
- Demo project with specific domain : only one site audit allowed
- Users with Free account type : expired after 1 month, audits pages (and results) deleted
- Fix script timeout on some pages
- Add API key for wordpress plugin
- Fix scheduled audits
- Csv export of audit synthesis
- Scenario audit : store command to keep only certain pages

## 1.0.5
- update tests to 5.1.0

## 1.0.6
- New feature : SSO
- Update project_user authorities : add ressources
- update tests to 5.1.1

## 1.0.7
- Fixes:
  - basic authentication on all type of audit
  - remove project with apiKey
- add getAuditsByAuditScheduler && getLastAuditByScheduledByAndAuditStatus audit endpoints

## 1.0.8
- add logout endpoint

## 2.0.0
- New features:
  - choose default reference
  - subscribe/unsubscribe option for project audit completion emails
- Update:
  - default language (en/fr) can now be set via the new environment variable "message.lang"

## 2.1.0
- Temporary feature (partially implemented): purge database (delete all orphaned audits)
- Update:
  - removal of audits
  - statistics services
  - audit completion email
  - add many indexes to the database

## 2.2.0
- New feature: user token
- Fix : footer link of audit end mail

## 2.2.1
- Update : new endpoint deleteUserToken
- Fix : UserToken constructor

## 2.3.0
- Remove temporary purge feature (see 2.1.0)
- Update :
  - new database indexes for TestHierarchyResult
  - RGAA test file
- New :
  - HikariCP properties (common)
  - thread pools properties (rest)