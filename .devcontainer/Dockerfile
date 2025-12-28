# Dockerfile
# 1. Start from the VS Code base image (Debian Bullseye)
FROM mcr.microsoft.com/devcontainers/base:bullseye

# 2. Install APT packages and tools
RUN set -eux; \
    export DEBIAN_FRONTEND=noninteractive; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
      sudo \
      software-properties-common \
      build-essential \
      make \
      git \
      curl \
      wget \
      unzip \
      lsb-release \
      ca-certificates \
      apt-transport-https \
      openssh-server \
      locales \
      tzdata \
      python3 python3-pip python3-venv \
    ; \
    # Node.js 18.x
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -; \
    apt-get install -y nodejs; \
    # Java (Amazon Corretto 21) + Maven + Gradle
    wget -qO - https://apt.corretto.aws/corretto.key | apt-key add -; \
    add-apt-repository 'deb https://apt.corretto.aws stable main'; \
    apt-get update; \
    apt-get install -y java-21-amazon-corretto-jdk maven gradle; \
    # Go 1.20
    curl -fsSL https://go.dev/dl/go1.20.7.linux-amd64.tar.gz -o /tmp/go.tar.gz; \
    tar -C /usr/local -xzf /tmp/go.tar.gz && rm /tmp/go.tar.gz; \
    # Docker CLI + Podman
    apt-get install -y docker.io podman; \
    # Terraform (HashiCorp repo)
    wget -qO- https://apt.releases.hashicorp.com/gpg | apt-key add -; \
    echo "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main" \
      > /etc/apt/sources.list.d/hashicorp.list; \
    apt-get update; apt-get install -y terraform; \
    # k3d installer
    curl -s https://raw.githubusercontent.com/k3d-io/k3d/main/install.sh | bash; \
    # Extra shells
    apt-get install -y zsh fish; \
    # Cleanup
    apt-get autoremove -y && apt-get clean && rm -rf /var/lib/apt/lists/*

# 3. Create 'codespace' user (with auto-group) and grant sudo
RUN useradd --create-home --shell /bin/bash codespace \
    && echo "codespace ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/codespace

# 4. Prepare Maven settings for codespace user
RUN mkdir -p /home/codespace/.m2 && chown -R codespace:codespace /home/codespace/.m2
COPY settings.xml /home/codespace/.m2/settings.xml
RUN chown codespace:codespace /home/codespace/.m2/settings.xml

# 5. Ensure SSH directory exists and has correct perms
RUN mkdir -p /home/codespace/.ssh && chown codespace /home/codespace/.ssh && chmod 700 /home/codespace/.ssh

# 6. Configure SSH daemon
RUN mkdir -p /var/run/sshd
EXPOSE 2222

# 7. Switch to 'codespace' user by default
USER codespace
WORKDIR /workspace
CMD ["bash", "-lc", "sudo service ssh start && tail -f /dev/null"]