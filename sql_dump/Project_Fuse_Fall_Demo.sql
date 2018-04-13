-- MySQL dump 10.13  Distrib 5.7.20, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: project_fuse
-- ------------------------------------------------------
-- Server version	5.7.20-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `interview`
--

LOCK TABLES `interview` WRITE;
/*!40000 ALTER TABLE `interview` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `organization`
--

LOCK TABLES `organization` WRITE;
/*!40000 ALTER TABLE `organization` DISABLE KEYS */;
INSERT INTO `organization` VALUES (1,2,'Jim\'s Class',NULL,1),(2,6,'Full-stack Web Developers',NULL,2);
/*!40000 ALTER TABLE `organization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `organization_invitation`
--

LOCK TABLES `organization_invitation` WRITE;
/*!40000 ALTER TABLE `organization_invitation` DISABLE KEYS */;
/*!40000 ALTER TABLE `organization_invitation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `organization_member`
--

LOCK TABLES `organization_member` WRITE;
/*!40000 ALTER TABLE `organization_member` DISABLE KEYS */;
INSERT INTO `organization_member` VALUES (1,1,2,3),(2,1,2,2),(3,2,6,3),(4,2,6,2);
/*!40000 ALTER TABLE `organization_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `organization_profile`
--

LOCK TABLES `organization_profile` WRITE;
/*!40000 ALTER TABLE `organization_profile` DISABLE KEYS */;
INSERT INTO `organization_profile` VALUES (1,'This is CS 4000 Fall 2017','All senior Computer Science students at the U, please register for this organization! We will be showcasing your projects on this site. This will also be how we keep track of your progress. - Jim',1),(2,'Showcasing and promoting full-stack web development','We are a group of full-stack web developers who collaborate, build, promote, and learn. We are a tight-nit community that creates amazing web sites and push new techniques. ',2);
/*!40000 ALTER TABLE `organization_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `organization_settings`
--

LOCK TABLES `organization_settings` WRITE;
/*!40000 ALTER TABLE `organization_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `organization_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (1,1,'Project Fuse',NULL,1),(2,2,'Jim\'s Juggling Robot',NULL,2),(3,3,'Soccer Training App',NULL,3),(4,4,'DnD RPG Online Tabletop',NULL,4),(5,6,'Pet Sitter',NULL,5);
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `project_invitation`
--

LOCK TABLES `project_invitation` WRITE;
/*!40000 ALTER TABLE `project_invitation` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_invitation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `project_member`
--

LOCK TABLES `project_member` WRITE;
/*!40000 ALTER TABLE `project_member` DISABLE KEYS */;
INSERT INTO `project_member` VALUES (1,1,1,3),(2,1,1,2),(3,2,2,3),(4,2,2,2),(5,3,3,3),(6,3,3,2),(7,4,4,3),(8,4,4,2),(9,5,6,3),(10,5,6,2);
/*!40000 ALTER TABLE `project_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `project_profile`
--

LOCK TABLES `project_profile` WRITE;
/*!40000 ALTER TABLE `project_profile` DISABLE KEYS */;
INSERT INTO `project_profile` VALUES (1,'Helping talented individuals come together','We are a combination of individuals whose experience nets to over 7 years of industry experience with members who have created sites that process millions of dollars a year. We created Project Fuse 2.0 to address issues with team formation, such as finding people and scheduling interviews. ',1),(2,'A juggling robot arm!','This is an incredible project headed by Jim\'s Research Team. We are creating a juggling robot that will be able to juggle multiple balls at once and autonomously. We also will have it be able to juggle different sizes and weights of balls.',2),(3,'Helping players improve their skills!','I\'m a coach for a little league soccer team and it\'s hard to keep track of what each player needs to improve on. The goal of this app is to provide a user interface so that coaches can know how each member of their team is performing and where they can improve.',3),(4,'Bringing dungeon crawlers into the future','Many DnD campaigns have tons of rule books and loads of papers and character sheets. There\'s also tons of dice being rolled. That\'s why we\'re bringing it online. By making an online tabletop RPGers can keep all of their rules and sheets in one place, and they can play remotely too!',4),(5,'Finding short-term caretakers for pets','Have you ever wanted to go on vacation but never been able to find someone to care for your pet? Look no further, Pet Sitter will find you a caretaker for your special animal friend! Simply specify when you need a sitter, pick one from a list, and you\'re done!',5);
/*!40000 ALTER TABLE `project_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `project_settings`
--

LOCK TABLES `project_settings` WRITE;
/*!40000 ALTER TABLE `project_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `schema_version`
--

LOCK TABLES `schema_version` WRITE;
/*!40000 ALTER TABLE `schema_version` DISABLE KEYS */;
INSERT INTO `schema_version` VALUES (1,'1','initial db','SQL','V1__initial_db.sql',-701137480,'root','2017-12-05 23:54:51',180,1),(2,'2','fixed email constraint','SQL','V2__fixed_email_constraint.sql',387054575,'root','2017-12-05 23:54:51',13,1),(3,'3','join groups','SQL','V3__join_groups.sql',-2006110730,'root','2017-12-05 23:54:52',205,1),(4,'4','invitations','SQL','V4__invitations.sql',-1580176440,'root','2017-12-05 23:54:52',92,1),(5,'5','registration','SQL','V5__registration.sql',-1821046126,'root','2017-12-05 23:54:52',52,1),(6,'6','message','SQL','V6__message.sql',122399402,'root','2017-12-05 23:54:52',34,1),(7,'7','interview','SQL','V7__interview.sql',1489576215,'root','2017-12-05 23:54:52',307,1),(8,'8','settings and date and more interviews','SQL','V8__settings_and_date_and_more_interviews.sql',512622790,'root','2017-12-05 23:54:52',123,1),(9,'9','accept invites','SQL','V9__accept_invites.sql',-919765907,'root','2017-12-05 23:54:52',65,1),(10,'10','more entities','SQL','V10__more_entities.sql',1552026557,'root','2017-12-05 23:54:53',286,1);
/*!40000 ALTER TABLE `schema_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `session`
--

LOCK TABLES `session` WRITE;
/*!40000 ALTER TABLE `session` DISABLE KEYS */;
INSERT INTO `session` VALUES ('b94d607c-232d-fe96-82f3-ac79a15acb6a','2017-12-06 01:51:04',6);
/*!40000 ALTER TABLE `session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `taken_interview`
--

LOCK TABLES `taken_interview` WRITE;
/*!40000 ALTER TABLE `taken_interview` DISABLE KEYS */;
/*!40000 ALTER TABLE `taken_interview` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `team`
--

LOCK TABLES `team` WRITE;
/*!40000 ALTER TABLE `team` DISABLE KEYS */;
INSERT INTO `team` VALUES (1,2,'Jim\'s Research Team',NULL,1),(2,3,'Super Soccer Team',NULL,2),(3,6,'The Rubik\'s Cube Solver',NULL,3),(4,4,'Larp Alliance',NULL,4),(5,4,'DnD Mages',NULL,5),(6,6,'DevStack',NULL,6);
/*!40000 ALTER TABLE `team` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `team_invitation`
--

LOCK TABLES `team_invitation` WRITE;
/*!40000 ALTER TABLE `team_invitation` DISABLE KEYS */;
/*!40000 ALTER TABLE `team_invitation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `team_member`
--

LOCK TABLES `team_member` WRITE;
/*!40000 ALTER TABLE `team_member` DISABLE KEYS */;
INSERT INTO `team_member` VALUES (1,1,2,3),(2,1,2,2),(3,2,3,3),(4,2,3,2),(5,3,6,3),(6,3,6,2),(7,4,4,3),(8,4,4,2),(9,5,4,3),(10,5,4,2),(11,6,6,3),(12,6,6,2);
/*!40000 ALTER TABLE `team_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `team_profile`
--

LOCK TABLES `team_profile` WRITE;
/*!40000 ALTER TABLE `team_profile` DISABLE KEYS */;
INSERT INTO `team_profile` VALUES (1,'Jim\'s Super Awesome Research Team','We are a group of students led by the amazing Professor Jim who research computer science and push the boundaries of computing. We do amazing projects all the time. If you are a U of U CS Major and want to join, reach out to Jim!',1),(2,'We make amazing soccer apps','Our goal is to help players become better. Right now we\'re focusing on the little leagues since that\'s where most of our experience is, but our goal is to help all age groups improve their skills!',2),(3,'Solving Rubik\'s Cubes so you don\'t have to','I\'m often frustrated when I encounter a Rubik\'s cube that I can\'t solve, and I\'ve found that many of my peers are frustrated too. So, I want to create a robot that will solve Rubik\'s cubes for me, that way I don\'t have to.',3),(4,'A group of professional LARPers','Salutations! We do lots of LARPing. We schedule tons of events, create tons of costumes, and even do LARPing for charity. We are all-inclusive and non-discriminatory. All are beckoned to join under our banner!',4),(5,'We are the mages of Dungeons and Dragons','Hail adventurer! What bringest thou to our page? Is it the thirst for adventure? A quest for danger? Perhaps the excitement for discover? We have it all. Welcome the the DnD Mages!',5),(6,'A full-stack web development group','We develop state-of-the-art websites. We use cutting-edge technology and lighting fast techniques to create robust, amazing websites. If you need a web application, we can build it for you.',6);
/*!40000 ALTER TABLE `team_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `team_settings`
--

LOCK TABLES `team_settings` WRITE;
/*!40000 ALTER TABLE `team_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `team_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `unregistered_user`
--

LOCK TABLES `unregistered_user` WRITE;
/*!40000 ALTER TABLE `unregistered_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `unregistered_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Team Fusion',NULL,'$2a$10$54fLFsxG3/udasTMwjqhdO3QBXtGxePfkkSpufpnYDkmdvPNgEZ5O','teamfusion@projectfuse.io','r',2),(2,'Jim',NULL,'$2a$10$8Ls1dDPotfM5i3uguaFQG.oeBz3ORmvKZKM.UiOUcBbPPiyZmyaKW','jim@uofu.com','r',4),(3,'Rachel',NULL,'$2a$10$Z.RT.yEPTlqftV.aVO8VzeFOwwmdxNntfA7bs.ePmJAY3BvMuOQzC','rachel@none.com','r',5),(4,'Bob',NULL,'$2a$10$jz7F4YKf8yIqBh/Dt1jihO6nC4GXVKYDdRamKpfYd3X8fL0MRMsu.','bob@none.com','r',6),(5,'Watson',NULL,'$2a$10$py8c9w.cNCw6FTtkM2DGeusbHyRh/fI0fL5iJfJtwHCqrT1Yk9Pua','watson@backer.street','r',1),(6,'John Doe',NULL,'$2a$10$ATcTcDI9YapluOLA26vybuGTS68w9VR8QIeP1uXewoS00T4So4EWm','jd@jd.com','r',3);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_profile`
--

LOCK TABLES `user_profile` WRITE;
/*!40000 ALTER TABLE `user_profile` DISABLE KEYS */;
INSERT INTO `user_profile` VALUES (1,'A journalist for a world-renown detective','I work for a world renown detective, documenting our adventures and reporting on the fascinating crimes we solve. I\'m also looking for a team to update my blog. The technology I\'m using has gotten a little, stale. I\'m hoping to find some help.','Solving Crimes,Running Around,Licensed Doctor,Good Storyteller',5),(2,'The team that made this site','We are the team behind this site. We build and maintain it. Let us know of any ideas you have to improve it.','UI,UX,Java,MySQL,React,Redux,JavaScript,C++,Python,HTML,CSS,Spring',1),(3,'A generic guy','John Doe is a generic name, which makes me perfect for this demo. My goal is to be the account by which you, the user, will be able to see the inside of Project Fuse. While using this product, please be respectful of others and don\'t post profane or offensive content.','UI,UX,C++,Being Awesome,HTML,CSS,C#',6),(4,'Director of Undergraduate Studies ','My name is H. James de St. Germain, but feel free to call me Jim. I am an Associate Professor (Lecturer) for the School of Computing and the Director of Undergraduate Studies. My research interests include Reverse Engineering and Constraint Optimization. (https://www.cs.utah.edu/~germain/)','Teaching,C#,Juggling,Hiking,Soccer,Java,jQuery,Ultimate Frisbee',2),(5,'An enthusiastic soccer nerd','I do programming and soccer (although not at the same time). I love creating apps and I coach a little league soccer team. I want to create an app that helps coaches better instruct and train their players.','Java,Objective-C,Swift,Soccer,Enthusiasm',3),(6,'Stereotypical Nerd','I\'m your stereotypical nerd. I do LARPing, tabletop RPGs (such as DnD), coding, and I\'m a comic book buff (I have an original copy of Green Lama, so don\'t mess with me). I do a lot of C/C++, COBOL, Fortran, and Java.','COBOL,Fortran,x86 Assembly,ARM Assembly,Java,C,C++',4);
/*!40000 ALTER TABLE `user_profile` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-12-05 19:04:29
