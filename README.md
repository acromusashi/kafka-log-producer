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
### ビルド環境
* JDK 7以降  
* Maven 2.2.1以降

### ビルド手順
* ソースをGitHubから取得後、取得先ディレクトリに移動し下記のコマンドを実行する。  
** コマンド実行の結果、 kafka-log-producer.zip が生成される。  

```
# mvn clean package  
```

## 利用手順
### LinuxApacheLogProducer
#### インストール手順
* kafka-log-producer.zip をログ収集対象サーバの /opt ディレクトリ配下に配置する。
* ログ収集対象サーバにログインし、下記のコマンドを実行する。

```
# cd /opt  
# unzip kafka-log-producer.zip  
# ln -s kafka-log-producer-0.1.0 kafka-log-producer  
# chmod +x kafka-log-producer/bin/*  
# mkdir kafka-log-producer/log  
```

#### 起動手順
* ログ収集対象サーバにログインし、下記のコマンドを実行する。

```
# cd /opt/kafka-log-producer  
# ./start_kafka_producer  
```

#### 終了手順
* ログ収集対象サーバにログインし、下記のコマンドを実行する。

```
# cd /opt/kafka-log-producer  
# ./stop_kafka_producer  
```

### WinApacheLogProducer
#### インストール手順
* kafka-log-producer.zip をログ収集対象サーバの /Tools ディレクトリ配下に配置する。
* kafka-log-producer.zipを展開し、ディレクトリ名を kafka-log-producer にリネームする。

#### 起動手順
* bin/start_kafka_producer.bat を実行する。
* bin/check_apache_logs.bat のディレクトリ設定を更新し、bin/check_apache_logs.bat を実行する。  
** Windowsにおいてはdirコマンドなどでファイルのチェックを行わないとファイルが実際に更新されないため、check_apache_logs.batを実行している。

#### 終了手順
* check_apache_logs.bat を実行しているウィンドウを終了する。
* start_kafka_producer.bat を実行しているウィンドウを終了する。

## 設定項目
### LinuxApacheLogProducer
[LinuxApacheLogProducer.yaml](https://github.com/acromusashi/kafka-log-producer/blob/master/conf/LinuxApacheLogProducer.yaml) 参照。

### WinApacheLogProducer
[WinApacheLogProducer.yaml](https://github.com/acromusashi/kafka-log-producer/blob/master/conf/WinApacheLogProducer.yaml) 参照。

## License
This software is released under the MIT License, see LICENSE.txt.

