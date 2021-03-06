package com.courses.management.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LogManager.getLogger(UserServiceImpl.class);

    private UserRepository userRepository;
    private BCryptPasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setEncoder(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public User getUser(int id) {
        LOG.debug(String.format("getUser: id=%d", id));
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotExistException(String.format("User with id %s not found", id)));
    }

    @Override
    public User getUser(String email) {
        LOG.debug(String.format("getUser: email=%s", email));
        return userRepository.getByEmail(email)
                .orElseThrow(() -> new UserNotExistException(String.format("User with email %s not exist", email)));
    }

    @Override
    public List<User> getAllUsers() {
        LOG.debug("getAllUsers: ");
        return userRepository.findAll();
    }

    @Override
    public void registerUser(User user) {

        if (emailExist(user.getEmail())) {
            throw new UserAlreadyExistsException(
                    String.format("There is an account with that email address: %s", user.getEmail()));
        }

        user.setUserRole(UserRole.ROLE_NEWCOMER);
        user.setPassword(encoder.encode(user.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    private boolean emailExist(String email) {
        return userRepository.getByEmail(email).isPresent();
    }
}
