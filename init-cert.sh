#!/bin/bash
set -e

echo "=== [1/4] HTTP 전용 설정으로 Nginx 시작 ==="
cp infra/nginx/default.conf infra/nginx/default.conf.backup
cp infra/nginx/init.conf infra/nginx/default.conf
sudo docker compose up -d app nginx

echo "=== [2/4] DNS 전파 대기 (10초) ==="
sleep 10

echo "=== [3/4] Let's Encrypt 인증서 발급 ==="
sudo docker compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email leeclaude1221@gmail.com \
  --agree-tos \
  --no-eff-email \
  -d api.partition.site

echo "=== [4/4] HTTPS 설정 복원 후 전체 재시작 ==="
cp infra/nginx/default.conf.backup infra/nginx/default.conf
sudo docker compose up -d

echo ""
echo "완료! 아래 주소로 확인하세요:"
echo "  https://api.partition.site/v3/api-docs"