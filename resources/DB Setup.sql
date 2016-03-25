DROP TABLE IF EXISTS search_chain;
DROP TABLE IF EXISTS search;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS document_events;
CREATE TABLE search_chain (
  id INT PRIMARY KEY AUTO_INCREMENT,
  result_count INT,
  time BIGINT,
  member_token INT,
  search_hash TEXT
);

CREATE TABLE search (
  id INT PRIMARY KEY AUTO_INCREMENT,
  search_chain INT,
  query TEXT,
  type INT
);

CREATE TABLE members (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255)
);

CREATE TABLE document_events (
  id INT PRIMARY KEY AUTO_INCREMENT,
  document_id INT,
  event_type INT,
  `date` BIGINT,
  member_token INT
);