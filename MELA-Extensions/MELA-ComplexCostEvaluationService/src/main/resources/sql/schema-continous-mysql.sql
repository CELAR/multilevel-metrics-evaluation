create table IF NOT EXISTS CaschedHistoricalUsage (ID int AUTO_INCREMENT PRIMARY KEY, monSeqID VARCHAR(200), timestampID int, data  LONGBLOB, FOREIGN KEY (monSeqID) REFERENCES MonitoringSeq(ID), FOREIGN KEY (timestampID) REFERENCES Timestamp(ID) );
create table IF NOT EXISTS CaschedCompleteHistoricalUsage (ID int AUTO_INCREMENT PRIMARY KEY, monSeqID VARCHAR(200), timestampID int, data  LONGBLOB, FOREIGN KEY (monSeqID) REFERENCES MonitoringSeq(ID), FOREIGN KEY (timestampID) REFERENCES Timestamp(ID) );
create table IF NOT EXISTS InstantCostHistory (ID int AUTO_INCREMENT PRIMARY KEY, monSeqID VARCHAR(200), timestampID int, data  LONGBLOB, FOREIGN KEY (monSeqID) REFERENCES MonitoringSeq(ID), FOREIGN KEY (timestampID) REFERENCES Timestamp(ID) );
create table IF NOT EXISTS TotalCostHistory (ID int AUTO_INCREMENT PRIMARY KEY, monSeqID VARCHAR(200), timestampID int, data  LONGBLOB, FOREIGN KEY (monSeqID) REFERENCES MonitoringSeq(ID), FOREIGN KEY (timestampID) REFERENCES Timestamp(ID) );
create table IF NOT EXISTS InstantCostElasticitySpace (ID int AUTO_INCREMENT PRIMARY KEY, monSeqID VARCHAR(200),  startTimestampID int, endTimestampID int, elasticitySpace LONGBLOB, FOREIGN KEY (monSeqID) REFERENCES MonitoringSeq(ID), FOREIGN KEY (startTimestampID) REFERENCES Timestamp(ID), FOREIGN KEY (endTimestampID) REFERENCES Timestamp(ID) );
create table IF NOT EXISTS InstantCostElasticityPathway (ID int AUTO_INCREMENT PRIMARY KEY, monSeqID VARCHAR(200),  timestampID int, elasticityPathway LONGBLOB, FOREIGN KEY (monSeqID) REFERENCES MonitoringSeq(ID),FOREIGN KEY (timestampID) REFERENCES Timestamp(ID) );





