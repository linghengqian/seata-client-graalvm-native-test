# seata-client-graalvm-native-test

- For https://github.com/apache/incubator-seata/pull/5476 and https://github.com/raphw/byte-buddy/issues/1588 .
- Verified `seata-provider` under `Ubuntu 22.04.4 LTS` with `Docker Engine` and `SDKMAN!`.
```bash
sdk install java 22.0.1-graalce
sdk use java 22.0.1-graalce
sudo apt-get install build-essential zlib1g-dev -y

git clone git@github.com:apache/incubator-seata.git
cd ./incubator-seata/
git reset --hard 1c0a442842801413e4dc8e663c452ed18fc1dc1b
./mvnw -Prelease-seata -Dmaven.test.skip=true clean install -T1C

git clone git@github.com:linghengqian/seata-client-graalvm-native-test.git
cd ./seata-client-graalvm-native-test/
./mvnw clean test
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy
./mvnw -PnativeTestInJunit -T1C -e clean test
```

- The Log as follows.
```shell
$./mvnw -PnativeTestInJunit -T1C -e clean test
```