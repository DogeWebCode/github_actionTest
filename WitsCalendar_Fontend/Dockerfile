FROM node:22-alpine3.19 AS builder

WORKDIR /app

COPY . .

RUN npm install

RUN npm run build

FROM nginx:1.18

RUN rm /etc/nginx/conf.d/default.conf
RUN echo "server { \
    listen 80; \
    server_name hsu.world; \
    return 301 https://\$host\$request_uri; \
} \
server { \
    listen 443 ssl; \
    server_name hsu.world; \
    root /home/ubuntu/dist; \
    index index.html; \
    ssl_certificate /etc/nginx/ssl/certificate.crt; \
    ssl_certificate_key /etc/nginx/ssl/private.key; \
    location / { \
        try_files \$uri \$uri/ /index.html; \
    } \
    location /events { \
        proxy_pass http://127.0.0.1:8080; \
        proxy_set_header Host \$host; \
        proxy_set_header X-Real-IP \$remote_addr; \
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for; \
        proxy_set_header X-Forwarded-Proto \$scheme; \
        proxy_set_header Authorization \$http_authorization; \
    } \
    error_page 404 /index.html; \
    access_log /app/nginx/hsu.world.access.log; \
    error_log /app/nginx/hsu.world.error.log; \
}" > /etc/nginx/conf.d/default.conf

COPY --from=builder /app/dist /usr/share/nginx/html

