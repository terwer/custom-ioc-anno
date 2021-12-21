package com.test.transfer.service;

/**
 * @author: terwer
 * @date: 2021/12/14 22:56
 * @description: 代理对象工厂，主要用于生产对象
 */
public interface TransferService {

    void transfer(String fromCardNo,String toCardNo,int money) throws Exception;
}
