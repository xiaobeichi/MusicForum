package musicForum.service;

import musicForum.utils.Result;
import musicForum.vo.param.MusicGenerateParam;
import org.bytedeco.opencv.presets.opencv_core;

import java.io.IOException;
import java.util.List;

public interface MusicUpdateService {
    Result comment(Long id,Long audioId,Long parentId, String myComment);
    Result like(Long userId,Long audioId);
    Result notLike(Long userId,Long audioId);
    Result favorite(Long userId,Long audioId);
    Result notFavorite(Long userId,Long audioId);
    Result uploadMusic(Long id, String name, String description, Integer type,
                       Integer permission, String photoPath,String savePath);
    Result generateMusic(List<List<MusicGenerateParam>> musicGenerateParams) throws IOException;
}
