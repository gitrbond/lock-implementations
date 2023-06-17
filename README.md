# Lock-implementations
### Implementations and benchmarking of TAS, TTAS, Backoff, CLH and MCS multithread locks.
#### by Roman Bondar & Dmitry Kravtsov.

* TAS (Test-and-set) lock: лок, использующий при взятии лока атомарную операцию testAndSet. 
Из-за сета переменной в цикле, страдает от постоянной инвалидации кеш-линий.
* TTAS (Test-and-test-and-set) lock: решает проблему TAS, спинясь на локальном кеше переменной. 
Но при отпуске лока возникает одновременная инфалидация кешей у всех.
* Backoff (Exponential delay) lock: Если лок взять не получилось, логично что ресурс занят, и не стоит загружать его
запросами. Поэтому таймаут между следующей попыткой взять лок увеличивается.
* CLH (Craig, Landin, and Hagersten) lock: Идея состоит в том чтобы обеспечить порядок FCFS 
(first come - first served) через индуцированную очередь. Каждый поток имеет ссылку на блок предыдущего в очереди, 
показывающий отпущен ли лок. Минус в том что процесс спинится на чужом кеше, что в NUMA архитектурах работает медленно
* MCS lock: та же индуцированная очередь, но спинимся на своем узле.

Проект позволяет запустить тестирование определенного лока при многопоточном доступе к одному ресурсу - для
простоты, был создан класс ```Counter``` со счетчиком. Доступ к счетчику является критической секцией, доступ к которой
организован через нужный лок.

### Сборка
Потребуется Java (тестировалось на версии 17, но должно работать и на предыдущих), gradle. Заходим в корень проекта, где лежит файл build.gradle, запускаем:
```gradle build```
Сборка jar-файла опциональна.

### Запуск
Общая команда для пакетного запуска набора тестов производительности (бенчмарков):

`java -Dverbose -DlockTypes=TAS,TTAS,Backoff;CLH;MCS  "-DnThreads=1 2 4 8 16 24 32 40" -DwarmupIters=5 -DwarmupMillisecs=11000 -DmeasureIters=4 -DmeasureMillisecs=5000 -cp "build\classes\java\test\;build\classes\java\main\"    ru.sbt.mipt.locks.CountingTest`

Параметры:
- ```verbose``` - подробный режим (лучше опускать);
- ```lockTypes=TAS,TTAS,Backoff;CLH;MCS``` - типы локов участвующие в тестировании - любой набор из указанного списка, разделенный запятыми, ; или пробелами (тогда весь параметр в кавычки);
- ```nThreads``` - список количеств потоков, для которых нужно прогнать тесты, разделенный запятыми, ; или пробелами (тогда весь параметр в кавычки);
- ```warmupIters``` - число итераций прогрева JVM; 
- ```warmupMillisecs``` - длительность каждой итерации прогрева в миллисекундах;
- ```measureIters``` - число итераций бенчмарка, результат будет усреднен;
- ```measureMillisecs``` - длительность каждой итерации бенчмарка в миллисекундах.
- `-cp "build\classes\java\test\;build\classes\java\main\"` - пути к классам, получившимся после сборки.
- `ru.sbt.mipt.locks.CountingTest` - какой класс запускать 

### Тестирование при помощи JMH (Java Microbenchmark Harness) 

Команда ```gradle jmh``` позволяет запустить бенчмарк на каждом локе в отдельности 
(исследуется само время между входом и выходом из функции инкремента), и по полученным результатам можно сделать вывод
о latency лока.


~~~