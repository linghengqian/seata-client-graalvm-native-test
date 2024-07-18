package com.lingh;

import com.lingh.commons.repository.TestShardingService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.core.exception.TransactionException;
import io.seata.core.rpc.netty.RmNettyRemotingClient;
import io.seata.rm.RMClient;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.tm.TMClient;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("resource")
@Testcontainers
public class SeataTest {

    @Container
    public GenericContainer<?> seataContainer = new GenericContainer<>("apache/seata-server:2.1.0")
            .withExposedPorts(7091, 8091)
            .waitingFor(Wait.forHttp("/health").forPort(7091).forResponsePredicate("ok"::equals));

    private static final String SERVICE_DEFAULT_GROUP_LIST_KEY = "service.default.grouplist";

    @Test
    void assertSeataTransactions() throws SQLException, TransactionException {
        assertThat(System.getProperty(SERVICE_DEFAULT_GROUP_LIST_KEY), is(nullValue()));
        System.setProperty(SERVICE_DEFAULT_GROUP_LIST_KEY, "127.0.0.1:" + seataContainer.getMappedPort(8091));
        String applicationId = "seata-client-graalvm-native-test";
        String txServiceGroup = "default_tx_group";
        TMClient.init(applicationId, txServiceGroup);
        RMClient.init(applicationId, txServiceGroup);
        RmNettyRemotingClient rmNettyRemotingClient = RmNettyRemotingClient.getInstance();
        Class<? extends RmNettyRemotingClient> rmRemoteClass = rmNettyRemotingClient.getClass();
        ReflectionUtils.doWithFields(rmRemoteClass, field -> {
            if (field.getName().equals("clientChannelManager")) {
                field.setAccessible(true);
                Object clientChannelManager = field.get(rmNettyRemotingClient);
                try {
                    Method reconnect = clientChannelManager.getClass().getDeclaredMethod("reconnect", String.class);
                    reconnect.setAccessible(true);
                    reconnect.invoke(clientChannelManager, "default_tx_group");
                } catch (Exception e) {
                    throw new RuntimeException("reconnect failed!", e);
                }
            }
        });
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.testcontainers.jdbc.ContainerDatabaseDriver");
        config.setJdbcUrl("jdbc:tc:postgresql:16.3-bookworm://test-databases-postgres/demo_ds?TC_INITSCRIPT=seata-script-client-at-postgresql.sql");
        DataSource dataSource = new DataSourceProxy(new HikariDataSource(config));
        TestShardingService testShardingService = new TestShardingService(dataSource);
        testShardingService.processSuccess();
        System.clearProperty(SERVICE_DEFAULT_GROUP_LIST_KEY);
    }
}
