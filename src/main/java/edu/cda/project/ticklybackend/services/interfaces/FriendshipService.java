package edu.cda.project.ticklybackend.services.interfaces;

import edu.cda.project.ticklybackend.dtos.friendship.FriendsDataResponseDto;
import edu.cda.project.ticklybackend.dtos.friendship.SendFriendRequestDto;
import edu.cda.project.ticklybackend.dtos.friendship.UpdateFriendshipStatusDto;

public interface FriendshipService {

    FriendsDataResponseDto getFriendsData();

    void sendFriendRequest(SendFriendRequestDto requestDto);

    void updateFriendshipStatus(Long friendshipId, UpdateFriendshipStatusDto updateDto);

    void removeFriend(Long friendUserId);
}