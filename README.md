Telegram-бота, который позволит пользователям создавать, просматривать и удалять дерево категорий.
Проект реализован на Spring Boot.
Для хранения информации о категориях используется база данных PostgreSQL.

Основные функциональные возможности:
Команда: /viewTree
Отображается дерево в структурированном виде.

Команда: /addElement <название элемента>
Этот элемент будет корневым, если у него нет родителя.

Команда: /addElement <родительский элемент> <дочерний элемент>
Добавление дочернего элемента к существующему элементу. Если родительский элемент не существует, выводить соответствующее сообщение.

Команда: /removeElement <название элемента>
При удалении родительского элемента, все дочерние элементы также должны быть удалены. Если элемент не найден, выводить соответствующее сообщение.

Команда: /help
Выводит список всех доступных команд и краткое их описание.

Дополнительные команды:
Команда: /download
Скачивает Excel документ с деревом категорий, формат на ваше усмотрение.

Команда: /upload
Принимает Excel документ с деревом категорий и сохраняет все элементы в базе данных.


