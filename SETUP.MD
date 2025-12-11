Follow the steps below to run the backend locally using IntelliJ IDEA and MySQL.

Prerequisites

  - Ensure the following are installed on your machine:

  - JDK 21 or later

  - IntelliJ IDEA (Community or Ultimate)

  - MySQL Server

  - Maven (optional, IntelliJ bundles it automatically)

  - Database Setup (MySQL): Open your MySQL client (Workbench, CLI, TablePlus, etc.)

Create the database:

CREATE DATABASE ticket_system;

Make sure MySQL is running on port 3306.

Project Installation:

Clone the repository or download and extract the backend project.

Open IntelliJ IDEA → Open → select your project folder.

IntelliJ will automatically import Maven dependencies.

Environment Variables / Properties

For local development:

Inside the project, locate:

src/main/resources/example.application.properties


Make a copy and rename it to:

application.properties


Fill in the required variables:

spring.application.name=TicketSystem

spring.datasource.url=jdbc:mysql://localhost:3306/ticket_system
spring.datasource.username=root
spring.datasource.password=your_mysql_password

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update

app.jwt-secret=your_jwt_secret
app.jwt-expiration=8640000
app.refresh-short-expiration=604800
app.refresh-long-expiration=2592000

logging.level.org.springframework.security=DEBUG

slack.webhook.url=

AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=
AWS_S3_BUCKET=

spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=200MB

Notes:

jwt-secret can be any long random string.

If you are not using AWS or Slack locally, leave those empty.

Ensure your MySQL password matches your local installation.

Running the Backend

Inside IntelliJ:

Open the class containing
public static void main(String[] args)
(usually TicketSystemApplication.java)

Click Run 

Spring Boot will start on the default port 8080.

Alternatively, from terminal inside the project:

mvn spring-boot:run

Verifying Local Setup

Open browser or Postman:

http://localhost:8080/api/auth/login

use the emial, password feilds in the seeder config/seeders/roleseeder file

ex:

email: admin@example.com
password: Password123@
rememberMe: true
