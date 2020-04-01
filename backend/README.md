# chat-app-backend

To build:
```
lein with-profile prod uberjar
docker build . -t chat-app-backend
```

To run:
```
docker run -it -p 8888:8888 chat-app-backend
```

(this to change)

## Config

See `./config/[dev|prod]/config.edn`. For now, only `host` is set.
