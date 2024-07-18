package com.lingh;

import com.lingh.commons.repository.TestShardingService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("resource")
@Testcontainers
public class SeataTest {

    /**
     * TODO Further processing of `/health` awaits <a href="https://github.com/apache/incubator-seata/pull/6356">apache/incubator-seata#6356</a>.
     */
    @Container
    public GenericContainer<?> seataContainer = new GenericContainer<>("seataio/seata-server:1.8.0")
            .withExposedPorts(7091, 8091)
            .waitingFor(Wait.forHttp("/health").forPort(7091).forStatusCode(401));

    private static final String SERVICE_DEFAULT_GROUP_LIST_KEY = "service.default.grouplist";

    @Test
    void assertSeataTransactions() throws SQLException {
        assertThat(System.getProperty(SERVICE_DEFAULT_GROUP_LIST_KEY), is(nullValue()));
        System.setProperty(SERVICE_DEFAULT_GROUP_LIST_KEY, "127.0.0.1:" + seataContainer.getMappedPort(8091));
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.testcontainers.jdbc.ContainerDatabaseDriver");
        config.setJdbcUrl("jdbc:tc:postgresql:16.3-bookworm://test-databases-postgres/demo_ds");
        DataSource dataSource = new HikariDataSource(config);
        TestShardingService testShardingService = new TestShardingService(dataSource);
        testShardingService.getAddressRepository().createTableIfNotExistsInMySQL();
        testShardingService.getAddressRepository().truncateTable();
        testShardingService.processSuccess();
        testShardingService.cleanEnvironment();
        System.clearProperty(SERVICE_DEFAULT_GROUP_LIST_KEY);
    }
}
