package com.courses.management.user;

import com.courses.management.common.exceptions.SQLCourseException;
import com.courses.management.common.exceptions.SQLUserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDAOImpl implements UserDAO {

    private DataSource dataSource;
    private final static Logger LOG = LogManager.getLogger(UserDAOImpl.class);

    private final static String INSERT = "INSERT INTO users(first_name, last_name, email, user_role, status) " +
            "VALUES (?, ?, ?, ?, ?);";
    private final static String UPDATE = "UPDATE users SET first_name=?, last_name=?, email=?, user_role=?, " +
            "status=?, course_id=? WHERE id=?;";
    private static final String DELETE = "DELETE FROM users WHERE id=?;";
    private static final String GET_BY_ID = "SELECT id, first_name, last_name, email, user_role, status " +
            "FROM users WHERE id=?;";
    private final static String GET_BY_EMAIL = "SELECT id, first_name, last_name, email, user_role, status " +
            "FROM users WHERE email=?;";
    private static final String GET_ALL = "SELECT id, first_name, last_name, email, user_role, status " +
            "FROM users;";
    private static final String GET_USERS_BY_COURSE_TITLE = "SELECT u.id, u.first_name, u.last_name, u.email, u.user_role, u.status " +
            "FROM users u " +
            "INNER JOIN course c ON c.id=u.course_id " +
            "WHERE c.title=?;";
    private static final String GET_ALL_USERS_BY_STATUS = "SELECT id, first_name, last_name, email, user_role, status " +
            "FROM users WHERE status=?;";

    public UserDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void create(User user) {

        LOG.debug(String.format("create: user.first_name=%s " +
                "user.last_name=%s" +
                "user.email=%s", user.getFirstName(), user.getLastName(), user.getEmail()));
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(INSERT)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getUserRole().name());
            statement.setString(5, user.getStatus().name());
            statement.execute();
        } catch (SQLException e) {
            LOG.error(String.format("create: user.email=%s", user.getEmail()), e);
            throw new SQLUserException("Error occurred when creating user");
        }

    }

    @Override
    public void update(User user) {

        LOG.debug(String.format("update: user.first_name=%s " +
                "user.last_name=%s" +
                "user.email=%s", user.getFirstName(), user.getLastName(), user.getEmail()));
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(UPDATE)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getUserRole().name());
            statement.setString(5, user.getStatus().name());
            if (Objects.isNull(user.getCourse())){
                statement.setNull(6, Types.NULL);
            } else
                statement.setInt(6, user.getCourse().getId());
            statement.setInt(7, user.getId());
            statement.execute();
        } catch (SQLException e) {
            LOG.error(String.format("update: user.email=%s", user.getEmail()), e);
            throw new SQLUserException("Error occurred when updating user");
        }
    }

    @Override
    public void delete(int id) {

        LOG.debug(String.format("delete: user.id=%s ", id));
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(DELETE)) {
            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            LOG.error(String.format("delete: user.id=%s", id), e);
            throw new SQLUserException("Error occurred when removing user");
        }
    }

    @Override
    public User get(int id) {

        LOG.debug(String.format("get: user.id=%s ", id));
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(GET_BY_ID)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            return getUser(resultSet);
        } catch (SQLException e) {
            LOG.error(String.format("get: user.id=%s", id), e);
            throw new SQLUserException("Error occurred when retrieving user");
        }
    }

    @Override
    public List<User> getAll() {

        LOG.debug("getAll: ");
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(GET_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            return getUserList(resultSet);
        } catch (SQLException e) {
            LOG.error("getAll: ", e);
            throw new SQLUserException("Error occurred when retrieving all user");
        }
    }

    @Override
    public User get(String email) {

        LOG.debug(String.format("get: user.email=%s ", email));
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(GET_BY_EMAIL)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            return getUser(resultSet);
        } catch (SQLException e) {
            LOG.error(String.format("get: user.email=%s", email), e);
            throw new SQLUserException("Error occurred when retrieving user");
        }
    }

    @Override
    public List<User> getUsersByCourse(String courseTitle) {

        LOG.debug(String.format("getUsersByCourse: course.title=%s", courseTitle));
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(GET_USERS_BY_COURSE_TITLE)) {
            statement.setString(1, courseTitle);
            ResultSet resultSet = statement.executeQuery();
            return getUserList(resultSet);
        } catch (SQLException e) {
            LOG.error(String.format("getUsersByCourse: course.title=%s", courseTitle), e);
            throw new SQLUserException("Error occurred when retrieving users by course title");
        }
    }

    @Override
    public List<User> getAllByStatus(UserStatus userStatus) {

        LOG.debug(String.format("getAllByStatus: user.status=%s", userStatus.name()));
        try (PreparedStatement statement = dataSource.getConnection().prepareStatement(GET_ALL_USERS_BY_STATUS)) {
            statement.setString(1, userStatus.name());
            return getUserList(statement.executeQuery());
        } catch (SQLException e) {
            LOG.error(String.format("getAllByStatus: user.status=%s", userStatus.name()), e);
            throw new SQLUserException("Error occurred when retrieving users by status");
        }
    }

    private User getUser(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return mapUserFromRS(resultSet);
        }
        return null;
    }

    private List<User> getUserList(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(mapUserFromRS(rs));
        }
        return users;
    }

    private User mapUserFromRS(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        user.setUserRole(UserRole.getUserRole(resultSet.getString("user_role")).get());
        user.setStatus(UserStatus.getUserStatus(resultSet.getString("status")).get());
        return user;
    }

}
