kafka-log-producerはWindows/Linux上のApacheLog形式のファイルを収集し、KafkaBrokerに対して投入を行うプロセスです。
![Abstract Image](http://acromusashi.github.io/kafka-log-producer/images/Abstract.jpg)

## コンポーネント一覧
### LinuxApacheLogProducer
LinuxのApacheログを収集するProducerプロセス。 
tailコマンドを実行してログの収集を行っています。
### WinApacheLogProducer
WindowsのApacheログを収集するProducerプロセス。 
ファイルの更新をJava側で検知してログの収集を行っています。

## ビルド手順
下記の前提環境を整えます。
### 対象環境
* JDK 7以降
* Maven 2.2.1以降
### ビルド手順
ソースをGitHubから取得後、取得先ディレクトリに移動し下記のコマンドを実行する。  
コマンド実行の結果、「kafka-log-producer.zip」が生成される。  
```
# mvn clean package  
```


## License
This software is released under the MIT License, see LICENSE.txt.

