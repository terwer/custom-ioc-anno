package com.test.transfer.utils;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author: terwer
 * @date: 2021/12/16 10:10
 * @description: 连接池
 */
public class DruidUtils {

    private DruidUtils(){
    }

    private static DruidDataSource druidDataSource = new DruidDataSource();

    static {
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost:3306/bank");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456");

    }

    public static DruidDataSource getInstance() {
        return druidDataSource;
    }

}
