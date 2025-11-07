package dao;

import java.util.Optional;

public interface UserDao {
	 Optional<UserRecord> authenticate(String username, String password);

	 record UserRecord(int id, String username) {}
}
