/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tutorial.todomvc;

import com.vaadin.server.data.Query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Vaadin DataProvider over pure JDBC. Only fixed SQL statements are supported,
 * no custom filtering or sorting.
 *
 * @author Vaadin Ltd
 */
public class SimpleJDBCDataProvider<T> extends AbstractJDBCDataProvider<T> {

    public static final Logger LOGGER = Logger.getLogger(SimpleJDBCDataProvider.class.getName());
    private final PreparedStatement resultSetStatement;
    private final PreparedStatement sizeStatement;

    public SimpleJDBCDataProvider(Connection connection,
            String sqlQuery, DataRetriever<T> jdbcReader) throws SQLException {
        super(connection, jdbcReader);
        resultSetStatement = connection.prepareStatement(sqlQuery,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

        sizeStatement = connection.prepareStatement(
                "select count(*) from (" + sqlQuery + ")",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

    }


    @Override
    protected ResultSet rowCountStatement(Connection connection,
            Query<Void> query) throws SQLException {
        assert query.getSortOrders() == null || query.getSortOrders().isEmpty();
        return sizeStatement.executeQuery();
    }

    @Override
    protected ResultSet resultSetStatement(
            Query<Void> query) throws SQLException {
        assert query.getSortOrders() == null || query.getSortOrders().isEmpty();
        return resultSetStatement.executeQuery();
    }

    @Override
    public void close() throws Exception {
        Stream.of(resultSetStatement, sizeStatement).forEach(statement ->
                {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Prepared statement was closed with error", e);
                    }
                }
        );
    }

}
