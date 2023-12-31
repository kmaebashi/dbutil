# dbutil
## これは何?
Java(JDBC)向けのユーティリティライブラリです。現状で以下のふたつの機能があります。
+ PreparedStatementで名前付きパラメタを使うNamedParameterPreparedStatementクラス。内部的に?を使ったSQLに変換します。
+ ResultSetをクラスにマッピングするResultSetMapperクラス。クラスの方には、フィールドごとに属性を付けておきます。

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
