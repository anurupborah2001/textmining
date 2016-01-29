
CREATE TABLE `pattern` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pattern` longtext,
  `nValue` int(11) DEFAULT NULL,
  `slotPosition` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=101236 DEFAULT CHARSET=utf8


CREATE TABLE `userreview` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `text` longtext,
  `sideeffects` longtext COMMENT 'POSITIVE/NEGATIVE',
  `SNO` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34249 DEFAULT CHARSET=utf8


CREATE TABLE `splittedreview` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `reviewText` text,
  `isProcessed` tinyint(1) DEFAULT NULL,
  `userReviewId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FULLTEXT KEY `reviewText` (`reviewText`)
) ENGINE=MyISAM AUTO_INCREMENT=296916 DEFAULT CHARSET=utf8

CREATE TABLE `slot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userReviewId` bigint(20) DEFAULT NULL,
  `slotText` longtext,
  `hasSideEffect` tinyint(1) DEFAULT NULL,
  `sideEffects` longtext,
  PRIMARY KEY (`id`),
  KEY `fk_Slot_Pattern` (`userReviewId`),
  CONSTRAINT `fk_Slot_userReview` FOREIGN KEY (`userReviewId`) REFERENCES `userreview` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2166 DEFAULT CHARSET=utf8

CREATE TABLE `sideeffect` (
  `sideeffectId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `sideEffect` text,
  PRIMARY KEY (`sideeffectId`),
  FULLTEXT KEY `sideeffect` (`sideEffect`)
) ENGINE=MyISAM AUTO_INCREMENT=365825 DEFAULT CHARSET=utf8


