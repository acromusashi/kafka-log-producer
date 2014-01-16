## 概要
kafka-log-producerはWindows/Linux上のApacheLog形式のファイルを収集し、KafkaBrokerに対して投入を行うプロセスです。
## システム構成イメージ
![Abstract Image](http://acromusashi.github.io/kafka-log-producer/images/Abstract.jpg)


## スタートガイド
### ビルド環境
* JDK 7以降  
* Maven 2.2.1以降

### ビルド手順
* ソースをGitHubから取得後、取得先ディレクトリに移動し下記のコマンドを実行してください。  
** コマンド実行の結果、 kafka-log-producer.zip が生成されます。  

```
# mvn clean package  
```

### 利用手順
#### LinuxApacheLog収集Producer
##### インストール手順
* kafka-log-producer.zip をログ収集対象サーバの /opt ディレクトリ配下に配置します。
* ログ収集対象サーバにログインし、下記のコマンドを実行します。

```
# cd /opt  
# unzip kafka-log-producer.zip  
# ln -s kafka-log-producer-0.1.0 kafka-log-producer  
# chmod +x kafka-log-producer/bin/*  
# mkdir kafka-log-producer/log  
```

##### 起動手順
* ログ収集対象サーバにログインし、下記のコマンドを実行します。

```
# cd /opt/kafka-log-producer  
# ./start_kafka_producer  
```

##### 終了手順
* ログ収集対象サーバにログインし、下記のコマンドを実行します。

```
# cd /opt/kafka-log-producer  
# ./stop_kafka_producer  
```

#### WinApacheLog収集Producer
##### インストール手順
* kafka-log-producer.zip をログ収集対象サーバの /Tools ディレクトリ配下に配置します。
* kafka-log-producer.zipを展開し、ディレクトリ名を kafka-log-producer にリネームします。

##### 起動手順
* bin/start_kafka_producer.bat を実行する。
* bin/check_apache_logs.bat のディレクトリ設定を更新し、bin/check_apache_logs.bat を実行します。  
** Windowsにおいてはdirコマンドなどでファイルのチェックを行わないとファイルが実際に更新されないため、check_apache_logs.batを実行しています。

##### 終了手順
* check_apache_logs.bat を実行しているウィンドウを終了します。
* start_kafka_producer.bat を実行しているウィンドウを終了します。

##### 設定項目
[WinApacheLogProducer.yaml](https://github.com/acromusashi/kafka-log-producer/blob/master/conf/WinApacheLogProducer.yaml) 参照。


## 機能一覧
### LinuxApacheLog収集Producer
LinuxのApacheログを収集するためにはLinuxApacheLogProducerを利用します。  
Linux上のApacheが出力したログをKafkaクラスタに投入することができます。  
設定項目は「スタートガイド」を参照してください。
### WinApacheLog収集Producer
WindowsのApacheログを収集するためにはWinApacheLogProducerを利用します。  
Windows上のApacheが出力したログをKafkaクラスタに投入することができます。  
設定項目は「スタートガイド」を参照してください。

## ダウンロード

## License
This software is released under the MIT License, see LICENSE.txt.

