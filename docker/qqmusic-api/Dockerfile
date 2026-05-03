FROM node:18-alpine
WORKDIR /app
RUN apk add --no-cache git && \
    git clone https://github.com/jsososo/QQMusicApi.git . && \
    npm install --production
EXPOSE 3300
CMD ["node", "app.js"]
