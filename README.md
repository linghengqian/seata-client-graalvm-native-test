# seata-client-graalvm-native-test

- For https://github.com/apache/incubator-seata/issues/6686 and https://github.com/raphw/byte-buddy/issues/1588 .
- Verified under `Ubuntu 22.04.4 LTS` with `Docker Engine` and `SDKMAN!`.
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

# Running unit tests under the JVM
./mvnw clean test

# Running `./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy` is not necessary unless unit tests require more GRM
# ./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy

# Running unit tests under GraalVM Native Image
./mvnw -PnativeTestInJunit -T1C -e clean test
```

- The Log as follows.
```shell
$./mvnw -PnativeTestInJunit -T1C -e clean test

com.lingh.SeataTest > assertSeataTransactions() SUCCESSFUL


Test run finished after 7408 ms
[         2 containers found      ]
[         0 containers skipped    ]
[         2 containers started    ]
[         0 containers aborted    ]
[         2 containers successful ]
[         0 containers failed     ]
[         1 tests found           ]
[         0 tests skipped         ]
[         1 tests started         ]
[         0 tests aborted         ]
[         1 tests successful      ]
[         0 tests failed          ]

```