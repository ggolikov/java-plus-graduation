package ewm.common.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import ewm.common.dto.LocationDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventFullDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private Long category;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Long initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String state;
    private Long confirmedRequests;
    private Double rating;
}