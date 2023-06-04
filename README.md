# scalatra-sugar
Syntatic sugar of Scalatra for web development with Scala 3. The project now is used by [ForNetCode/fornet](https://github.com/ForNetCode/fornet) backend server.

## Target of this project
This project would collect all things we used in the development of web server with Scala 3. The libraries of this project would not treat `Performance` or `Cooperation` as first thing, but easily use and write less code. 

So instead of using this project as well promised web framework, just treat it as a synatatic sugar wrapper of Scalatra Web Framework. You could copy the codebase everywhere or import this project using `git submodule` to your project (This is how we used this project).

**We recommond you fork this project, and change it for your needs.**

## The depencies the project used
### DB
We choose Postgres for its wonderful complex index support, like array/bson/geo. 
Protoquill is used as ORM, because at this time(2022Y/11M), It is the only choice of ORM which supports Scala 3.
Flyway is used as database migration tool.
### Web Framework: Scalatra
We have compared it with , But we don't like Annotation, so we choose Scalatra, we don't choose Playframework because of the license of Akka(even we don't believe we could earn enought money to reach the free usage limitation of Akka)
### Json:  zio-json
both zio-json and circe is ok, they all support Scala 3 well(except enum). We have forgot why we don't choose circe. 
### GRPC: ScalaPB
There's no alternative choose of GRPC at Scala world. We choosed Netty runtime of GRPC, also because the license of Akka.
### Others:
#### Keycloak
For SSO.
#### Typesafe Config
Scala world love typesafe config, others not.
## What's more
Sometimes we would rewrite the codebase if it could not support our projects, just like switch Scalatra to Playframework for Async. So we could not promise this project would be long term support, and will not publish it to any jar repository.



