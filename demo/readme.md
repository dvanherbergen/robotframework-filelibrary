## Demo

To run the demo tests, execute the following steps:


### Install Database

The demo requires a running MySql database. To install and run a MySql database using docker

```
docker run --name rf-demo-mysql -p 3306:3306 \
	-e MYSQL_DATABASE=demo \
	-e MYSQL_USER=user \
	-e MYSQL_PASSWORD=secret2 \
	-d mysql/mysql-server:8.0
```

After the demo, you can stop the database using:

```
docker stop rf-demo-mysql
```

To start it again later, you can use:

```
docker start rf-demo-mysql
```

### Download JDBC driver

```
wget https://repo1.maven.org/maven2/org/hsqldb/hsqldb/2.3.6/hsqldb-2.3.6.jar
```

### Run demo

```
mvn package
robot -P target/robotframework-filelibrary-0.10.2.jar:hsqldb-2.3.6.jar demo
```

