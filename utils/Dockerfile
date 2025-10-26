# filepath: Dockerfile
FROM ubuntu:22.04

ENV IDEA_VERSION="2025.1"
ENV IDEA_BUILD="ideaIC-${IDEA_VERSION}"
ENV IDEA_TAR_GZ="${IDEA_BUILD}.tar.gz"
ENV IDEA_DOWNLOAD_URL="https://download.jetbrains.com/idea/${IDEA_TAR_GZ}"
ENV IDEA_INSTALL_DIR="/opt/idea"

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjdk-17-jdk \
        wget \
        tar \
        libfuse2 \
        fonts-inter \
    && rm -rf /var/lib/apt/lists/*

# Download, extract, and clean up IntelliJ IDEA
RUN mkdir -p ${IDEA_INSTALL_DIR} && \
    wget -q --show-progress -O /tmp/${IDEA_TAR_GZ} ${IDEA_DOWNLOAD_URL} && \
    tar -xzf /tmp/${IDEA_TAR_GZ} -C ${IDEA_INSTALL_DIR} --strip-components=1 && \
    rm /tmp/${IDEA_TAR_GZ}

# Set environment variable for IntelliJ IDEA executable path
ENV PATH="${IDEA_INSTALL_DIR}/bin:${PATH}"

WORKDIR /home/${USER_NAME}
