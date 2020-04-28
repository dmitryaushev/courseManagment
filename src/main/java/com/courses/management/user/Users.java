package com.courses.management.user;

import com.courses.management.common.View;
import com.courses.management.common.commands.util.InputString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Users {

    private static final Logger LOG = LogManager.getLogger(Users.class);
    private static String EMAIL_REGEXP = "^[-a-z0-9!#$%&'*+/=?^_`{|}~]+(?:\\.[-a-z0-9!#$%&'*+/=?^_`{|}~]+)*@(?:[a-z0-9](" +
            "[-a-z0-9]{0,61}[a-z0-9])?\\.)*(?:aero|arpa|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|" +
            "net|org|pro|tel|travel|[a-z][a-z])$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEXP);
    private static final int FIRST_NAME_INDEX = 1;
    private static final int LAST_NAME_INDEX = 2;
    private static final int EMAIL_INDEX = 3;

    private UserDAO userDAO;
    private UserRepository userRepository;

    public Users(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static void validateEmail(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            LOG.warn(String.format("validateEmail: email %s is incorrect", email));
            throw new IllegalArgumentException(String.format("Wrong email address %s", email));
        }
    }

    public static User mapUser(InputString inputString) {

        String[] parameters = inputString.getParameters();
        String email = parameters[EMAIL_INDEX];
        validateEmail(email);

        User user = new User();
        user.setFirstName(parameters[FIRST_NAME_INDEX]);
        user.setLastName(parameters[LAST_NAME_INDEX]);
        user.setEmail(email);
        user.setUserRole(UserRole.NEWCOMER);
        user.setStatus(UserStatus.NOT_ACTIVE);
        return user;
    }

    public static void printUser(View view, User user) {

        view.write("User:");
        view.write(String.format("\t first name = %s", user.getFirstName()));
        view.write(String.format("\t last name = %s", user.getLastName()));
        view.write(String.format("\t email = %s", user.getEmail()));
        view.write(String.format("\t user role = %s", user.getUserRole().getRole()));
        view.write(String.format("\t user status = %s", user.getStatus().getStatus()));
    }

    public User getById(int id) {
        return userRepository.findById(id).orElse(new User());
    }

    public User getByEmail(String email) {
        return userRepository.getByEmail(email);
    }
}
