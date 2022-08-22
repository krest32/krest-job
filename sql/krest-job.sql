-- MySQL dump 10.13  Distrib 5.7.34, for Win64 (x86_64)
--
-- Host: localhost    Database: krest-job
-- ------------------------------------------------------
-- Server version	5.7.34-log

--
-- Table structure for table `job_handler`
--


CREATE DATABASE `krest-job`
/*!40100 DEFAULT CHARACTER SET utf8 */;

DROP TABLE IF EXISTS `job_handler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job_handler` (
  `id` varchar(100) NOT NULL,
  `app_name` varchar(100) DEFAULT NULL,
  `path` varchar(100) DEFAULT NULL,
  `method_type` varchar(100) DEFAULT NULL,
  `args` varchar(100) DEFAULT NULL,
  `cron` varchar(1000) DEFAULT NULL,
  `last_trigger_time` timestamp NULL DEFAULT NULL,
  `next_trigger_time` timestamp NULL DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `job_type` varchar(100) DEFAULT NULL,
  `job_name` varchar(100) NOT NULL,
  `job_Group` varchar(100) DEFAULT NULL,
  `is_running` tinyint(1) DEFAULT NULL,
  `load_balance_type` varchar(100) DEFAULT NULL,
  `app_pos` bigint(20) DEFAULT '0',
  `retry_times` bigint(20) DEFAULT NULL,
  `service_address` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `job_log`
--

DROP TABLE IF EXISTS `job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job_log` (
  `log_id` varchar(100) NOT NULL,
  `job_id` varchar(100) NOT NULL,
  `run_app` varchar(100) DEFAULT NULL,
  `retry_count` int(11) DEFAULT NULL,
  `result_code` varchar(100) DEFAULT NULL,
  `result_msg` text,
  `create_time` varchar(100) DEFAULT NULL,
  `request_args` varchar(2000) DEFAULT NULL,
  `exception_msg` varchar(2000) DEFAULT NULL,
  `batch_id` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_info`
--

DROP TABLE IF EXISTS `service_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_info` (
  `id` varchar(100) NOT NULL,
  `app_name` varchar(100) NOT NULL,
  `service_address` varchar(100) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL,
  `weight` varchar(100) DEFAULT NULL,
  `service_role` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_lock`
--

DROP TABLE IF EXISTS `service_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_lock` (
  `id` varchar(100) DEFAULT NULL,
  `is_lock` varchar(100) DEFAULT NULL,
  `service_address` varchar(100) DEFAULT NULL,
  `start_time` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_lock`
--

LOCK TABLES `service_lock` WRITE;
/*!40000 ALTER TABLE `service_lock` DISABLE KEYS */;
INSERT INTO `service_lock` VALUES ('1','false',NULL,'2021-11-29 16:00:00');
/*!40000 ALTER TABLE `service_lock` ENABLE KEYS */;
UNLOCK TABLES;
