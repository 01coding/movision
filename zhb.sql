DROP TABLE user;
CREATE TABLE user (id int unsigned NOT NULL AUTO_INCREMENT COMMENT '�û�ID', name varchar(256) COMMENT '����', mobile varchar(11) COMMENT '�ֻ�����', password varchar(256) COMMENT '����', salt varchar(256) COMMENT '���ܴ�', status char(1) DEFAULT '0' COMMENT '�ʺ�״̬ 0:��Ч 1:��Ч', PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='�û���';
INSERT INTO user (id, name, mobile, password, salt, status) VALUES (1, 'С־', '18652093798', '83d784512851260234933a426df30b6a', 'aaa0af3b28f8376cb7263a6e2cefdefe', '1');
INSERT INTO user (id, name, mobile, password, salt, status) VALUES (2, '�û�һ', '17700000001', '83d784512851260234933a426df30b6a', 'aaa0af3b28f8376cb7263a6e2cefdefe', '0');
INSERT INTO user (id, name, mobile, password, salt, status) VALUES (3, '�û���', '17700000002', '83d784512851260234933a426df30b6a', 'aaa0af3b28f8376cb7263a6e2cefdefe', '1');
INSERT INTO user (id, name, mobile, password, salt, status) VALUES (4, '�û���', '17700000003', '83d784512851260234933a426df30b6a', 'aaa0af3b28f8376cb7263a6e2cefdefe', '1');
INSERT INTO user (id, name, mobile, password, salt, status) VALUES (5, '�û���', '17700000004', '83d784512851260234933a426df30b6a', 'aaa0af3b28f8376cb7263a6e2cefdefe', '1');
INSERT INTO user (id, name, mobile, password, salt, status) VALUES (6, '�û���', '17700000005', '83d784512851260234933a426df30b6a', 'aaa0af3b28f8376cb7263a6e2cefdefe', '0');
INSERT INTO user (id, name, mobile, password, salt, status) VALUES (7, '�û���', '17700000006', '83d784512851260234933a426df30b6a', 'aaa0af3b28f8376cb7263a6e2cefdefe', '0');
