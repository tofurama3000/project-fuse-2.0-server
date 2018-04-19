package server.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.user.Friendship;
import server.entities.dto.user.User;

import java.util.List;

public interface FriendRepository extends CrudRepository<Friendship, Long> {
  @Query("FROM Friendship a where (a.receiver = :user or a.sender = :user) and (a.status= " + "'" + "accepted" + "')")
  List<Friendship> getFriends(@Param("user") User user);

  @Query("FROM Friendship a where a.receiver = :user and a.status= " + "'" + "applied" + "'")
  List<Friendship> getFriendApplicant(@Param("user") User user);

  @Query("FROM Friendship a where a.receiver = :user or a.sender = :user")
  List<Friendship> getAllFriends(@Param("user") User user);

  @Query("FROM Friendship f where (f.receiver = :user1 and f.sender = :user2) or (f.receiver = :user2 and f.sender = :user1)")
  List<Friendship> getFriendships(@Param("user1") User user1, @Param("user2") User user2);
}

