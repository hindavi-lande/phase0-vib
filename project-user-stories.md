ViB (Lead-Gen Platform) — Full Phase Plan

Phase 0 — Bare minimum, just to prove it generates
- 2 entities: Client(companyName, contactName, contactEmail, industry, status), Campaign(clientId FK, name, targetCriteria, status)
- No RBAC, no capabilities, no search — just create/get/list/update/delete with one FK relation
- Goal: clean generate + passing tests, nothing else

Phase 1 — Core lead-gen service (real foundation)
- Entities:
  - Client (+ website, tier: SMB/MID_MARKET/ENTERPRISE)
  - Campaign (+ jobTitles, industries, companySizeRange, deliveryType: EMAIL_LIST/MEETING/CONTENT_DOWNLOAD/WEBINAR_ATTENDEE, leadsRequested)
  - Member (community person: firstName, lastName, email, jobTitle, company, industry, companySize, optInStatus)
  - Lead (campaignId FK, memberId FK, deliveryType, status)
- Real RBAC: ROLE_MEMBER / ROLE_CLIENT / ROLE_ACCOUNT_MANAGER / ROLE_ADMIN
- FK relations: Campaign → Client, Lead → Campaign, Lead → Member
- Full-text search on Member (name/email/company), filters on Campaign/Lead (status, deliveryType, clientId)

Phase 2 — Workflow & complexity on top of Phase 1
- status-transition on Lead: NEW → MATCHED → DELIVERED → ACCEPTED / REJECTED
- status-transition on Campaign: DRAFT → ACTIVE → PAUSED / COMPLETED
- assign-round-robin — auto-assign an account manager to new campaigns
- This is where it stops being generic CRUD and starts having real state machines

Phase 3 — Companion microservice (separate service/DB)
- Option A: Meetings & Scheduling service — Meeting, Attendee, referencing Lead/Member/Client by id only (no FK across services)
- Option B: Content & Webinar service — ContentAsset, Download, WebinarRegistration, referencing Member/Client by id only

Phase 4 — Reporting & cross-cutting capabilities
- aggregate-summary — leads delivered per campaign per month, conversion rate (delivered vs. accepted)
- export — lead delivery report CSV/PDF per client/campaign
- webhook-trigger — notify client's CRM/webhook when a new lead is delivered or a campaign hits its quota

---

User Stories

Phase 1 — Core lead-gen service
1. As a system, I want Client with company profile fields (companyName, contactName, contactEmail, website, industry, tier, status), so that buyers of leads are tracked as accounts.
2. As a user, I want to create, view, list, update, and delete Campaign records (targetCriteria, deliveryType, leadsRequested, status) linked to a Client, so that each lead request is tracked against a client.
3. As a system, I want Member records for community participants (name, email, jobTitle, company, industry, companySize, optInStatus), so that the platform has a pool of real people to match against campaigns.
4. As a user, I want to create, view, list, update, and delete Lead records (deliveryType, status) linking a Campaign to a Member, so that matched/delivered leads are tracked per campaign.
5. As a system, I want role-based access control with ROLE_MEMBER, ROLE_CLIENT, ROLE_ACCOUNT_MANAGER, and ROLE_ADMIN, so that endpoints are restricted based on who's calling (member self-service vs. client dashboard vs. internal ops).
6. As a user, I want full-text search on Member by name/email/company, so that account managers can locate specific community members.
7. As a user, I want to filter Campaign by status, deliveryType, and clientId, so that I can narrow down which campaigns are active for a given client.
8. As a user, I want to filter Lead by status, deliveryType, and clientId, so that I can see delivery progress for a campaign.

Phase 2 — Workflow & complexity
9. As a system, I want to transition a Lead's status from NEW through MATCHED → DELIVERED → ACCEPTED/REJECTED with validation, so that only legal state changes are persisted and clients can't accept a lead that was never delivered.
10. As a system, I want to transition a Campaign's status through DRAFT → ACTIVE → PAUSED/COMPLETED with validation, so that leads are only generated against active campaigns.
11. As a system, I want to auto-assign an account manager to every new campaign using round-robin, so that campaign ownership is distributed evenly without manual assignment.

Phase 3 — Companion microservice

Option A: Meetings & Scheduling
12. As a system, I want a standalone Meetings service with its own database, so that meeting booking/scheduling logic is decoupled from the core lead-gen service.
13. As a user, I want to create, view, update, and delete Meeting records referencing a Lead/Member/Client by id, so that booked-meeting-type leads have their own scheduling data without a cross-service FK dependency.
14. As a user, I want to create, view, update, and delete Attendee records on a Meeting, so that meeting participants (member + client rep) are tracked independently of the core service's schema.

Option B: Content & Webinar
15. As a system, I want a standalone Content & Webinar service with its own database, so that gated-content and webinar logic is decoupled from the core lead-gen service.
16. As a user, I want to create, view, update, and delete ContentAsset records (whitepapers, case studies, webinars) referencing a Client by id, so that client-owned content is catalogued independently.
17. As a user, I want to create, view, update, and delete Download and WebinarRegistration records referencing a Member/ContentAsset by id, so that content-download and webinar-attendance leads are captured without a cross-service FK dependency.

Phase 4 — Reporting & cross-cutting
18. As a user, I want to view a monthly aggregate summary of leads delivered per campaign (volume + delivered-vs-accepted conversion rate), so that I can report performance to the client.
19. As a user, I want to export a campaign's lead delivery report as CSV, so that clients can import lead data into their own CRM.
20. As a user, I want to export a campaign's lead delivery report as PDF, so that account managers have a shareable formatted record for client reviews.
21. As a system, I want to trigger a webhook when a new lead is delivered or a campaign hits its requested lead quota, so that a client's CRM/webhook endpoint gets near real-time notification.