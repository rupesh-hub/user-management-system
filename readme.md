1. Install docker 
2. Install Jenkins
3. Install sonarqube in docker
   - docker run -itd --name sonarqube-server -p 9000:9000 sonarqube:lts-community
   - Initial: username: admin password: admin

4. Install Trivy
   sudo apt-get install wget gnupg
   wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | gpg --dearmor | sudo tee /usr/share/keyrings/trivy.gpg > /dev/null
   echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb generic main" | sudo tee -a /etc/apt/sources.list.d/trivy.list
   sudo apt-get update
   sudo apt-get install trivy

   Example:
   trivy image redis

5. Go to jenkins > manage jenkins and install plugins
    - Jenkins > Manage Jenkins > Plugins > Available Plugins
      a. SonarQube Scanner
      b. Sonar Quality Gates
      c. OWASP Dependency-Check
      d. Docker

6. Jenkins and SonarQube Integration steps (Webhooks)
    - SonarQube > Administration > Configuration > Webhooks > Create
      Name: Jenkins
      URL: http://192.168.1.70:8080/sonarqube-wekhook/ 
      > Create

        - SonarQube > Administration > Security > Users > Tokens
          > Name: Jenkins (or anything)
          > Expires in  (any)
            - squ_bde51f22418332bcd60ae8e34cb02d2d7a4466bd

    - Jenkins > Manage Jenkins > System > SonarQube servers > + Add SonarQube >
      Name: Sonar
      Server URL: http://192.168.1.70:9000
      Server authentication token:
      - click on [+ Add]
      - Or Jenkins > Manage Jenkins > Credentials > (global) > [+ Add Credentials]
      > Kind: Secret text
      > Secret: <token>
      > Id: Sonar
      > Description: Sonar

5. SonarQube Quality Gate
    - Jenkins > Manage Jenkins > Tools > SonarQube Scanner installations > [+ Add SonarQube Scanner]
   > Name: Sonar
   > Version:
   > Add installer > Install From Maven Central

6. Setup OWASP
   -> Jenkins > Manage Jenkins > Tools > Dependency-Check installations > [+ Add Dependency-Check]
   > Name: OWASP
   > Installation directory [Leave Blank]
   > Install automatically [tick]
   > Add Installer > Install from github.com
>

7. Create pipeline