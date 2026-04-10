FROM jenkins/jenkins:lts

# Install plugins from plugins.txt
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Install Trivy with scoped repository key (apt-key is deprecated)
USER root
RUN apt-get update && \
	apt-get install -y wget unzip curl gnupg ca-certificates && \
	install -m 0755 -d /etc/apt/keyrings && \
	curl -fsSL https://aquasecurity.github.io/trivy-repo/deb/public.key | gpg --dearmor -o /etc/apt/keyrings/trivy.gpg && \
	chmod a+r /etc/apt/keyrings/trivy.gpg && \
	echo "deb [signed-by=/etc/apt/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb bookworm main" > /etc/apt/sources.list.d/trivy.list && \
	apt-get update && apt-get install -y trivy && \
	rm -rf /var/lib/apt/lists/*

# Install OWASP Dependency-Check
RUN mkdir -p /opt && \
	wget -O /opt/dependency-check.zip https://github.com/jeremylong/DependencyCheck/releases/download/v8.4.0/dependency-check-8.4.0-release.zip && \
	unzip /opt/dependency-check.zip -d /opt && \
	ln -s /opt/dependency-check/bin/dependency-check.sh /usr/local/bin/dependency-check.sh && \
	rm /opt/dependency-check.zip
USER jenkins

# Expose Jenkins ports
EXPOSE 8080 50000

# Default Jenkins home volume
VOLUME /var/jenkins_home
