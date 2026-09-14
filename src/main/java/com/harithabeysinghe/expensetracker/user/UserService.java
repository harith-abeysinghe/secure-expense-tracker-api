package com.harithabeysinghe.expensetracker.user;

import com.harithabeysinghe.expensetracker.common.dto.PageResponse;
import com.harithabeysinghe.expensetracker.common.error.ConflictException;
import com.harithabeysinghe.expensetracker.common.error.NotFoundException;
import com.harithabeysinghe.expensetracker.user.dto.UserResponse;
import com.harithabeysinghe.expensetracker.user.entity.UserStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) { this.users = users; }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) { return map(require(id)); }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(int page, int size) {
        var request = PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 100), Sort.by("createdAt").descending());
        return PageResponse.from(users.findAll(request), this::map);
    }

    @Transactional
    public UserResponse updateStatus(UUID actorId, UUID userId, UserStatus status) {
        if (actorId.equals(userId) && status == UserStatus.DISABLED) {
            throw new ConflictException("Administrators cannot disable their own account");
        }
        var user = require(userId);
        user.setStatus(status);
        return map(user);
    }

    public com.harithabeysinghe.expensetracker.user.entity.UserEntity require(UUID id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserResponse map(com.harithabeysinghe.expensetracker.user.entity.UserEntity user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getCurrency(),
                user.getRole(), user.getStatus(), user.getCreatedAt());
    }
}

