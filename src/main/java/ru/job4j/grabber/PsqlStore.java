package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cnn = DriverManager.getConnection(cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"),
                    cfg.getProperty("jdbc.password"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement preparedStatement = cnn.prepareStatement(
                "INSERT INTO post(title, link, description, created) VALUES(?,?,?,?)"
                        + " ON CONFLICT ON CONSTRAINT link_unique"
                        + " DO NOTHING;",
                Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getLink());
            preparedStatement.setString(3, post.getDescription());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));

            preparedStatement.execute();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (Statement statement = cnn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM post;");
            while (resultSet.next()) {
                Post post = new Post(resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getTimestamp(5).toLocalDateTime());
                post.setId(resultSet.getInt(1));
                list.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Post findById(int id) {
        try (Statement statement = cnn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(String.format(
                    "SELECT * FROM post WHERE id = %s", id));
            if (resultSet.next()) {
                Post post = new Post(resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4),
                resultSet.getTimestamp(5).toLocalDateTime());
                post.setId(resultSet.getInt(1));
                return post;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("app.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Store psqlStore = new PsqlStore(config);

        Post post1 = new Post("testTitle1", "http://link1", "testDescription1", LocalDateTime.now());
        psqlStore.save(post1);

        System.out.println(psqlStore.getAll());

        Post post2 = new Post("testTitle2", "http://link2", "testDescription2", LocalDateTime.now());
        psqlStore.save(post2);

        Post result = post1.getId() == 0 ? null : psqlStore.findById(post1.getId());
        System.out.println(result);
    }
}
