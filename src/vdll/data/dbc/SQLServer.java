package vdll.data.dbc;

import java.sql.Connection;
import java.sql.DriverManager;

/** libs/sqljdbc41.jar
 * Created by Hocean on 2017/5/11.
 */
public class SQLServer {
    private static String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static String dbURL = "jdbc:sqlserver://rds0nn217mqz086f831t.sqlserver.rds.aliyuncs.com:3400;database=bluesky_desk";
    private static String userName = "bluesky";
    private static String userPwd = "blueskycits";

    static {
        load();
    }

    public SQLServer() {
    }

    public static void load() {
        try {
            Class.forName(driverName);
        } catch (Exception e) {
        }
    }

    /**
     * 获取数据库连接
     *
     * @return Connection conn
     */
    public static Connection open() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbURL, userName, userPwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
//        #sql Server Sky
//        sky.jdbc.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver
//        sky.jdbc.jdbcUrl =jdbc:sqlserver://rds0nn217mqz086f831t.sqlserver.rds.aliyuncs.com:3400;database=bluesky_desk;
//        sky.jdbc.user =bluesky
//        sky.jdbc.password =blueskycits