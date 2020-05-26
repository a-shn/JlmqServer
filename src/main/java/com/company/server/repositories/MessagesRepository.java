package com.company.server.repositories;

import com.company.server.models.Message;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class MessagesRepository {
    private JdbcTemplate jdbcTemplate;
    //language=SQL
    private final String SQL_SAVE_MESSAGE = "INSERT INTO message_queue (queue, message, status, time) VALUES (?,?,?,?)";
    //language=SQL
    private final String SQL_LAST_UNPROCESSED_MESSAGE = "SELECT * FROM message_queue WHERE queue=? AND (status='RECEIVED' OR status='ACKNOWLEDGED')";
    //language=SQL
    private final String SQL_CHANGE_STATUS = "UPDATE message_queue SET status=? WHERE id=?";
    //language=SQL
    private final String SQL_FIND_BY_ID = "SELECT * FROM message_queue WHERE id=?";

    public MessagesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<Message> messageRowMapper = (row, rowNumber) -> {
        Long id = row.getLong("id");
        String queue = row.getString("queue");
        String message = row.getString("message");
        Message.Status status = Message.Status.valueOf(row.getString("status"));
        LocalDateTime time = row.getTimestamp("time").toLocalDateTime();

        return new Message(id, queue, message, time, status);
    };

    public Message save(Message message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updRows = jdbcTemplate.update((connection) -> {
            PreparedStatement statement = connection.prepareStatement(SQL_SAVE_MESSAGE, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, message.getQueue());
            statement.setString(2, message.getMessage());
            statement.setString(3, message.getStatus().toString());
            statement.setTimestamp(4, Timestamp.valueOf(message.getTime()));
            return statement;
        }, keyHolder);

        if (updRows == 0) {
            throw new IllegalArgumentException();
        }
        message.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return message;
    }

    public Optional<Message> findById(long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(SQL_FIND_BY_ID, new Object[]{id}, messageRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Message> getFirstUnprocessedMessage(String queue) {
        return jdbcTemplate.query(SQL_LAST_UNPROCESSED_MESSAGE, new Object[]{queue}, messageRowMapper);
    }

    public void changeStatus(long id, Message.Status status) {
        jdbcTemplate.update(SQL_CHANGE_STATUS, status.toString(), id);
    }
}
