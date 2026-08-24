FROM node:24-alpine AS build

WORKDIR /app

COPY frontend-typescript/package.json .
COPY frontend-typescript/package-lock.json .

RUN npm ci

COPY frontend-typescript/ .

ARG VITE_ANALYTICS_API_URL=http://localhost:8000
ENV VITE_ANALYTICS_API_URL=${VITE_ANALYTICS_API_URL}

RUN npm run build


FROM nginx:stable-alpine AS runtime

COPY frontend-typescript/nginx.conf \
    /etc/nginx/conf.d/default.conf

COPY --from=build \
    /app/dist \
    /usr/share/nginx/html

EXPOSE 80