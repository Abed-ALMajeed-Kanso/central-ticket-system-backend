Centralized Ticket System: Developed a centralized ticket system designed to integrate various ticket systems used by one company or individual for different purposes across large teams, while ensuring that all tickets are stored in a single place. The platform is built using Spring Boot, MySQL, tailwind, and Next.js, and supports the following features:


Key Features & Functionality:

- Real-time ticket handling: Implemented using WebSockets and integrated with Slack via Webhooks

Backend Deployment: Hosted on AWS EC2 (Linux Red Hat) and connected to AWS RDS, with CLI-based SSH management. Ticket attachments are securely stored in AWS S3, and Cloudflare is used for secured backend connections.

-	Frontend Deployment: Deployed on Vercel, providing a fast and responsive user interface.

-	Authentication & Security:

  - Spring Security with cookie-based authentication 
  
  -	Generation of access and refresh tokens, with check-auth and refresh-auth mechanisms
  
  -	Rate limiting implemented with Bucket4j
  
  -	Role-Based Access Control (RBAC)
  
-	Frontend Enhancements:

  -	TenStack for managing the users table
  
  -	Formik for form validation
  
  -	Pagination, sorting, and filtering supported on both frontend and backend

-	Task Automation & Maintenance:

  -	Quartz Scheduler marks tickets unseen for more than a week as urgent
  
  -	Auditing applied to users
  
  -	Orphan delete rules enforced across entity hierarchies: users → tickets → messages → attachments
  
  -	Transactional methods ensure database integrity during complex operations

-	Project Setup: Provided in SETUP.MD.
