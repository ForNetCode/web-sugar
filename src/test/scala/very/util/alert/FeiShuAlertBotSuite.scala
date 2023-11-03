package very.util.alert

import munit.FunSuite
import very.util.config.WithConfig

class FeiShuAlertBotSuite extends FunSuite with WithConfig {
  test("simple") {
    assert(FeiShuAlertBot(config.getString("alert.feishu.url")).send("Hello World"))
  }

}
