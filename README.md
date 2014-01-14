kafka-log-producerはWindows/Linux上のApacheLog形式のファイルを収集し、KafkaBrokerに対して投入を行うプロセスです。
![Abstract Image](http://acromusashi.github.io/kafka-log-producer/images/Abstract.jpg)

## コンポーネント一覧
### LinuxApacheLogProducer
LinuxのApacheログを収集するProducerプロセス。 
tailコマンドを実行してログの収集を行っています。
### WinApacheLogProducer
WindowsのApacheログを収集するProducerプロセス。 
ファイルの更新をJava側で検知してログの収集を行っています。
## License
This software is released under the MIT License, see LICENSE.txt.

