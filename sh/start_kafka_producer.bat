@echo off
@rem #-----------------------------------------------------------------------------
@rem # start_kafka_producer.bat
@rem #   kafka-log-producer�̋N���X�N���v�g
@rem #-----------------------------------------------------------------------------
@rem # Producer�̍�ƃf�B���N�g��
set PRODUCER_HOME=C:\Tools\kafka-log-producer

@rem #-----------------------------------------------------------------------------
@rem # Producer�̎��ʖ��B
@rem # ������Producer�𓯎��ɋN������ꍇ�Ƀv���Z�X���ʖ��Ƃ��ė��p�����B
set PRODUCER_NAME=WinApacheLogProducer

@rem #-----------------------------------------------------------------------------
@rem # �N���X�p�X�w��
set CLASSPATH=%PRODUCER_HOME%\conf;%PRODUCER_HOME%\lib\*

@rem #-----------------------------------------------------------------------------
@rem # Producer���N������
echo WinApacheLogProducer���N�����܂�
pause 
java -D"%PRODUCER_NAME%" -cp %CLASSPATH% -Dlogback.configurationFile="%PRODUCER_HOME%"\conf\logback.xml -XX:+HeapDumpOnOutOfMemoryError acromusashi.kafka.log.producer.WinApacheLogProducer -c "%PRODUCER_HOME%"\conf\WinApacheLogProducer.yaml 
pause 

