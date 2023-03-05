package musicForum.vo.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicSavaReturn {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long audioId;
    private String audioUrl;
    private String audioImg;
    private Date audioTime;
    private Double duration;
}
