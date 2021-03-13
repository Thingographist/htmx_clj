.PHONY:dump reload sass clean
SHELL=bash

init_db:
	docker-compose exec db bash -c "mysql -pexample -e 'drop database my_db'"

dump:
	docker-compose exec db bash -c "mysqldump -pexample my_db | gzip > /dumps/dump_`date +'%Y_%m_%d'`.gz"

reload: clean load

clean:
	docker-compose exec db bash -c "mysql -pexample -e 'drop database my_db'"
	docker-compose exec db bash -c "mysql -pexample -e 'create database my_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'"

load:
	docker-compose exec db bash -c "zcat /dumps/`ls -t data/dumps/ | head -1` | mysql -pexample my_db"

sass:
	clojure -M:sass