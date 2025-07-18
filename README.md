# web-sugar
Syntactic sugar of Tapir for web development with Scala 3.
## Target of this project
This project would collect all things we used in the development of web server with Scala 3. The libraries of this project would not treat `Performance` or `Cooperation` as first thing, but easily use and write less code. 

So instead of using this project as well promised web framework, just treat it as a syntactic sugar wrapper of Scalatra Web Framework. You could copy the codebase everywhere or import this project using `git submodule` to your project (This is how we used this project). 

**We recommend you fork this project, and change it for your needs.**

## The dependencies the project used
### DB
We choose Postgres for its wonderful complex index support, like array/bson/geo. 

Scalasql is used as ORM.

Flyway is used as a database migration tool.

### Web Framework: Tapir with Netty
We have switched from Scalatra, for it has no well mixed Swagger, OpenTelemetry.
### Http Client: sttp
We used scalaj-http at Scala 2, but it's archived now. So we switch to sttp which provide wrapper of `HttpClient`, It also has invaluable plugins.
### Json:  circe
Both zio-json and circe are ok, they all support Scala 3 well(except enum). But zio-json needs more template code.
### GRPC: ScalaPB
There's no alternative of GRPC in the Scala world. And we choose Netty runtime of GRPC.
### Others:
#### Typesafe Config
Scala world love typesafe config, others not.

## Documentation
We provide [startup template](https://github.com/ForNetCode/web-sugar-startup) for this project.

## What's more
Sometimes we would rewrite the codebase if it could not support our projects, just like switch Scalatra to Tapir for Async. So we could not promise this project would be long-term support.

## License
Apache License 2.0
