-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jul 23, 2016 at 03:13 PM
-- Server version: 5.6.17
-- PHP Version: 5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `951787`
--

-- --------------------------------------------------------

--
-- Table structure for table `friends`
--

CREATE TABLE IF NOT EXISTS `friends` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ID` char(16) NOT NULL,
  `USER_ID_FRIEND` char(16) NOT NULL,
  `FRIENDS_SINCE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `LAST_MODIFIED` timestamp NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=5 ;

-- --------------------------------------------------------

--
-- Table structure for table `location`
--

CREATE TABLE IF NOT EXISTS `location` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ID` char(16) NOT NULL,
  `LAT` double NOT NULL,
  `LNG` double NOT NULL,
  `TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `ACCURACY` float NOT NULL,
  `LAST_MODIFIED` timestamp NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=19 ;

-- --------------------------------------------------------

--
-- Table structure for table `request`
--

CREATE TABLE IF NOT EXISTS `request` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ID_FROM` char(16) NOT NULL,
  `USER_ID_TO` char(16) NOT NULL,
  `IS_ACCEPTED` tinyint(1) DEFAULT NULL,
  `DATE_REQUEST` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DATE_ACCEPTED` timestamp NOT NULL,
  `LAST_MODIFIED` timestamp NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=8 ;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ID` char(16) NOT NULL,
  `USER_NAME` char(128) NOT NULL,
  `PASSWORD` char(128) NOT NULL,
  `EMAIL` char(255) NOT NULL,
  `MOBILE` char(20) NOT NULL,
  `PROFILE_PIC` varchar(512) NOT NULL,
  `PROFILE_PIC_CHECKSUM` varchar(512) NOT NULL,
  `LAST_MODIFIED` timestamp NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `USERNAME` (`USER_ID`),
  KEY `USERNAME_2` (`USER_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=10 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
