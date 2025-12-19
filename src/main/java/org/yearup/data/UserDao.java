package org.yearup.data;

import org.yearup.models.User;

import java.util.List;
//This interface defines what actions can be done with users in the system.
// It lists methods for creating users, finding users by ID or username, checking
// if a user exists, and getting all users, while the database logic is handled by
// the implementing class.
public interface UserDao {

    List<User> getAll();

    User getUserById(int userId);

    User getByUserName(String username);

    int getIdByUsername(String username);

    User create(User user);

    boolean exists(String username);
}
