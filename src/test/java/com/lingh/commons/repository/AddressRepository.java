package com.lingh.commons.repository;

import com.lingh.commons.entity.Address;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "UnusedReturnValue"})
public final class AddressRepository {

    private final DataSource dataSource;

    public AddressRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * create table t_address if not exists in MySQL.
     *
     * @throws SQLException SQL exception
     */
    public void createTableIfNotExistsInMySQL() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    /**
     * drop table t_address.
     *
     * @throws SQLException SQL exception
     */
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_address";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    /**
     * truncate table t_address.
     *
     * @throws SQLException SQL exception
     */
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_address";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    /**
     * insert something to table t_address.
     *
     * @param address address
     * @return addressId of the insert statement
     * @throws SQLException SQL exception
     */
    public Long insert(final Address address) throws SQLException {
        String sql = "INSERT INTO t_address (address_id, address_name) VALUES (?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, address.addressId());
            preparedStatement.setString(2, address.addressName());
            preparedStatement.executeUpdate();
        }
        return address.addressId();
    }

    /**
     * delete by id.
     *
     * @param id id
     * @throws SQLException SQL exception
     */
    public void delete(final Long id) throws SQLException {
        String sql = "DELETE FROM t_address WHERE address_id=?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * select all.
     *
     * @return list of address
     * @throws SQLException SQL exception
     */
    public List<Address> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_address";
        List<Address> result = new LinkedList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Address address = new Address(resultSet.getLong(1), resultSet.getString(2));
                result.add(address);
            }
        }
        return result;
    }

    /**
     * Assert rollback with transactions.
     * This is currently just a simple test against a non-existent table and does not involve the competition scenario of distributed transactions.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void assertRollbackWithTransactions() throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            connection.createStatement().executeUpdate("INSERT INTO t_address (address_id, address_name) VALUES (2024, 'address_test_2024')");
            connection.createStatement().executeUpdate("INSERT INTO t_table_does_not_exist (test_id_does_not_exist) VALUES (2024)");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
        try (
                Connection conn = dataSource.getConnection();
                ResultSet resultSet = conn.createStatement().executeQuery("SELECT * FROM t_address WHERE address_id = 2024")) {
            assertThat(resultSet.next(), is(false));
        }
    }
}