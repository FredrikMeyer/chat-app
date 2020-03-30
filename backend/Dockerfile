FROM clojure
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY project.clj /usr/src/app/
RUN lein deps
COPY . /usr/src/app
RUN mv "$(lein with-profile prod uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar
EXPOSE 8080
CMD ["java", "-jar", "app-standalone.jar"]
