# dbutil
## これは何?
Java(JDBC)向けのユーティリティライブラリです。現状で以下のふたつの機能があります。
+ PreparedStatementで名前付きパラメタを使うNamedParameterPreparedStatementクラス。内部的に?を使ったSQLに変換します。
+ ResultSetをクラスにマッピングするResultSetMapperクラス。クラスの方には、フィールドごとにアノテーションを付けておきます。

## NamedParameterPreparedStatementクラス


## ResultSetMapperクラス
ResultSetをDTO(Data Transfer Object)となるクラスにマッピングします。
DTOクラスには、テーブルの列名と対応付けるためにアノテーションを付けておきます。
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



