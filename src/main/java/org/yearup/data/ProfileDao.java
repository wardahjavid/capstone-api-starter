package org.yearup.data;


import org.yearup.models.Profile;
//This interface defines **what actions can be done with user profiles**.
// It lists methods for creating a profile, getting a profile by user ID,
// and updating profile information, while the database details are handled
// by the implementing class.
public interface ProfileDao {
    Profile create(Profile profile);
    Profile getByUserId(int userId);
    void update(Profile profile);
}
