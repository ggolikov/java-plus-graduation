package ewm.user.mapper;

import ewm.user.dto.NewUserRequest;
import ewm.common.dto.user.UserDto;
import ewm.common.dto.user.UserShortDto;
import ewm.user.model.User;

public class UserMapper {

    public static User toEntity(NewUserRequest dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        return user;
    }

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        return dto;
    }

    public static UserShortDto toShortDto(User user) {
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getUserId());
        dto.setName(user.getName());
        return dto;
    }
}