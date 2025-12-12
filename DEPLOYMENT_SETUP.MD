# Deploying Central Ticket System Backend on AWS EC2 (Red Hat) — Java 21 + Maven + RDS MySQL

This README provides a clean, step‑by‑step guide for running the *central-ticket-system-backend* on an AWS **EC2** (Red Hat) instance using **Java 21**, **Maven**, and an **AWS RDS MySQL** database. The original commands and syntax have been preserved exactly; only organization, headings, and explanatory text were improved for clarity.

---

## Prerequisites

* An AWS account with permissions to create EC2 instances and RDS databases.
* A downloaded EC2 key pair (`selected_name.pem`) saved in the directory where you will run SSH from.
* At least **20 GiB** root volume recommended for the instance (to run both the Java application and Cloudflare tunnel reliably).
* The repository: `https://github.com/Abed-ALMajeed-Kanso/central-ticket-system-backend`

> During RDS setup, create the initial database **`central_ticket_system_database`** (recommended). If you don't create it during setup, you'll need to install the MySQL client on the EC2 instance to create it manually.

---

## 1. Connect to your EC2 instance

Run from the directory containing your key pair:

```
ssh -i "selected_name.pem" ec2-user@YOUR_EC2_PUBLIC_IP
```

Make sure you created the EC2 instance and downloaded the Key pair assigned at launch.

---

## 2. Update system packages

```
sudo yum update -y
```

---

## 3. Install Java 21 (Amazon Corretto)

Download and install Corretto 21:

```
sudo curl -Lo /tmp/corretto-21.rpm https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.rpm
sudo dnf install /tmp/corretto-21.rpm -y
```

Verify:

```
java -version
javac -version
```

---

## 4. Install Maven

```
cd /tmp
wget https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
sudo tar xf apache-maven-3.9.11-bin.tar.gz -C /opt/
```

Add Java & Maven to `PATH`:

```
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
export PATH=$JAVA_HOME/bin:$PATH
```

Make it permanent:

```
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

Verify:

```
java -version
mvn -version
```

---

## 5. Install Git

```
sudo dnf install git -y
```

---

## 6. Clone the backend repository

```
git clone https://github.com/Abed-ALMajeed-Kanso/central-ticket-system-backend.git
```

---

## 7. Add production properties file and configuration changes

Navigate to the project and create the production properties file:

```
cd ~/central-ticket-system-backend
nano application-prod.properties
```

Paste your production configuration (use the settings from `example.application.properties`).

Add these two variables to the properties file:

```
cors.allowedOrigin_1 = front_end_url (Vercel used, other provider can be used as well)
cors.allowedOrigin_2 = cloudflare_url (obtained after running cloudflare tunnel)
```

Make sure to:

* Add the new variables to the `setAllowedOrigins` in the `CorsConfigurationSource` bean inside `SpringSecurityConfig`.
* Add a new constructor without the variables injected from `application.properties` if necessary.

Edit the Spring Security config:

```
cd ~/central-ticket-system-backend/src/main/java/com/example/ticket_system/auth/security/
nano SpringSecurityConfig.java
```

Modify cookie attributes in the AuthController to work with cross‑domain HTTPS deployments:

```
cd ~/central-ticket-system-backend/src/main/java/com/example/ticket_system/auth/security/
nano AuthController.java
```

Change the sending cookie attributes to:

```
                .secure(true)
                .sameSite("None")
```

Save files with `CTRL + O`, `ENTER`, then `CTRL + X`.

---

## 8. Build the application

```
cd ~/central-ticket-system-backend
mvn clean package -DskipTests
```

> Note: normally `mvn clean package` runs tests; `-DskipTests` is used here to ignore them.

---

## 9. Run the application

Start the app with the production properties file:

```
java -jar target/*.jar --spring.config.location=file:/home/ec2-user/central-ticket-system-backend/application-prod.properties
```

If the database seeding runs successfully, press `Ctrl + C` to stop and continue to the background steps.

To run the app in the background:

```
nohup java -jar target/*.jar --spring.config.location=file:/home/ec2-user/central-ticket-system-backend/application-prod.properties > backend.log 2>&1 &
```

---

## 10. Install Cloudflare and deploy with Cloudflare Tunnel (no authenticated domain)

Create the Cloudflared repo and install:

```
sudo tee /etc/yum.repos.d/cloudflared.repo > /dev/null << 'EOF'
[cloudflared]
name=cloudflared
baseurl=https://pkg.cloudflare.com/cloudflared/rpm
enabled=1
gpgcheck=1
gpgkey=https://pkg.cloudflare.com/cloudflare-main.gpg
EOF

sudo yum install -y cloudflared

sudo yum clean all
sudo yum makecache
```

Verify:

```
cloudflared –version
```

Start a tunnel to your local Spring Boot port:

```
cloudflared tunnel --url http://localhost:8080
```

To run the tunnel in the background so you can capture the URL:

```
nohup cloudflared tunnel --url http://localhost:8080 > cloudflare.log 2>&1 &
```

Check process:

```
ps aux | grep cloudflared
```

Extract the tunnel URL:

```
echo "Your new tunnel URL:"
cat cloudflare.log | grep -o "https://[^ ]*\.trycloudflare\.com" | head -1
```

Example output:

```
https://complete-males-insight-textbooks.trycloudflare.com
```

Use this URL to update `cors.allowedOrigin_2` in `application-prod.properties` and update your front-end environment variables (Vercel example):

* `NEXT_PUBLIC_API_URL` → include `/api` if the app expects it
* `NEXT_PUBLIC_WS_URL` → include `/ws` if the app expects it

> Tip: you can set `cors.allowedOrigin_2` to `https://*.trycloudflare.com` during quick testing so the app accepts any ephemeral trycloudflare domain. This is **not** recommended for production CORS configuration.

The tunnel will often last from ~30 minutes to a few hours when using an unauthenticated setup.

---

## Notes & Troubleshooting

* Do **not** modify security groups if you followed the instructions to connect RDS directly to EC2 during setup. If you changed security groups, ensure the EC2 instance can communicate with RDS on the correct port (default: 3306).
* If you did not create the initial DB during RDS provisioning, install the MySQL client on the EC2 instance and create `central_ticket_system_database` manually.
* If you see permission issues, confirm the EC2 user matches the AMI (e.g., `ec2-user`) or adjust accordingly.
* Keep an eye on `backend.log` and `cloudflare.log` for startup errors and the tunnel URL.

---

## Repository

`https://github.com/Abed-ALMajeed-Kanso/central-ticket-system-backend`

---

If you want this README saved to the repository directly (PR or commit), I can provide an edit-ready file content or a patch you can apply locally.


