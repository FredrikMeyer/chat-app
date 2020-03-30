FROM node:12.2.0-alpine as build

WORKDIR /app

COPY package.json /app/package.json
RUN yarn install
COPY . /app

RUN yarn build

## Production

FROM node:12.2.0-alpine
RUN yarn global add serve 
WORKDIR /app
COPY --from=build /app/build .
CMD ["serve","-s", "-l", "8080", "-s", "."]
