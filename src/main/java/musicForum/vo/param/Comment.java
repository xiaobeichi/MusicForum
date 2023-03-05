package musicForum.vo.param;

import lombok.Data;

@Data
public class Comment {
    Long id;
    Long audioId;
    Long parentId;
    String myComment;
}
