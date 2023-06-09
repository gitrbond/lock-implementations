# lock-implementations
Implementations and benchmarking of TAS, TTAS, Backoff, CLH and MCS multithread locks by Roman Bondar & Dmitry Kravtsov.

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

# Запуск

Сборка: ```gradle build```

Проект позволяет запустить тестирование определенного лока при многопоточном доступе к одному ресурсу - для 
простоты, был создан класс ```Counter``` со счетчиком. Доступ к счетчику является критической секцией, доступ к которой
организован через нужный лок.

Запустить тест конкретного лока с параметрами можно командой 
```gradle test --tests CountingTest.TASLockTest -DnThreads=3 -DmeasureTasks=100000```. 
Синтаксис параметров: ```-D<option>=<value>```. 
Будет запущено тестирование 3 потоков, выполняющих параллельно 100_000 операций инкремента на counter-е, 
доступ к которому организован при помощи лока ```TASLock```.

Доступные параметры:
- ```nThreads``` - число потоков в тестировании
- ```warmupTasks``` - общее число задач на инкремент, распеделенных между потоками на одном этапе разогрева
- ```warmupIters``` - число итераций разогрева (в каждой итерации выполняется warmupTasks операций)
- ```measureTasks``` - общее число задач на инкремент, распеделенных между потоками на одном этапе измерений
- ```measureIters``` - число итераций измерений, результат будет усреднен.

# Тестирование при помощи JMH (Java Microbenchmark Harness) 

Команда ```gradle jmh``` позволяет запустить бенчмарк на каждом локе в отдельности 
(исследуется само время между входом и выходом из функции инкремента), и по полученным результатам можно сделать вывод
о latency лока.

# Предварительные результаты тестирования 
Результат исполнение команды `gradle test -DnThreads=5 -DwarmupTasks=1000000 -DwarmupIters=7 -DmeasureTasks=1000000 -DmeasureIters=3`:

Самым быстрым оказался BackoffLock, а самым медленным - TASLock. На предпоследнем месте MCSLock, что удивительно и требует дальнейшего исследования. Следом TTASLock и CLHLock с ощутимой разницей в перфомансе. 
~~~
$
Benchmark results for BackoffLock:
BenchmarkOptions[nThreads=5, warmupIterations=7, nWarmupTotalTasks=1000000, measureIterations=3, nMeasureTotalTasks=1000000]
avgTime to execute one task = 2330ns
$
Benchmark results for CLHLock:
BenchmarkOptions[nThreads=5, warmupIterations=7, nWarmupTotalTasks=1000000, measureIterations=3, nMeasureTotalTasks=1000000]
avgTime to execute one task = 3250ns
$
Benchmark results for TTASLock:
BenchmarkOptions[nThreads=5, warmupIterations=7, nWarmupTotalTasks=1000000, measureIterations=3, nMeasureTotalTasks=1000000]
avgTime to execute one task = 3805ns
$
Benchmark results for MCSLock:
BenchmarkOptions[nThreads=5, warmupIterations=7, nWarmupTotalTasks=1000000, measureIterations=3, nMeasureTotalTasks=1000000]
avgTime to execute one task = 4010ns
$
Benchmark results for TASLock:
BenchmarkOptions[nThreads=5, warmupIterations=7, nWarmupTotalTasks=1000000, measureIterations=3, nMeasureTotalTasks=1000000]
avgTime to execute one task = 4025ns
~~~