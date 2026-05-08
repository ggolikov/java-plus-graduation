package ewm.event.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventInternalDto {
    private Long id;
    private Long initiatorId;
    private Long categoryId;
    private String annotation;
    private String description;
    private String title;
    private Float lat;
    private Float lon;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private Long confirmedRequests;
    private LocalDateTime eventDate;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private String state;
}
