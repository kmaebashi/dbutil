# dbutil
## これは何?
Java(JDBC)向けのユーティリティライブラリです。現状で以下のふたつの機能があります。
+ PreparedStatementで名前付きパラメタを使うNamedParameterPreparedStatementクラス。内部的に?を使ったSQLに変換します。
+ ResultSetをクラスにマッピングするResultSetMapperクラス。クラスの方には、フィールドごとにアノテーションを付けておきます。

Java17 + PostgreSQL15.3で動作確認しています。

## NamedParameterPreparedStatementクラス
JDBCでPreparedStatementを使う時は、プレースホルダには「?」しか使えず、何番目の「?」なのかを数えないといけませんが、
「:PARAM_NAME」のようにコロンで始まる名前付きパラメタを使えるようにします。
C#あたりだと昔から使えますし、Spring FrameworkのNamedParameterJdbcTemplateと同様の機能です。
- 名前付きパラメタの名前は、Javaの`Character.isJavaIdentifierStart()`がtrueの文字で始まり、
  以後`Character.isJavaIdentifierPart()`がtrueの文字が続く文字列です。
  要はJavaの変数名と同じですので、通常の使用では、「英大文字小文字アンダースコアから始まり、
  以後は「英大文字小文字アンダースコア数字」と考えてよいかと思います。
- 「`--`」から始まるSQLコメント、「`/*`」から「`*/`」までのCスタイルコメント、「`'`」で囲まれた
  SQL文字列リテラルの中にコロンで始まる名前付きパラメタらしき文字列があっても、
  そこは名前付きパラメタとはみなしません。
  
```
// 名前付きパラメタを含むSQLを書く。
String sql = """
  INSERT INTO USERS (
    SERIALID,
    NAME,
    ADDRESS,
    TEL
  ) VALUES (
    :SERIALID,
    :NAME,
    :ADDRESS,
    :TEL
  )
  """;
// NamedParameterPreparedStatementのインスタンス生成。
// 第1引数はjava.sql.Connection, 第2引数が名前付きパラメタを含むSQLの文字列。
NamedParameterPreparedStatement npps
            = NamedParameterPreparedStatement.newInstance(conn, sql);
// 各パラメタの名前と値の組をMapに詰め込む。
var params = new HashMap<String, Object>();
params.put("SERIALID", serialId);
params.put("NAME", name);
params.put("ADDRESS", address);
params.put("TEL", tel);
// NamedParameterPreparedStatementに対してパラメタを設定する。
npps.setParameters(params);
// NamedParameterPreparedStatementからPreparedStatementを取得し、
// 以後は好きにする。
int result = npps.getPreparedStatement().executeUpdate();
```
`Map`に値として格納できるデータ型は以下の通りです(プリミティブ型は当然auto boxingで拡張されるとして)。
+ Integer
+ Double
+ Boolean
+ String
+ java.sql.Date
+ LocalDate(java.sql.Dateに変換します)
+ java.sql.Timestamp
+ LocalDateTime(java.sql.Timestampに変換します)
これ以外の型を設定した場合、UnsupportedTypeExceptionがスローされます。必要に応じて書き足してください。

## ResultSetMapperクラス
JDBCにおいてDBからの検索結果を保持するResultSetの内容を、DTO(Data Transfer Object)となるクラスにマッピングします。

この機能はDBの検索結果からクラスへの一方通行のマッピングであり、(JPAとかのような)クラスからDBに書き込む機能はありません。

これを使うには、まずDTOとするクラスに、テーブルの列名と対応付けるためにアノテーションを付けます。

```
import com.kmaebashi.dbutil.TableColumn;

public class Person {
    @TableColumn("SERIALID")
    public int serialId;

    @TableColumn("NAME")
    public String name;

    @TableColumn("ADDRESS")
    public String address;

    @TableColumn("TEL")
    public String tel;
}
```
こうしておいて、データが複数件の場合は`ResultSetMapper.toDtoList()`メソッドを、データが1件の場合には`ResultSetMapper.toDto()`メソッドを使うことで、ResultSetからDTOへの変換ができます。
```
// ResultSetからList<Person>に変換する。
// 第1引数はResultSet, 第2引数はDTOのクラス。
List<Person> personList = ResultSetMapper.toDtoList(rs, Person.class);

// ResultSetからList<Person>に変換する。
// 第1引数はResultSet, 第2引数はDTOのクラス。
// 1件も取得できなければ結果はnull、2件以上取得出来たらMultipleMatchExceptionを投げる。
Person person = ResultSetMapper.toDto(rs, Person.class);
```
JDBCの型(java.sql.Types)とJavaの型との対応付けは以下の通り。

| java.sql.Types | Javaの型 |
| ---- | ---- |
| Types.INTEGER | intまたはInteger |
| Types.REAL | doubleまたはDouble |
| Types.BITまたはTypes.BOOLEAN | booleanまたはBoolean |
| Types.CHAR | String |
| Types.VARCHARまたはTypes.NVARCHAR | String |
| Types.DATE | java.util.DateまたはLocalDate |
| Types.TIMESTAMP | java.util.DateまたはLocalDateTime |
| 上記以外 | UnsupportedTypeExceptionを投げる |

DBの型がこの表の左側である場合、DTOのフィールドの型はこの表の右側の型のいずれかでなければいけません(異なる場合、UnsupportedTypeExceptionを投げます)。

DBの値がNULLの時、intやdoubleやbooleanといったプリミティブ型で受けると、0や0.0やfalseが設定されます(これは、ResultSetのgetXXX()の仕様を踏襲しています)。

Types.CHARの場合に限り、以下のようにtrim=trueを付けることで、取得時に末尾の空白をトリムすることができます。これはJavaのString.stripTrailing()メソッドを使っているので、全角の空白も除去されます。
```
@TableColumn(value="CHAR_COLUMN", trim=true)
    public String charColumn;
```
この場合、列名は「`value="列名"`」の形式で書く必要があります。

## ライセンスについて
NYSL Version 0.9982とします。作者は一切の著作権を主張しませんので、改変するなり煮るなり焼くなり好きにしてください。
http://www.kmonos.net/nysl/

