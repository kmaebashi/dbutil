package com.kmaebashi.dbutilimpl;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ParameterValueNotFoundException;
import com.kmaebashi.dbutil.SqlParseException;
import com.kmaebashi.dbutil.UnsupportedTypeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.sql.*;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NamedParameterPreparedStatementImplTest {
    @org.junit.jupiter.api.Test
    void testParseSql001() throws Exception {
        String srcSql = """
                -- コメント(:ABC を含む)
                SELECT
                  A, /* Cスタイルコメント(:ABC を含む) */
                  B, /*** Cスタイルコメント(:ABC を含む) ***/
                  A / B
                  A - B
                FROM TABLE_NAME 
                WHERE
                  C = :C_VALUE
                  AND D = :D_VALUE
                  AND E = :C_VALUE
                  AND F = 'abc:def'
                  AND G = 'ab'':def'
                  AND H = 'ab':AFTER_STRING
                  AND I = 'ab'-- after string comment
                  AND J = 'ab'/* after string c-style comment */
                  AND K = 'ab''cd'
                  AND L = 'ab''''''cd'
                """;
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("C_VALUE", "c_value");
        params.put("D_VALUE", "d_value");

        SqlAndParams sqlAndParams = NamedParameterPreparedStatementImpl.parseSql(srcSql);

        String expected = """
                -- コメント(:ABC を含む)
                SELECT
                  A, /* Cスタイルコメント(:ABC を含む) */
                  B, /*** Cスタイルコメント(:ABC を含む) ***/
                  A / B
                  A - B
                FROM TABLE_NAME
                WHERE
                  C = ?
                  AND D = ?
                  AND E = ?
                  AND F = 'abc:def'
                  AND G = 'ab'':def'
                  AND H = 'ab'?
                  AND I = 'ab'-- after string comment
                  AND J = 'ab'/* after string c-style comment */
                  AND K = 'ab''cd'
                  AND L = 'ab''''''cd'
                """;
        assertEquals(expected, sqlAndParams.sql);
        assertEquals(4, sqlAndParams.paramNames.length);
        assertEquals(sqlAndParams.paramNames[0], "C_VALUE");
        assertEquals(sqlAndParams.paramNames[1], "D_VALUE");
        assertEquals(sqlAndParams.paramNames[2], "C_VALUE");
        assertEquals(sqlAndParams.paramNames[3], "AFTER_STRING");
    }

    @org.junit.jupiter.api.Test
    void testParseSqlError001() throws Exception {
        String sql = """
                -- コメント(:を含む)
                SELECT * FROM TABLE_NAME
                WHERE A = :
                AND B = :B_VALUE
                """;

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("B_VALUE", "b_value");

        try {
            SqlAndParams sqlAndParams = NamedParameterPreparedStatementImpl.parseSql(sql);
        } catch (Exception ex) {
            assertTrue(ex instanceof SqlParseException);
            assertEquals(":の後ろに識別子がありません。", ex.getMessage());
        }
    }

    private static Connection conn;
    @BeforeAll
    static void connectDb() throws Exception {
        ResourceBundle rb = ResourceBundle.getBundle("test");

        Class.forName(rb.getString("dbutiltest.driver-class-name"));
        conn = DriverManager.getConnection(rb.getString("dbutiltest.url"),
                rb.getString("dbutiltest.user-name"), rb.getString("dbutiltest.password"));
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void testNamedParameterPreparedStatementImpl001() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE INT_VAL = :INT_VALUE
                """;

        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("INT_VALUE", 10);
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl002() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE INT_VAL > :INT_VALUE_MIN
                AND INT_VAL < :INT_VALUE_MAX
                """;

        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("INT_VALUE_MIN", 9);
        params.put("INT_VALUE_MAX", 11);
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl003() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE REAL_VAL > :REAL_VALUE_MIN
                AND REAL_VAL < :REAL_VALUE_MAX
                """;

        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("REAL_VALUE_MIN", 10.0);
        params.put("REAL_VALUE_MAX", 11.0);
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl004() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE BOOL_VAL = :BOOL_VALUE
                """;

        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("BOOL_VALUE", true);
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl005() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE STR_VAL = :STR_VALUE
                """;
        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("STR_VALUE", "abc");
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl006() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE DATE_VAL = :DATE_VALUE
                """;
        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        LocalDate localDate = LocalDate.of(2023, 11, 4);
        params.put("DATE_VALUE", java.sql.Date.valueOf(localDate));
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl007() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE DATE_VAL = :DATE_VALUE
                """;
        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);
        HashMap<String, Object> params = new HashMap<String, Object>();
        LocalDate localDate = LocalDate.of(2023, 11, 4);
        params.put("DATE_VALUE", localDate);
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl008() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE TIMESTAMP_VAL = :TIMESTAMP_VALUE
                """;
        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        LocalDateTime localDateTime = LocalDateTime.of(2023, 11, 4, 23, 15, 15);
        params.put("TIMESTAMP_VALUE", java.sql.Timestamp.valueOf(localDateTime));
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl009() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE TIMESTAMP_VAL = :TIMESTAMP_VALUE
                """;
        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        LocalDateTime localDateTime = LocalDateTime.of(2023, 11, 4, 23, 15, 15);
        params.put("TIMESTAMP_VALUE", localDateTime);
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl010() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE INT_VAL > :INT_VALUE_MIN
                AND REAL_VAL > :REAL_VALUE_MIN
                AND BOOL_VAL = :BOOL_VALUE
                AND STR_VAL = :STR_VALUE
                AND DATE_VAL > :DATE_VALUE_MIN
                AND TIMESTAMP_VAL > :TIMESTAMP_VALUE_MIN
                AND INT_VAL < :INT_VALUE_MAX
                AND REAL_VAL < :REAL_VALUE_MAX
                AND DATE_VAL < :DATE_VALUE_MAX
                AND DATE_VAL < :DATE_VALUE_MAX
                """;
        NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("INT_VALUE_MIN", 9);
        params.put("REAL_VALUE_MIN", 9.0);
        params.put("BOOL_VALUE", true);
        params.put("STR_VALUE", "abc");
        LocalDate localDateMin = LocalDate.of(2023, 11, 03);
        params.put("DATE_VALUE_MIN", localDateMin);
        LocalDateTime localDateTimeMin = LocalDateTime.of(2023, 11, 04, 23, 15, 0);
        params.put("TIMESTAMP_VALUE_MIN", localDateTimeMin);
        params.put("INT_VALUE_MAX", 11);
        params.put("REAL_VALUE_MAX", 11.0);
        LocalDate localDateMax = LocalDate.of(2023, 11, 05);
        params.put("DATE_VALUE_MAX", localDateMax);
        LocalDateTime localDateTimeMax = LocalDateTime.of(2023, 11, 04, 23, 15, 20);
        params.put("TIMESTAMP_VALUE_MAX", localDateTimeMax);
        npps.setParameters(params);

        ResultSet rs = npps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("selecttest01", testKey);
        assertFalse(rs.next());
    }

    @Test
    void testNamedParameterPreparedStatementImpl011() throws Exception {
        String insertSql = """
                INSERT INTO NAMEDPARAMETERINSERTTEST (
                  TEST_KEY, INT_VAL, REAL_VAL, BOOL_VAL, STR_VAL, DATE_VAL, TIMESTAMP_VAL
                ) VALUES (
                  'inserttest01',
                  :INT_VALUE,
                  :REAL_VALUE,
                  :BOOL_VALUE,
                  :STR_VALUE,
                  :DATE_VALUE,
                  :TIMESTAMP_VALUE
                )
                """;

        NamedParameterPreparedStatement insertNpps = NamedParameterPreparedStatement.newInstance(conn, insertSql);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("INT_VALUE", 10);
        params.put("REAL_VALUE", 10.5);
        params.put("BOOL_VALUE", true);
        params.put("STR_VALUE", "abc");
        LocalDate localDate = LocalDate.of(2023, 11, 4);
        params.put("DATE_VALUE", localDate);
        LocalDateTime localDateTime = LocalDateTime.of(2023, 11, 4, 23, 15, 15);
        params.put("TIMESTAMP_VALUE", localDateTime);
        insertNpps.setParameters(params);

        int insertRet = insertNpps.getPreparedStatement().executeUpdate();
        assertEquals(1, insertRet);

        String selectSql = """
                SELECT * FROM NAMEDPARAMETERINSERTTEST
                WHERE INT_VAL = 10
                """;
        NamedParameterPreparedStatement selectNpps = NamedParameterPreparedStatement.newInstance(conn, selectSql);
        ResultSet rs = selectNpps.getPreparedStatement().executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("inserttest01", testKey);
        assertFalse(rs.next());

        String deleteSql = """
                DELETE FROM NAMEDPARAMETERINSERTTEST
                """;
        NamedParameterPreparedStatement deleteNpps = NamedParameterPreparedStatement.newInstance(conn, deleteSql);
        int deleteRet = deleteNpps.getPreparedStatement().executeUpdate();
        assertEquals(1, deleteRet);
    }

    @Test
    void testNamedParameterPreparedStatementImpl012() throws Exception {
        String insertSql = """
                INSERT INTO NAMEDPARAMETERINSERTTEST (
                  TEST_KEY, INT_VAL, REAL_VAL, BOOL_VAL, STR_VAL, DATE_VAL, TIMESTAMP_VAL
                ) VALUES (
                  'inserttest01',
                  :INT_VALUE,
                  :REAL_VALUE,
                  :BOOL_VALUE,
                  :STR_VALUE,
                  :DATE_VALUE,
                  :TIMESTAMP_VALUE
                )
                """;
        NamedParameterPreparedStatement insertNpps = NamedParameterPreparedStatement.newInstance(conn, insertSql);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("INT_VALUE", null);
        params.put("REAL_VALUE", null);
        params.put("BOOL_VALUE", null);
        params.put("STR_VALUE", null);
        params.put("DATE_VALUE", null);
        params.put("TIMESTAMP_VALUE", null);
        insertNpps.setParameters(params);

        int insertRet = insertNpps.getPreparedStatement().executeUpdate();
        assertEquals(1, insertRet);

        String selectSql = """
                SELECT * FROM NAMEDPARAMETERINSERTTEST
                """;
        PreparedStatement selectPS = conn.prepareStatement(selectSql);
        ResultSet rs = selectPS.executeQuery();
        rs.next();
        String testKey = rs.getString("test_key");
        assertEquals("inserttest01", testKey);
        assertFalse(rs.next());

        String deleteSql = """
                DELETE FROM NAMEDPARAMETERINSERTTEST
                """;
        PreparedStatement deletePS = conn.prepareStatement(deleteSql);
        int deleteRet = deletePS.executeUpdate();
        assertEquals(1, deleteRet);
    }

    @Test
    void testNamedParameterPreparedStatementImplError001() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE STR_VAL = :DUMMY
                """;
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("DUMMY", new byte[100]);
        try {
            NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);
            npps.setParameters(params);
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedTypeException);
            assertEquals("型[Bはサポートしていません。必要に応じて書き足してください。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void testNamedParameterPreparedStatementImplError002() throws Exception {
        String sql = """
                SELECT * from NAMEDPARAMETERTEST
                WHERE STR_VAL = :DUMMY
                """;
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("DUMMY2", "dummy");
        try {
            NamedParameterPreparedStatement npps = NamedParameterPreparedStatement.newInstance(conn, sql);
            npps.setParameters(params);
        } catch (Exception ex) {
            assertTrue(ex instanceof ParameterValueNotFoundException);
            assertEquals("パラメタDUMMYの値が見つかりません。", ex.getMessage());
            return;
        }
        fail();
    }
}