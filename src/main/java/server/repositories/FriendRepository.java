package server.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.Notification;
import server.entities.dto.user.Friend;
import server.entities.dto.user.User;

import java.util.List;

public interface FriendRepository extends CrudRepository<Friend, Long> {
  @Query("FROM Friend a where (a.receiver = :user or a.sender = :user) and (a.status= " + "'" + "accepted" + "')")
  List<Friend> getFriends(@Param("user") User user);

  @Query("FROM Friend a where a.receiver = :user and a.status= " + "'" + "applied" + "'")
  List<Friend> getFriendApplicant(@Param("user") User user);

  @Query("FROM Friend a where a.receiver = :user or a.sender = :user")
  List<Friend> getAllFriends(@Param("user") User user);

}

