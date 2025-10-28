#!/usr/bin/env bash
set -e

EMAIL="somith727@gmail.com"   # <--- thay bằng email của bạn
DOMAIN="smrs.space"
COMPOSE="docker compose"         # hoặc "docker-compose" nếu bạn dùng phiên bản cũ

echo "1) Start nginx (http only)"
# đảm bảo chỉ có site-http.conf đang tồn tại
cp -f nginx/conf.d/site-http.conf nginx/conf.d/active-site.conf
# site.conf (https) có thể tồn tại nhưng hiện đang ro mount, ta dùng active-site.conf để cung cấp config
# Restart nginx
$COMPOSE up -d nginx

echo "2) Waiting a few seconds for nginx to be reachable..."
sleep 3

echo "3) Request certificate from Let's Encrypt via certbot (webroot)"
$COMPOSE run --rm --entrypoint "\
  certbot certonly --webroot -w /var/www/certbot \
  -d $DOMAIN -d www.$DOMAIN \
  --email $EMAIL --agree-tos --no-eff-email --noninteractive" certbot

echo "4) Switch to HTTPS nginx config and reload"
# copy full config to active file
cp -f nginx/conf.d/site.conf nginx/conf.d/active-site.conf

# reload nginx
$COMPOSE exec nginx nginx -s reload

echo "Done. Certificates stored in docker volume certbot-etc and nginx reloaded."
echo "Check: docker compose exec nginx ls -l /etc/letsencrypt/live/$DOMAIN"
