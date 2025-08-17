# Build stage
FROM node:20-alpine AS build

WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy built application from build stage
COPY --from=build /app/dist/auth-app/* /usr/share/nginx/html/

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]