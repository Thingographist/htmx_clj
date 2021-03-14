# Эксперименты с HTMX

## План

### Чисто HTMX

1. [x] ~~быстрое переключение страниц~~
1. [x] ~~как подключать дополнительные скрипты и css? (append to html)~~
1. [x] ~~обновление графика plotly.js~~
1. [x] ~~дополнительные данные для отправки (id модифицируемого поста. посмотреть в примераах)~~

### Развитие на бакенде

1. [ ] graphql-like запросы в базу
1. [ ] компоненты для бутстрапа
1. [ ] виджеты для страниц
1. [ ] множественные объекты с web-socket
1. [ ] добавить релиз-билд
1. [ ] завернуть релиз-билд в docker

### Непонятно

1. [ ] как организовать теплейты
1. [x] ~~как строить rest~~
1. [ ] валидация параметров и отображение проблем

## базовый конфиг в .env

```
WEB_PORT=4080
MYSQL=jdbc:mysql://localhost:3332/my_db?user=root&password=example&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
```

## запуск

```
make init_db
```

1. запускаем репл
1. через application подключаем стейт
1. в user.db.migrations применяем миграции
1. ребутим стейт
