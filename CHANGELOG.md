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

- Multiple files for upload audit
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
