package musicForum.vo.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicGenerateParam {
    private Long id;
    private String savePath;
    private Double startTime;
    private Double duration;
}
