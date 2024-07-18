package com.lingh.commons.repository;

import com.lingh.commons.entity.Address;
import io.seata.core.exception.TransactionException;

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

    public void processSuccess() throws SQLException, TransactionException {
        addressRepository.createTableIfNotExists();
        addressRepository.truncateTable();
        for (int i = 1; i <= 10; i++) {
            addressRepository.insert(new Address((long) i, "address_test_" + i));
        }
        assertThat(addressRepository.selectAll(),
                equalTo(LongStream.range(1, 11).mapToObj(i -> new Address(i, "address_test_" + i)).collect(Collectors.toList())));
        for (long i = 1; i <= 10; i++) {
            addressRepository.delete(i);
        }
        assertThat(addressRepository.selectAll(), equalTo(new ArrayList<>()));
        addressRepository.assertRollbackWithTransactions();
        addressRepository.dropTable();
    }
}
