package musicForum.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import musicForum.bean.musicComment;
import musicForum.bean.musicFavorite;
import musicForum.bean.musicLike;
import musicForum.bean.musicUpload;
import musicForum.mapper.MusicCommentMapper;
import musicForum.mapper.MusicFavoriteMapper;
import musicForum.mapper.MusicLikeMapper;
import musicForum.mapper.MusicUploadMapper;
import musicForum.service.MusicInfoService;
import musicForum.service.MusicUpdateService;
import musicForum.utils.ErrorCode;
import musicForum.utils.MusicUtils;
import musicForum.utils.Result;
import musicForum.utils.qiniuUtils;
import musicForum.vo.param.MusicGenerateParam;
import musicForum.vo.result.MusicGenerateReturn;
import musicForum.vo.result.MusicSavaReturn;
import org.bytedeco.opencv.presets.opencv_core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class MusicUpdateServiceImple implements MusicUpdateService {
    @Autowired
    private MusicUploadMapper musicUploadMapper;
    @Autowired
    private MusicFavoriteMapper musicFavoriteMapper;
    @Autowired
    private MusicLikeMapper musicLikeMapper;
    @Autowired
    private MusicCommentMapper musicCommentMapper;

    @Value("${music.musicImg-parent-path}")
    private String musicImgParentPath;
    @Value("${music.local-storage-parent-path}")
    private String musicLocalStorageParentPath;
    @Value("${music.remote-storage-parent-path}")
    private String musicRemoteStorageParentPath;

    public Result comment(Long userId, Long audioId, Long parentId, String myComment){
        musicComment comment = new musicComment();
        comment.setCommentTime(new Date());
        comment.setContent(myComment);
        comment.setMusicId(audioId);
        comment.setUserId(userId);
        if(parentId==null){
            comment.setLevel(1);
        }else {
            comment.setLevel(2);
            comment.setParentId(parentId);
            musicComment parentComment = musicCommentMapper.selectById(parentId);
            if(parentComment!=null){
                comment.setToUserId(parentComment.getUserId());
            }
        }
        musicCommentMapper.insert(comment);
        //???????????????id??????
        Map<String, Long> result = new HashMap<>();
        result.put("commentId",comment.getId());
        return Result.success(result);
    }

    public Result like(Long userId,Long audioId){
        // ????????????????????????music???user???????????????????????????????????????????????????
        QueryWrapper<musicLike> musicLikeQueryWrapper = new QueryWrapper<>();
        musicLikeQueryWrapper.eq("user_id",userId).eq("music_id",audioId);
        musicLike like = musicLikeMapper.selectOne(musicLikeQueryWrapper);
        if(like==null){
            musicLike musicLike = new musicLike();
            musicLike.setMusicId(audioId);
            musicLike.setUserId(userId);
            musicLike.setLikeTime(new Date());
            musicLikeMapper.insert(musicLike);
            return Result.success(null);
        }else {
            return Result.fail(ErrorCode.MUSIC_ALREADY_LIKE.getCode()
                    , ErrorCode.MUSIC_ALREADY_LIKE.getMsg());
        }
    }

    public Result notLike(Long userId,Long audioId){
        // ????????????????????????music???user???????????????????????????????????????????????????
        QueryWrapper<musicLike> musicLikeQueryWrapper = new QueryWrapper<>();
        musicLikeQueryWrapper.eq("user_id",userId).eq("music_id",audioId);
        musicLike like = musicLikeMapper.selectOne(musicLikeQueryWrapper);
        if(like!=null){
            musicLikeMapper.delete(musicLikeQueryWrapper);
            return Result.success(null);
        }else {
            return Result.fail(ErrorCode.MUSIC_ALREADY_NOTLIKE.getCode()
                    , ErrorCode.MUSIC_ALREADY_NOTLIKE.getMsg());
        }
    }

    public Result favorite(Long userId,Long audioId){
        // ????????????????????????music???user???????????????????????????????????????????????????
        QueryWrapper<musicFavorite> musicFavQueryWrapper = new QueryWrapper<>();
        musicFavQueryWrapper.eq("user_id",userId).eq("music_id",audioId);
        musicFavorite favorite = musicFavoriteMapper.selectOne(musicFavQueryWrapper);
        if(favorite==null){
            musicFavorite musicFavorite = new musicFavorite();
            musicFavorite.setMusicId(audioId);
            musicFavorite.setUserId(userId);
            musicFavorite.setFavTime(new Date());
            musicFavoriteMapper.insert(musicFavorite);
            return Result.success(null);
        }else {
            return Result.fail(ErrorCode.MUSIC_ALREADY_FAV.getCode()
                    , ErrorCode.MUSIC_ALREADY_FAV.getMsg());
        }
    }

    public Result notFavorite(Long userId,Long audioId){
        QueryWrapper<musicFavorite> musicFavQueryWrapper = new QueryWrapper<>();
        musicFavQueryWrapper.eq("user_id",userId).eq("music_id",audioId);
        musicFavorite favorite = musicFavoriteMapper.selectOne(musicFavQueryWrapper);
        if(favorite!=null){
            musicFavoriteMapper.delete(musicFavQueryWrapper);
            return Result.success(null);
        }else {
            return Result.fail(ErrorCode.MUSIC_ALREADY_NOTFAV.getCode()
                    , ErrorCode.MUSIC_ALREADY_NOTFAV.getMsg());
        }
    }

    public Result uploadMusic(Long id, String name, String description, Integer type,
                       Integer permission, String photoPath,String savePath){
        musicUpload musicUpload = new musicUpload();
        musicUpload.setDescription(description);
        musicUpload.setUpUserId(id);
        musicUpload.setName(name);
        musicUpload.setPermission(permission);
        musicUpload.setPhotoPath(photoPath);
        musicUpload.setSavePath(savePath);
        musicUpload.setType(type);
        musicUpload.setUpTime(new Date());
        //??????????????????
        musicUpload.setDuration(MusicUtils.getMp3Duration(musicLocalStorageParentPath+savePath));
        musicUploadMapper.insert(musicUpload);

        MusicSavaReturn musicSavaReturn = new MusicSavaReturn();
        musicSavaReturn.setDuration(musicUpload.getDuration());
        musicSavaReturn.setAudioId(musicUpload.getId());
        musicSavaReturn.setAudioTime(musicUpload.getUpTime());
        musicSavaReturn.setAudioImg(musicImgParentPath+photoPath);
        musicSavaReturn.setAudioUrl(qiniuUtils.url+musicRemoteStorageParentPath+savePath);
        //return
        return Result.success(musicSavaReturn);
    }

    public Result generateMusic(List<List<MusicGenerateParam>> musicGenerateParamLists) throws IOException {
        //???????????????????????????
        Double maxTime = 0d;
        for (List<MusicGenerateParam> musicGenerateParams : musicGenerateParamLists) {
            if(musicGenerateParams.size()>0){
                MusicGenerateParam musicGenerateParam = musicGenerateParams.get(musicGenerateParams.size() - 1);
                double time = musicGenerateParam.getStartTime() + musicGenerateParam.getDuration();
                if (time > maxTime) maxTime = time;
            }
        }
        String concatTemPath = musicLocalStorageParentPath+UUID.randomUUID();
        Path path = Paths.get(concatTemPath);
        Files.createDirectories(path);

        Path path2 = Paths.get(concatTemPath+"/tem");
        Files.createDirectories(path2);

        ArrayList<String> temfilePaths = new ArrayList<>();
        //?????????????????????concat,?????????????????????
        int i=0;
        for (List<MusicGenerateParam> musicGenerateParamList : musicGenerateParamLists) {
            ArrayList<String> filePaths = new ArrayList<>();
            ArrayList<Double> startTime = new ArrayList<>();
            ArrayList<Double> duration = new ArrayList<>();
            for (MusicGenerateParam musicGenerateParam : musicGenerateParamList) {
                filePaths.add(musicLocalStorageParentPath+musicGenerateParam.getSavePath());
                startTime.add(musicGenerateParam.getStartTime());
                duration.add(musicGenerateParam.getDuration());
            }
            String outFilePath = concatTemPath+"/" + i + ".mp3";
            temfilePaths.add(outFilePath);
            String temFilePath = concatTemPath + "/tem/";
            MusicUtils.concatMp3ListWithNull(filePaths,startTime,duration,maxTime,
                    outFilePath,temFilePath);
            i++;
            //??????tem???????????????????????????
            File file = new File(concatTemPath+"/tem");
            String[] files = file.list();
            for (String f:files) {
                new File(concatTemPath+"/tem",f).delete();
            }
        }
        //??????tem?????????
        new File(concatTemPath+"/tem").delete();

        String savePath = UUID.randomUUID()+".mp3";
        //????????????
        MusicUtils.mixMp3List(temfilePaths,musicLocalStorageParentPath+savePath);
        //??????????????????,??????concatpath????????????????????????
        File file = new File(concatTemPath);
        String[] files = file.list();
        if(files.length>0){
            for (String f:files) {
                new File(concatTemPath,f).delete();
            }
            //????????????????????????????????????????????????
            new File(file.getPath()).delete();
        }else{
            //????????????????????????????????????????????????
            new File(file.getPath()).delete();
        }
        //????????????????????????
        boolean b = qiniuUtils.uploadLocal(musicLocalStorageParentPath + savePath,
                musicRemoteStorageParentPath + savePath);
        //????????????????????????
        if(!b){
            return Result.fail(ErrorCode.UPLOAD_MUSIC_ERROR.getCode(),
                    ErrorCode.UPLOAD_MUSIC_PHOTO_ERROR.getMsg());
        }
        //?????????????????????

        //??????????????????
        MusicGenerateReturn musicGenerateReturn = new MusicGenerateReturn();
        musicGenerateReturn.setUrl(qiniuUtils.url+musicRemoteStorageParentPath+savePath);
        musicGenerateReturn.setDuration(maxTime);
        return Result.success(musicGenerateReturn);
    }

}
