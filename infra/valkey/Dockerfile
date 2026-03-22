FROM valkey/valkey:9.0-alpine

# 디버깅 편의 패키지 추가
RUN apk add --no-cache \
    bash \
    curl \
    netcat-openbsd \
    bind-tools \
    busybox-extras

SHELL ["/bin/bash", "-c"]
