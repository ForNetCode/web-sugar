package very.util.keycloak

import com.typesafe.config.ConfigFactory
import munit.FunSuite
import sttp.client3.{ HttpClientSyncBackend }

class KeycloakAdminAPISuite extends FunSuite {
  private def config = KeycloakConfig.loadFromConfig(ConfigFactory.load().getConfig("keycloak"))

  def getAdminAPI = {
    val client = HttpClientSyncBackend()
    KeycloakAdminAPI(config, client)
  }
  test("get token") {
    val result = getAdminAPI.getTokenByPassword()
    println(result)
    println(result.toTry.get.refresh_token)
  }

  test("refresh token") {
    println(getAdminAPI.getTokenByRefreshToken(
      "eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0ZjhhOTc0Zi02ZGY2LTQ2ZGYtODk3OC1hZjFkZTYzMzNlMGIifQ.eyJleHAiOjE3Mzg0NzU3MzYsImlhdCI6MTczODQ3MzkzNiwianRpIjoiMmYzMjFkNGYtMTBhMy00Mzg0LWExMjItY2M5N2VhZDU5OWI4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MTgwL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgxODAvcmVhbG1zL21hc3RlciIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJhZG1pbi1jbGkiLCJzaWQiOiI3ZjhjMDM3ZS1iZDEzLTRjNWMtOTQ3ZS1hZTI2MWQ2MmUzYTUiLCJzY29wZSI6Im9wZW5pZCByb2xlcyBlbWFpbCB3ZWItb3JpZ2lucyBhY3IgcHJvZmlsZSBiYXNpYyJ9.59r_eGXmy5eV6Dole19-4DyE7SzSiCLqQkmsA_oJvUuyGr0LHnd7qWY2zi8A4NBGJg7f6Tv-T9V0mFXgNCx-6A"
    ))
  }
}
