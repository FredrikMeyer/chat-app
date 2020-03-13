FROM node:12.2.0-alpine

WORKDIR /app

ENV PATH /app/node_modules/.bin:$PATH

COPY package.json /app/package.json
RUN yarn install --silent

EXPOSE 8888

# start app
CMD ["yarn", "start"]
