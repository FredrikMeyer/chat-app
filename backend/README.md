# chat-app-backend

To build locally:
```
lein with-profile dev uberjar
```

To run:
```
java -jar ./target/uberjar/chat-app-0.1.0-SNAPSHOT-standalone.jar
```

Open `http://host-ip:8080/ui` to see the GraphQL UI.

## Setting host

The host IP `10.0.1.16` is hard coded in `config/dev/config.edn`. This is the IP of my machine on my local network. Using this instead of `localhost` makes it possible to reach the backend on other devices on the same network.

To find your IP, do for example
```
ifconfig | grep inet
```

## Config

See `./config/[dev|prod]/config.edn`. For now, only `host` is set.
