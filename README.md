Centralized Ticket System: Developed a centralized ticket system designed to integrate various ticket systems used by one company or individual for different purposes across large teams, while ensuring that all tickets are stored in a single place. The platform is built using Spring Boot, MySQL, tailwind, and Next.js, and supports the following features:

Key Features & Functionality:

- Near-real-time ticket updates: Near-real-time behavior was implemented using WebSockets (STOMP over WebSocket with SockJS callback), with Slack integration handled via Webhooks for notifications. In real-world ticketing systems, near-real-time behavior is typically achieved using short polling or event-based refresh mechanisms, which are simpler, and more reliable. In this project, WebSockets were intentionally used for learning purposes to explore real-time communication patterns. They were applied as a UI enhancement layer, not as a core dependency, in the following areas:
  
  - Broadcasting ticket creation events to update active dashboards
  - Broadcasting new ticket messages to viewers of the same ticket
  - Updating ticket viewed/unviewed status so tickets reappear correctly in the list after new activity

- The system remains fully functional without WebSockets, ensuring correctness is always maintained through REST APIs and database persistence.

- Backend Deployment: Hosted on AWS EC2 (Linux Red Hat) and connected to AWS RDS, with CLI-based SSH management. Ticket attachments are securely stored in AWS S3, and Cloudflare tunnel is used for secured backend connections.

- Frontend Deployment: Deployed on Vercel
  
- Authentication & Security:
  
  - Spring Security with cookie-based authentication
  
  - Generation of access and refresh tokens, with check-auth and refresh-auth mechanisms
  
  - Rate limiting implemented with Bucket4j
  
  - Role-Based Access Control (RBAC)

- Frontend Enhancements:
  
  - TenStack for managing the users table
  
  - Formik for form validation
  
  - Pagination, sorting, and filtering supported on both frontend and backend

- Task Automation & Maintenance:
  
  - Quartz Scheduler marks tickets unseen for more than a week as urgent
  
  - Auditing applied to users
  
  - Orphan delete rules enforced across entity hierarchies: users → tickets → messages → attachments
  
  - Transactional methods ensure database integrity during complex operations

- Project Setup: Provided in SETUP.MD
