# Stage 1: Build the Angular application
FROM node:18.13 as build

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install

COPY . .

RUN npm run build --prod

# Stage 2: Serve the application with Nginx
FROM nginx:1.21.1-alpine

COPY --from=build /app/dist/demo/browser /usr/share/nginx/html

# Copy the Nginx configuration file
COPY nginx.conf /etc/nginx/conf.d/default.conf
