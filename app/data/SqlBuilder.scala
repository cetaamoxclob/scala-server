package data

case class SqlBuilder(from: String,
                      fields: Seq[SqlField] = Seq.empty,
                       where: Seq[SqlWhere] = Seq.empty
)

case class SqlField(name: String, sql: String)

case class SqlWhere(name: String, sql: String)
