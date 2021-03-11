# Changelog
## 1.0.0
- Rest API
- 4 types of audit
- User, contract and project management

## 1.0.2
- Rest : New key "admin.mail.whenblocked" (Allow send an email to admin when user is blocked)
- At launch we link the repositories present with the version of the webext engine used
- Endpoint healthcheck , new key in common properties : management.endpoint.health.show-details, in order to show details or not
- JSON export of audits and pages
- Send email at the end of the audit (site, scenario, group of pages)

## 1.0.3
- Multiple files for upload audit
- Fix contract modification : could change owner to get multiple contract on 1 user
- Admin and SuperAdmin can unlock/lock users
- Translation of the mail sended when the user is blocked
