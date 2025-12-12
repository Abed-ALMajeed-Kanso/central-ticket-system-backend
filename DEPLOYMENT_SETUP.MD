Follow these steps to run the backend on an AWS EC2 (Red Hat Linux) instance using SSH, Java 21, Maven, and an AWS RDS MySQL database.

1. Connect to Your EC2 Instance
ssh -i "selected_name.pem" ec2-user@YOUR_EC2_PUBLIC_IP

Make you sure you create an EC2 instance, download the Key pair assigned at launch,
and save it in the same directory you open the CLI from
also make sure to set the instance size more around 20 GiB, else the insatnce will not handle
both the java application and the cloudflare tunnel running

Create a database instance and connect it to the EC2 instance directly (dont modify security groups)
during the setup create initial database central_ticket_system_database, or you will need to install
MySQL client in your instance to create the database


2. Update System Packages
sudo yum update -y


3. Install Java 21
sudo curl -Lo /tmp/corretto-21.rpm https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.rpm
sudo dnf install /tmp/corretto-21.rpm -y

Verify installation:

java -version
javac -version


4. Install Maven
cd /tmp
wget https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
sudo tar xf apache-maven-3.9.11-bin.tar.gz -C /opt/

Add Java & Maven to PATH
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
export PATH=$JAVA_HOME/bin:$PATH

Make it permanent:

echo 'export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

Verify:

java -version
mvn -version


5. Install Git
sudo dnf install git -y


6. Clone the Backend Repository
git clone https://github.com/Abed-ALMajeed-Kanso/central-ticket-system-backend.git


7. Add Production Properties File

Navigate to the cloned project:

cd ~/central-ticket-system-backend

Create the production properties file:

nano application-prod.properties

Paste your production config what is mentioned in the example.application.properties

add those two variables

cors.allowedOrigin_1 = front_end_url (Vercel used, other provider can be used as well)
cors.allowedOrigin_2 = cloudflare_url (obtained after running cloudflare tunnel)

make sure to add those new variables to the setAllowedOrigins at the CorsConfigurationSource Bean
in the SpringSecurityConfig file, and to add a new constructor without the variables injected from the 
application.properties file

Save → CTRL + O → ENTER → CTRL + X

modify file by:

cd ~/central-ticket-system-backend/src/main/java/com/example/ticket_system/auth/security/ 
nano SpringSecurityConfig.java

Save → CTRL + O → ENTER → CTRL + X

Also, make sure to modify the sending Coockies attributes in the AuthController to:

                .secure(true)
                .sameSite("None")

since the after deployment, you are using https, and backend and frontend have differnet domains

cd ~/central-ticket-system-backend/src/main/java/com/example/ticket_system/auth/security/ 
nano AuthController.java

Save → CTRL + O → ENTER → CTRL + X


8. Build the Application

Navigate into the project folder:

cd ~/central-ticket-system-backend

Run Maven build:

mvn clean package -DskipTests

(Normally you would use: mvn clean package, but we are ignoring tests)

9. Run the Application
java -jar target/*.jar --spring.config.location=file:/home/ec2-user/central-ticket-system-backend/application-prod.properties

If everything goes well, you sill see the database seeding made successfully to the db. Press Ctrl C to move to second steps 

to run app in the backgroun (later used):

nohup java -jar target/*.jar --spring.config.location=file:/home/ec2-user/central-ticket-system-backend/application-prod.properties > backend.log 2>&1 &

10. Install Cloudflare and Deployment by Clouflare Tunnel (without an authenticated domain):

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

Verify:

cloudflared –version

cloudflared tunnel --url http://localhost:8080 

tunnel working, and public url can be viewed (independent from the Spring Boot App)
but we want it to run in the background so that we can modify the cors.allowedOrigin_2

(note: cors.allowedOrigin_2 can be set to https://*.trycloudflare.com so that everytime we 
create a tunnel the app can still be deployed, but still it is not the best practice for 
configuring CORS)

So we press Ctrl C, Start a new tunnel
nohup cloudflared tunnel --url http://localhost:8080 > cloudflare.log 2>&1 &

Wait a few seconds, Check if it's running
ps aux | grep cloudflared

echo "Your new tunnel URL:"
cat cloudflare.log | grep -o "https://[^ ]*\.trycloudflare\.com" | head -1

output example:
https://complete-males-insight-textbooks.trycloudflare.com

use this output to modify cors.allowedOrigin_2 in application-prod.properties and in the
environment variables of the front end deployment provider (Vercel in the case), modify:

NEXT_PUBLIC_API_URL (make sure to add the url/api)
NEXT_PUBLIC_WS_URL (make sure to add the url/ws)

the app should be working perfectly
the ideal case of the tunnel is to last 30 minutes to a few hours
since no cloudflare account made, and no domain used

