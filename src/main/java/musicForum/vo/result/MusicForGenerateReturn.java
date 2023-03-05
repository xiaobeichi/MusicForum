package musicForum.vo.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicForGenerateReturn {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    // 本地url或者只传文件名
    private String savePath;
    private String url;
    private Double duration;
    private String authorName;
    private String audioName;
    private String description;
}
