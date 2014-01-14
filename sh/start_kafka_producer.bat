@echo off
@rem #-----------------------------------------------------------------------------
@rem # start_kafka_producer.bat
@rem #   kafka-log-producerの起動スクリプト
@rem #-----------------------------------------------------------------------------
@rem # Producerの作業ディレクトリ
set PRODUCER_HOME=C:\Tools\kafka-log-producer

@rem #-----------------------------------------------------------------------------
@rem # Producerの識別名。
@rem # 複数のProducerを同時に起動する場合にプロセス識別名として利用される。
set PRODUCER_NAME=WinApacheLogProducer

@rem #-----------------------------------------------------------------------------
@rem # クラスパス指定
set CLASSPATH=%PRODUCER_HOME%\conf;%PRODUCER_HOME%\lib\*

@rem #-----------------------------------------------------------------------------
@rem # Producerを起動する
echo WinApacheLogProducerを起動します
pause 
java -D"%PRODUCER_NAME%" -cp %CLASSPATH% -Dlogback.configurationFile="%PRODUCER_HOME%"\conf\logback.xml -XX:+HeapDumpOnOutOfMemoryError acromusashi.kafka.log.producer.WinApacheLogProducer -c "%PRODUCER_HOME%"\conf\WinApacheLogProducer.yaml 
pause 

