在启动数据库容器命令中添加--auth参数。 
docker run --name some-mongo -d mongo:3.4 --auth
使用exec命令进入命令行，并添加用户名和密码。 
docker exec -it some-mongo mongo admin 

db.createUser({ user: 'jsmith', pwd: 'some-initial-password', roles: [ { role: "userAdminAnyDatabase", db: "admin" } ] });

db.createUser({ user: 'dg', pwd: 'dg', roles: [ { role: "userAdminAnyDatabase", db: "admin" } ] });


db.createUser({ user: 'kj', pwd: 'some-initial-password', roles: [ { role: "root", db: "admin" } ] });


db.dg.insert({"name":"kj"})

