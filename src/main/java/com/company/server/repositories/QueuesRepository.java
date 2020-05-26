package com.company.server.repositories;

import com.company.server.models.Queue;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Component
public class QueuesRepository {
    private JdbcTemplate jdbcTemplate;
    //language=SQL
    private final String SQL_FIND_BY_NAME = "SELECT * FROM queues WHERE queue=?";
    //language=SQL
    private final String SQL_INSERT_QUEUE = "INSERT INTO queues (queue, has_consumer) VALUES (?,?)";
    //language=SQL
    private final String SQL_UPDATE_QUEUE = "UPDATE queues SET has_consumer=? WHERE queue=?";
    
    private final RowMapper<Queue> queueRowMapper = (row, rowNumber) -> {
        String queue = row.getString("queue");
        boolean hasConsumer = row.getBoolean("has_consumer");
        
        return new Queue(queue, hasConsumer);
    };

    public QueuesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void update(String queue, boolean hasConsumer) {
        jdbcTemplate.update(SQL_UPDATE_QUEUE, queue, hasConsumer);
    }

    public void insert(Queue queue) {
        int updRows = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection
                    .prepareStatement(SQL_INSERT_QUEUE);
            statement.setString(1, queue.getQueue());
            statement.setBoolean(2, queue.getHasConsumer());
            return statement;
        });

        if (updRows == 0) {
            throw new IllegalArgumentException();
        }
    }

    public Optional<Queue> findByName(String queue) {
        Queue queueModel = jdbcTemplate.queryForObject(SQL_FIND_BY_NAME, new Object[]{queue}, queueRowMapper);
        return Optional.ofNullable(queueModel);
    }
}