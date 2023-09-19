package fc.server.palette.chat.entity;

import fc.server.palette.chat.entity.type.ChatRoomType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "chatRoom")
@Builder
public class ChatRoom {
    @Id
    private String id;
    private ChatRoomType type;
    private String title;
    private Long host;
    private String notice;
    private Long contentId;
    private List<Long> memberList;

    public ChatRoom() {
        this.memberList = new ArrayList<>();
    }
}
