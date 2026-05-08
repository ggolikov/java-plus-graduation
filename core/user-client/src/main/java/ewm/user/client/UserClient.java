package ewm.user.client;

import ewm.common.dto.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@FeignClient(
        contextId = "userFeign",
        name = "user-service",
        url = "${user.service.url:http://localhost:8080}"
)
public interface UserClient {

    @GetMapping("/admin/users")
    List<UserDto> getUsers(
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    );

    default Optional<UserDto> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        List<UserDto> users = getUsers(List.of(userId), 0, 1);
        if (users == null || users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.getFirst());
    }

    default boolean existsById(Long userId) {
        return findById(userId).isPresent();
    }

    default List<UserDto> findByUserIdIn(List<Long> ids, Pageable pageable) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        int from = pageable == null ? 0 : pageable.getPageNumber() * pageable.getPageSize();
        int size = pageable == null ? ids.size() : pageable.getPageSize();
        List<UserDto> users = getUsers(ids, from, size);
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream().toList();
    }
}
