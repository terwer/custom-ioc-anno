package com.test.transfer.utils;

import com.test.transfer.annotation.AutoWired;
import com.test.transfer.annotation.Component;

import java.sql.SQLException;

/**
 * @author: terwer
 * @date: 2021/12/16 10:14
 * @description: 事务管理器类：负责手动事务的开启、提交、回滚
 */
@Component("transactionManager")
public class TransactionManager {

    @AutoWired
    private ConnectionUtils connectionUtils;

    // 开启手动事务控制
    public void beginTransaction() throws SQLException {
       connectionUtils.getCurrentThreadConn().setAutoCommit(false);
    }

    // 提交事务
    public void commit() throws SQLException {
       connectionUtils.getCurrentThreadConn().commit();
    }

    // 回滚事务
    public void rollback() throws SQLException {
        connectionUtils.getCurrentThreadConn().rollback();
    }
}
