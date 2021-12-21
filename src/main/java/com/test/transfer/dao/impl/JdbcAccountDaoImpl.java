package com.test.transfer.dao.impl;

import com.test.transfer.annotation.AutoWired;
import com.test.transfer.annotation.Component;
import com.test.transfer.annotation.Transactional;
import com.test.transfer.dao.AccountDao;
import com.test.transfer.pojo.Account;
import com.test.transfer.utils.ConnectionUtils;
import com.test.transfer.utils.DruidUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author: terwer
 * @date: 2021/12/16 10:11
 * @description: 连接工具类
 */
@Component("accountDao")
public class JdbcAccountDaoImpl implements AccountDao {

    @AutoWired
    private ConnectionUtils connectionUtils;

    @Override
    public Account queryAccountByCardNo(String cardNo) throws Exception {
        //从连接池获取连接
        // Connection con = DruidUtils.getInstance().getConnection();
        Connection con = connectionUtils.getCurrentThreadConn();
        String sql = "select * from account where cardNo=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1,cardNo);
        ResultSet resultSet = preparedStatement.executeQuery();

        Account account = new Account();
        while(resultSet.next()) {
            account.setCardNo(resultSet.getString("cardNo"));
            account.setName(resultSet.getString("name"));
            account.setMoney(resultSet.getInt("money"));
        }

        resultSet.close();
        preparedStatement.close();
        //con.close();

        return account;
    }

    @Transactional
    @Override
    public int updateAccountByCardNo(Account account) throws Exception {

        // 从连接池获取连接
        // 改造为：从当前线程当中获取绑定的connection连接
        // Connection con = DruidUtils.getInstance().getConnection();
         Connection con = connectionUtils.getCurrentThreadConn();
        String sql = "update account set money=? where cardNo=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setInt(1,account.getMoney());
        preparedStatement.setString(2,account.getCardNo());
        int i = preparedStatement.executeUpdate();

        preparedStatement.close();
        //con.close();
        return i;
    }
}
