# Changelog
## 1.0.0
- Rest API
- 4 types of audit
- User, contract and project management

## 1.0.2
- Endpoint healthcheck , new key in common properties : management.endpoint.health.show-details, in order to show details or not
- Rest : New key "admin.mail.whenblocked" (Allow send an email to admin when user is blocked)
- JSON export of audits and pages
- Send email at the end of the audit (site, scenario, group of pages)

## 1.0.3
- Multiple files for upload audit
- Fix contract modification : could change owner to get multiple contract on 1 user
- Admin and SuperAdmin can unlock/lock users
- Translation of the mail sended when the user is blocked
- New module for launching audits in docker container : tanaguru-cli-docker
- 3 news configurations keys in audit-runner.properties, "auditrunner.audit-docker.enabled" to enable the launch of the audits in docker containers and "auditrunner.audit-docker.container" to specify the name of the docker image to use, and "auditrunner.audit-docker.network.mode" to specify the network host config of the container
