package com.test.transfer.service.impl;

import com.test.transfer.annotation.AutoWired;
import com.test.transfer.annotation.Service;
import com.test.transfer.annotation.Transactional;
import com.test.transfer.dao.AccountDao;
import com.test.transfer.pojo.Account;
import com.test.transfer.service.TransferService;
import com.test.transfer.utils.TransactionManager;

/**
 * @author: terwer
 * @date: 2021/12/16 10:14
 * @description:
 */
@Service("transferService")
public class TransferServiceImpl implements TransferService {

    @AutoWired
    private AccountDao accountDao;

    @Transactional
    @Override
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {
        Account from = accountDao.queryAccountByCardNo(fromCardNo);
        Account to = accountDao.queryAccountByCardNo(toCardNo);

        from.setMoney(from.getMoney() - money);
        to.setMoney(to.getMoney() + money);

        accountDao.updateAccountByCardNo(to);
        // int c = 1/0;
        accountDao.updateAccountByCardNo(from);
    }
}
