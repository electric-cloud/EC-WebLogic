EC-WebLogic Test running
============

## MySQL Machine. ##
Need to up and run the MySQL machine

## MySQL Settings##

mysql> DROP USER 'root'@'%';
Query OK, 0 rows affected (0.00 sec)

mysql> FLUSH PRIVILEGES;
Query OK, 0 rows affected (0.00 sec)

mysql> SELECT host FROM mysql.user WHERE User = 'root';
+-----------+
| host      |
+-----------+
| 0.0.0.0   |
| localhost |
+-----------+
2 rows in set (0.00 sec)

mysql> CREATE USER 'root'@'%' IDENTIFIED BY 'root';
Query OK, 0 rows affected (0.00 sec)
mysql> CREATE USER 'root'@'%' IDENTIFIED BY 'root';
Query OK, 0 rows affected (0.00 sec)

mysql> FLUSH PRIVILEGES;
Query OK, 0 rows affected (0.00 sec)

mysql> GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
Query OK, 0 rows affected (0.00 sec)

mysql> FLUSH PRIVILEGES;
Query OK, 0 rows affected (0.00 sec)
mysql> SELECT host FROM mysql.user WHERE User = 'root';
+-----------+
| host      |
+-----------+
| %         |
| 0.0.0.0   |
| localhost |
+-----------+
pvovk@pvovk:~$ mysql -uroot -proot -h 10.200.1.212
mysql: [Warning] Using a password on the command line interface can be insecure.
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 31
Server version: 5.7.22-0ubuntu0.16.04.1-log (Ubuntu)

Copyright (c) 2000, 2019, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.
