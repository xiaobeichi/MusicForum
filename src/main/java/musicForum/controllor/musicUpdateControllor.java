package musicForum.controllor;

import musicForum.service.MusicUpdateService;
import musicForum.utils.ErrorCode;
import musicForum.utils.Result;
import musicForum.utils.qiniuUtils;
import musicForum.vo.param.Comment;
import musicForum.vo.param.MusicGenerateParam;
import musicForum.vo.param.UserAndMusic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("music/musicUpdate")
public class musicUpdateControllor {
    @Autowired
    MusicUpdateService musicUpdateService;
    @Value("${music.musicImg-parent-path}")
    private String musicImgParentPath;
    @Value("${music.local-storage-parent-path}")
    private String musicLocalStorageParentPath;
    @Value("${music.remote-storage-parent-path}")
    private String musicRemoteStorageParentPath;

    //评论
    @PostMapping("comment")
    public Result comment(Comment myCom){
        //id，音频链接（远程），图片链接，发布时间，音频名称，发布者id，点赞数，评论内容
        return musicUpdateService.comment(myCom.getId(),myCom.getAudioId(), myCom.getParentId(),
                myCom.getMyComment());
    }
    //点赞
    @PostMapping("like")
    public Result like( UserAndMusic p){
        //记得更新我的点赞状态，不要重复点赞
        return musicUpdateService.like(p.getId(),p.getAudioId());
    }
    //取消点赞
    @PostMapping("notlike")
    public Result notLike(UserAndMusic p){
        //记得更新我的点赞状态，不要重复点赞
        return musicUpdateService.notLike(p.getId(),p.getAudioId());
    }
    //收藏
    @PostMapping("favorite")
    public Result favorite(UserAndMusic p){
        //记得更新我的点赞状态，不要重复点赞
        return musicUpdateService.favorite(p.getId(),p.getAudioId());
    }
    //取消收藏
    @PostMapping("notfavorite")
    public Result notFavorite(UserAndMusic p){
        //记得更新我的点赞状态，不要重复点赞
        return musicUpdateService.notFavorite(p.getId(),p.getAudioId());
    }

    /**
     * 上传音频
     * 对生成完音频的上传应该使用url直存
     * @param id
     * @param name
     * @param description
     * @param type
     * @param permission
     * @param photo
     * @param music
     * @return
     */
    @PostMapping("uploadMusic")
    public Result uploadMusic(Long id, String name, String description,  Integer type,
                              Integer permission,  MultipartFile photo,
                              MultipartFile music, String generatePath) {
        //记得更新我的点赞状态，不要重复点赞
        //原始文件名称
        String originalFilename = photo.getOriginalFilename();
        String photoPath = UUID.randomUUID().toString() + "."
                + StringUtils.substringAfter(originalFilename, ".");
        //上传到七牛云服务器
        boolean upload = qiniuUtils.upload(photo, musicImgParentPath+photoPath);
        if (!upload){
            return Result.fail(ErrorCode.UPLOAD_MUSIC_PHOTO_ERROR.getCode(),
                    ErrorCode.UPLOAD_MUSIC_PHOTO_ERROR.getMsg());
        }
//        FileOutputStream bos1 = null;
//        try {
//            bos1 = new FileOutputStream(musicImgParentPath+photoPath);
//            bos1.write(photo.getBytes());
//            bos1.flush();
//            bos1.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return Result.fail(ErrorCode.UPLOAD_MUSIC_ERROR.getCode(),
//                    ErrorCode.UPLOAD_MUSIC_ERROR.getMsg());
//        }

        //如果传了generatePath则直接调用上传数据库
        if(generatePath!=null){
            return musicUpdateService.uploadMusic(id,name,description,type,
                    permission,photoPath,StringUtils.substringAfter(generatePath, "/"));
        }

        String originalFilename2 = music.getOriginalFilename();
        String savePath = UUID.randomUUID().toString() + "."
                + StringUtils.substringAfter(originalFilename2, ".");
        //上传到七牛云服务器
        upload = qiniuUtils.upload(music, musicRemoteStorageParentPath+savePath);
        if (!upload){
            return Result.fail(ErrorCode.UPLOAD_MUSIC_ERROR.getCode(),
                    ErrorCode.UPLOAD_MUSIC_ERROR.getMsg());
        }
        //如果是短视频，上传到阿里云服务器
        FileOutputStream bos = null;
        try {
            bos = new FileOutputStream(musicLocalStorageParentPath+savePath);
            bos.write(music.getBytes());
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail(ErrorCode.UPLOAD_MUSIC_ERROR.getCode(),
                    ErrorCode.UPLOAD_MUSIC_ERROR.getMsg());
        }
        return musicUpdateService.uploadMusic(id,name,description,type,
                permission,photoPath,savePath);
    }

    /**
     * 合并生成音频
     * 看看有什么信息。尽量不要数据库再次查一遍
     * @param musicGenerateParams
     * @return
     */
    @PostMapping("generateMusic")
    public Result generateMusic(@RequestBody List<List<MusicGenerateParam>> musicGenerateParams) throws IOException {
        //看看有什么信息。尽量不要数据库再次查一遍
        return musicUpdateService.generateMusic(musicGenerateParams);
    }
    //删除暂存音频
}
