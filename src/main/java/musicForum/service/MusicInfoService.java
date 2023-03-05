package musicForum.service;

import musicForum.bean.musicUpload;
import musicForum.utils.Result;
import musicForum.vo.result.MusicDetailReturn;

public interface MusicInfoService {
    Result getMusicDetail(Long id,Long userId);
    MusicDetailReturn musicUploadToVo(musicUpload musicUpload);
    Result getMyUpload(Long userId);
    Result getMyLike(Long userId);
    Result getMyFavorite(Long userId);
    Result getInfoForGenerate();
}
