.PHONY:dump reload sass clean
SHELL=bash
DB=my_db

init_db:
	docker-compose exec db bash -c "mysql -pexample -e 'create database $(DB) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'"

dump:
	docker-compose exec db bash -c "mysqldump -pexample $(DB) | gzip > /dumps/dump_`date +'%Y_%m_%d'`.gz"

reload: clean load

clean:
	docker-compose exec db bash -c "mysql -pexample -e 'drop database $(DB)'"
	docker-compose exec db bash -c "mysql -pexample -e 'create database $(DB) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'"

load:
	docker-compose exec db bash -c "zcat /dumps/`ls -t data/dumps/ | head -1` | mysql -pexample $(DB)"

sass:
	clojure -M:sass