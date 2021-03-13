# Эксперименты с HTMX

## План

### Чисто HTMX

1. [ ] быстрое переключение страниц
1. [ ] как подключать дополнительные скрипты и css? (append to html)
1. [ ] обновление графика plotly.js
1. [ ] дополнительные данные для отправки (id модифицируемого поста. посмотреть в примераах)

### Развитие на бакенде

1. [ ] компоненты для бутстрапа
1. [ ] виджеты для страниц
1. [ ] множественные объекты с web-socket

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
