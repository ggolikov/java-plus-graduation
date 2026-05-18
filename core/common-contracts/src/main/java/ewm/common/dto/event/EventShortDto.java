package ewm.common.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private Long category;
    private Long confirmedRequests;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Long initiator;
    private Boolean paid;
    private Double rating;
}