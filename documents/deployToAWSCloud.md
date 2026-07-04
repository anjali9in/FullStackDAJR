
# Deploy Spring Boot JAR to AWS EC2 (Ubuntu)

This guide helps you deploy a Spring Boot application JAR to an EC2 instance.

## Prerequisites

- AWS account
- EC2 key pair file (for example: `spring-boot-app-key.pem`)
- Built Spring Boot JAR in `target/`
- Your app port (default is `8080`)

## 1. Build the JAR Locally

From project root:

```bash
./mvnw clean package -DskipTests
```

Expected output JAR (example):

```bash
target/FullStackDAJR-0.0.1-SNAPSHOT.jar
```

## 2. Create EC2 Instance

Create an EC2 instance from AWS Console:

- OS: Ubuntu (for example Ubuntu 22.04)
- Instance type: `t2.micro` (free-tier eligible, if available)
- Create/download key pair (`.pem`)
- Security Group inbound rules:
  - `22` (SSH) from your IP only
  - `8080` (app port) from allowed clients
  - `80` (optional, for reverse proxy)
  - `443` (optional, for HTTPS)

Note: Avoid opening `22` to `0.0.0.0/0` in production.

## 3. Connect to EC2 via SSH

Use a local folder containing the `.pem` and run:
ssh -i "spring-boot-app-key.pem" ubuntu@your-ec2-public-dns.amazonaws.com

```bash
chmod 400 spring-boot-app-key.pem
ssh -i spring-boot-app-key.pem ubuntu@<ec2-public-dns>
```
Example:

```bash
ssh -i spring-boot-app-key.pem ubuntu@ec2-3-110-45-81.ap-south-1.compute.amazonaws.com

```

## 4. Copy JAR to EC2

Run from local machine (project root):

```bash
scp -i spring-boot-app-key.pem target/FullStackDAJR-0.0.1-SNAPSHOT.jar ubuntu@<ec2-public-dns>:/home/ubuntu/

# for aws jdk

scp -i "spring-boot-app-key.pem" springBootAWS-0.0.1-SNAPSHOT.jar ec2-user@<ec2-public_ip>:/home/ec2-user/

```
ec2-public_ip = ec2-3-110-45-81.ap-south-1.compute.amazonaws.com


## 5. Install Java on EC2

check if Java is installed:
```bash
yum list available | grep java-21
```

### For Ubuntu

```bash
sudo apt update -y
sudo apt install -y openjdk-21-jdk
java -version
```

### For Amazon Linux (only if you chose Amazon Linux AMI)

```bash
sudo yum update -y
sudo yum install -y java-21-amazon-corretto-devel
or
sudo yum install java-21-amazon-corretto-devel.x86_64 -y
java -version
```

## 6. Run the Application (Foreground Test)

```bash
java -jar your-application.jar

ex - 
java -jar /home/ubuntu/FullStackDAJR-0.0.1-SNAPSHOT.jar
```

Press `Ctrl + C` to stop.

## 7. Run in Background with nohup (Quick Option)
```bash
    nohup java -jar your-application.jar &
```
to add logs in log file:

```bash
    nohup java -jar your-application.jar > outputlogs.txt &
```
or 

```bash
nohup java -jar /home/ubuntu/FullStackDAJR-0.0.1-SNAPSHOT.jar > /home/ubuntu/outputlogs.log 2>&1 &
```

Useful commands:

```bash
ps -ef | grep FullStackDAJR
tail -f /home/ubuntu/outputlogs.log
kill <pid>
kill -9 <pid>
```

## 8. Verify from Browser or Postman

```text
http://<ec2-public-dns>:8080/<your-endpoint>

```

If not reachable, confirm:

- App is running
- Security group allows the app port
- Spring Boot is listening on expected port (`8080` by default)

note- you have to add needed port to security group in aws dashboard to access the application from browser
go to instance -> security group -> inbound rules -> edit inbound rules -> add rule -> custom tcp rule -> port 8080 -> source -> 0.0.0.0/0

## 9. Recommended: Run as a systemd Service (Auto Restart + Boot Start)

Create app folder and move JAR:

```bash
mkdir -p /home/ubuntu/app
mv /home/ubuntu/FullStackDAJR-0.0.1-SNAPSHOT.jar /home/ubuntu/app/app.jar
```

Create service file:

```bash
sudo nano /etc/systemd/system/fullstackdajr.service
```

Paste:

```ini
[Unit]
Description=FullStackDAJR Spring Boot App
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/app
ExecStart=/usr/bin/java -jar /home/ubuntu/app/app.jar
SuccessExitStatus=143
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable fullstackdajr
sudo systemctl start fullstackdajr
sudo systemctl status fullstackdajr
```

View logs:

```bash
sudo journalctl -u fullstackdajr -f
```

## 10. Useful Deployment Improvements

- Use a dedicated `prod` profile:

```bash
java -jar app.jar --spring.profiles.active=prod
```

- Add health check endpoint (`/actuator/health`) using Spring Boot Actuator.
- Attach an Elastic IP so public IP does not change after restart.
- Put Nginx in front of app (`80/443`), then route to `localhost:8080`.
- Use HTTPS (for example, Let's Encrypt) for production.
- Restrict security group ports to required IPs only.

## 11. Quick Troubleshooting

```bash
sudo systemctl status fullstackdajr
sudo journalctl -u fullstackdajr --since "10 min ago"
ss -ltnp | grep 8080
curl -I http://localhost:8080
```

Common causes:

- Wrong Java version
- Wrong JAR path/name
- Port not opened in security group
- App failed at startup due to config/environment variables
