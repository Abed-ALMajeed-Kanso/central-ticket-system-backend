Follow the steps below to run the backend locally using IntelliJ IDEA and MySQL.

Ensure the following are installed on your machine:

  - JDK 21 or later

  - IntelliJ IDEA (Community or Ultimate), or STS

  - MySQL Server, or Wampserver

  - Maven (optional, IntelliJ bundles it automatically)

Database Setup (MySQL): Open your MySQL client (Workbench, CLI, TablePlus, etc.)

Create the database:

CREATE DATABASE ticket_system;

Make sure MySQL is running on port 3306.

Project Installation:

  - Clone the repository or download and extract the backend project.

  - Open IntelliJ IDEA → Open → select your project folder.

  - Sync Maven dependencies (clcik M on the top right).

Environment Variables / Properties

  - Inside the project, locate: src/main/resources/example.application.properties

  - Make a copy and rename it to: application.properties

  - Fill in the required variables

  - You need to configure some of them from their sources (create your own S3 bucket, IAM user, and Slack app), or to make some changes in the application

Notes:

jwt-secret can be any long random string.

If you are not using AWS or Slack locally, leave those empty.

Ensure your MySQL password matches your local installation.

Running the Backend

Inside IntelliJ:

Open the TicketSystemApplication.java

Click Run 

Spring Boot will start on the default port 8080.

Verifying Local Setup, use Postman or OpenAPI:

http://localhost:8080/api/auth/login

use a emial, password record in the seeder config/seeders/roleseeder file

ex:

  - email: admin@example.com
  - password: Password123@
  - rememberMe: true
