package com.kmaebashi.dbutil;
import com.kmaebashi.dbutil.TableColumn;

public class ResultSetMapperErrorDto1 {
    @TableColumn("INT_VAL")
    double intDummy;

    @TableColumn("REAL_VAL")
    int doubleDummy;

    @TableColumn("BOOLEAN_VAL")
    int booleanDummy;

    @TableColumn("DATE_VAL")
    int dateDummy;

    @TableColumn("TIMESTAMP_VAL")
    int timestampDummy;
}
