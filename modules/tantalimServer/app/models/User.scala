package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json._

case class User(
                 id: String,
                 username: String,
                 displayName: String)

object User {
  implicit def userWrites: Writes[User] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "username").write[String] and
      (JsPath \ "displayName").write[String]
    )(unlift(User.unapply))
}
