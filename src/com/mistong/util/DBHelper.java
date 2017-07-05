package com.mistong.util;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;


public final class DBHelper {

	/**
	 * 此方法为获取数据库连接
	 * 
	 * @param database
	 * @return
	 */
	public static Connection getConnection(String database) {
		Connection conn = null;
		try {
			String driver = "com.mysql.jdbc.Driver"; // 数据库驱动
			String url = PropertiesDataProvider.getTestData(
					"config/dbconfig.properties", database + "_url");// 数据库
			String user = PropertiesDataProvider.getTestData(
					"config/dbconfig.properties", database + "_user"); // 用户名
			String password = PropertiesDataProvider.getTestData(
					"config/dbconfig.properties", database + "_password"); // 密码
			Class.forName(driver); // 加载数据库驱动
			if (null == conn) {
				conn = DriverManager.getConnection(url, user, password);
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Sorry,can't find the Driver!");
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 增删改【Add、Del、Update】
	 * 
	 * @param database
	 * @param sql
	 * @return
	 */
	public static int executeNonQuery(String database, String sql) {
		int result = 0;
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getConnection(database);
			stmt = conn.createStatement();
			result = stmt.executeUpdate(sql);
		} catch (SQLException err) {
			err.printStackTrace();
			free(null, stmt, conn);
		} finally {
			free(null, stmt, conn);
		}
		return result;
	}

	/**
	 * 增删改【Add、Delete、Update】
	 * 
	 * @param database
	 * @param sql
	 * @param obj
	 * @return
	 */
	public static int executeNonQuery(String database, String sql,
			Object... obj) {
		int result = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection(database);
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < obj.length; i++) {
				pstmt.setObject(i + 1, obj[i]);
			}
			result = pstmt.executeUpdate();
		} catch (SQLException err) {
			err.printStackTrace();
			free(null, pstmt, conn);
		} finally {
			free(null, pstmt, conn);
		}
		return result;
	}

	/**
	 * 查【Query】
	 * 
	 * @param database
	 * @param sql
	 * @return
	 */
	public static ResultSet executeQuery(String database, String sql) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection(database);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException err) {
			err.printStackTrace();
			free(rs, stmt, conn);
		}
		return rs;
	}

	/**
	 * 查【Query】
	 * 
	 * @param database
	 * @param sql
	 * @param obj
	 * @return
	 */
	public static ResultSet executeQuery(String database, String sql,
			Object... obj) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection(database);
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < obj.length; i++) {
				pstmt.setObject(i + 1, obj[i]);
			}
			rs = pstmt.executeQuery();
		} catch (SQLException err) {
			err.printStackTrace();
			free(rs, pstmt, conn);
		}
		return rs;
	}

	/**
	 * 判断记录是否存在
	 * 
	 * @param database
	 * @param sql
	 * @return
	 */
	public static Boolean isExist(String database, String sql) {
		ResultSet rs = null;
		try {
			rs = executeQuery(database, sql);
			rs.last();
			int count = rs.getRow();
			if (count > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException err) {
			err.printStackTrace();
			free(rs);
			return false;
		} finally {
			free(rs);
		}
	}

	/**
	 * 判断记录是否存在
	 * 
	 * @param database
	 * @param sql
	 * @param obj
	 * @return Boolean
	 */
	public static Boolean isExist(String database, String sql, Object... obj) {
		ResultSet rs = null;
		try {
			rs = executeQuery(database, sql, obj);
			rs.last();
			int count = rs.getRow();
			if (count > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException err) {
			err.printStackTrace();
			free(rs);
			return false;
		} finally {
			free(rs);
		}
	}

	/**
	 * 获取查询记录的总行数
	 * 
	 * @param database
	 * @param sql
	 * @return int
	 */
	public static int getCount(String database, String sql) {
		int result = 0;
		ResultSet rs = null;
		try {
			rs = executeQuery(database, sql);
			rs.last();
			result = rs.getRow();
		} catch (SQLException err) {
			free(rs);
			err.printStackTrace();
		} finally {
			free(rs);
		}
		return result;
	}

	/**
	 * 获取查询记录的总行数
	 * @param database
	 * @param sql
	 * @param obj
	 * @return int
	 */
	public static int getCount(String database, String sql, Object... obj) {
		int result = 0;
		ResultSet rs = null;
		try {
			rs = executeQuery(database, sql, obj);
			rs.last();
			result = rs.getRow();
		} catch (SQLException err) {
			err.printStackTrace();
		} finally {
			free(rs);
		}
		return result;
	}

	/**
	 * 释放【ResultSet】资源
	 * 
	 * @param rs
	 */
	public static void free(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException err) {
			err.printStackTrace();
		}
	}

	/**
	 * 释放【Statement】资源
	 * 
	 * @param st
	 */
	public static void free(Statement st) {
		try {
			if (st != null) {
				st.close();
			}
		} catch (SQLException err) {
			err.printStackTrace();
		}
	}

	/**
	 * 释放【Connection】资源
	 * 
	 * @param conn
	 */
	public static void free(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException err) {
			err.printStackTrace();
		}
	}

	/**
	 * 释放所有数据资源
	 * 
	 * @param rs
	 * @param st
	 * @param conn
	 */
	public static void free(ResultSet rs, Statement st, Connection conn) {
		free(rs);
		free(st);
		free(conn);
	}
}
