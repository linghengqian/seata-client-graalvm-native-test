package com.lingh.commons.repository;

import com.lingh.commons.entity.Address;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public final class TestShardingService {

    private final AddressRepository addressRepository;

    public TestShardingService(final DataSource dataSource) {
        addressRepository = new AddressRepository(dataSource);
    }

    public AddressRepository getAddressRepository() {
        return addressRepository;
    }

    /**
     * Process success.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void processSuccess() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            Address address = new Address((long) i, "address_test_" + i);
            addressRepository.insert(address);
        }
        assertThat(addressRepository.selectAll(),
                equalTo(LongStream.range(1, 11).mapToObj(i -> new Address(i, "address_test_" + i)).collect(Collectors.toList())));
        for (long i = 1; i <= 10; i++) {
            addressRepository.delete(i);
        }
        assertThat(addressRepository.selectAll(), equalTo(new ArrayList<>()));
        addressRepository.assertRollbackWithTransactions();
    }

    /**
     * Clean environment.
     */
    public void cleanEnvironment() throws SQLException {
        addressRepository.dropTable();
    }
}