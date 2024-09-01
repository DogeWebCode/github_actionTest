FROM node:22-alpine3.19 AS builder

WORKDIR /app

COPY . .

RUN npm install

RUN npm run build

FROM nginx:1.18

COPY --from=builder /app/dist /usr/share/nginx/html

