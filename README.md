# Эксперименты с HTMX

## базовый конфиг в .env

```
WEB_PORT=4080
STATIC_VERSION=1.1.1
MYSQL=jdbc:mysql://localhost:3332/my_db?user=root&password=example&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
```

## запуск

```
make init_db
```

1. запускаем репл
2. в `pages.routes` ребутим стейт 

```clojure
(do
    (require 'application)
    (application/restart))
```

3. применяем миграции
```clojure
(system.db/migrate)
```
4. еще раз ребутим стейт
```clojure
(do
    (require 'application)
    (application/restart))
```


## План

### Чисто HTMX

1. [x] ~~быстрое переключение страниц~~
1. [x] ~~как подключать дополнительные скрипты и css? (append to html)~~
1. [x] ~~обновление графика plotly.js~~
1. [x] ~~дополнительные данные для отправки (id модифицируемого поста. посмотреть в примераах)~~
1. [x] ~~передвать скрипты в шаблоне через `<script hx-resouces="{js: [...], css: [...]"></script>`~~

### Развитие на бакенде

1. [x] ~~graphql-like запросы в базу~~
1. [x] ~~компоненты для бутстрапа~~
1. [x] ~~виджеты для страниц~~
1. [ ] множественные объекты с web-socket
1. [ ] добавить релиз-билд
1. [ ] завернуть релиз-билд в docker

### MQuery

1. [x] ~~Реализовать функции постобработки `:!first? :!group-by :!order-by :!limit :!offset`~~
1. [x] ~~Преобразование типов~~
1. [ ] макрос для запросов
1. [ ] формирование запроса без мутаций (избавиться от volatile!)
1. [ ] валидация запросов на malli
  - [ ] наличие связей у таблиц
  - [ ] наличие полей у таблиц

### Непонятно

1. [x] ~~как организовать теплейты~~
1. [x] ~~как строить rest~~
1. [x] ~~валидация параметров и отображение проблем~~
