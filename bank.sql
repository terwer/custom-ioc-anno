/*
 Navicat Premium Data Transfer

 Source Server         : local_mysql
 Source Server Type    : MySQL
 Source Server Version : 50731
 Source Host           : localhost:3306
 Source Schema         : bank

 Target Server Type    : MySQL
 Target Server Version : 50731
 File Encoding         : 65001

 Date: 16/12/2021 07:45:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `name` varchar(255) DEFAULT NULL COMMENT '用户名',
  `money` int(11) DEFAULT NULL COMMENT '账户金额',
  `cardNo` varchar(255) DEFAULT NULL COMMENT '银行卡号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of account
-- ----------------------------
BEGIN;
INSERT INTO `account` VALUES ('小风', 9100, '6029621011000');
INSERT INTO `account` VALUES ('小雨', 11000, '6029621011001');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
