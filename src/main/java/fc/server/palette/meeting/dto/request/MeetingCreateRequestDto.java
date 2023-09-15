package fc.server.palette.meeting.dto.request;

import com.fasterxml.jackson.annotation.*;
import fc.server.palette.member.entity.Member;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class MeetingCreateRequestDto {
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Member member;

    private Long id;
    private String category;
    private String type;
    private List<String> jobs;
    private List<String> positions;
    private String sex;
    private List<String> ageRange;
   // private List<Media> image;
    private String title;
    private String description;
    private int headCount;
    private Date startDate;
    private Date endDate;
    private boolean onOff;
    private String place;

    private String week;

    private List<String> days;
    private String time;
    private String progressTime;

    private String acceptType;
}
