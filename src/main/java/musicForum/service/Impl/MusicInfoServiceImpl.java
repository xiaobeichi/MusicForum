package musicForum.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import musicForum.bean.*;
import musicForum.mapper.*;
import musicForum.service.MusicInfoService;
import musicForum.utils.Result;
import musicForum.utils.qiniuUtils;
import musicForum.vo.result.MusicForGenerateReturn;
import musicForum.vo.result.MusicListReturn;
import musicForum.vo.result.MusicCommentReturn;
import musicForum.vo.result.MusicDetailReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MusicInfoServiceImpl implements MusicInfoService {
    @Autowired
    private MusicUploadMapper musicUploadMapper;
    @Autowired
    private MusicLikeMapper musicLikeMapper;
    @Autowired
    private MusicFavoriteMapper musicFavoriteMapper;
    @Autowired
    private MusicCommentMapper musicCommentMapper;
    @Autowired
    private UserMapper userMapper;
    @Value("${music.remote-storage-parent-path}")
    private String musicParentPath;
    @Value("${music.musicImg-parent-path}")
    private String musicImgParentPath;
    @Value("${ip}")
    private String ip;

    /**
     * 之后可以改成按照时间排序返回评论
     * @param id
     * @param userId
     * @return
     */
    public Result getMusicDetail(Long id,Long userId){
        //分别查出内容，汇总到返回结构
        musicUpload musicUpload = musicUploadMapper.selectById(id);
        MusicDetailReturn musicReturn = musicUploadToVo(musicUpload);
        //查出点赞总数和收藏总数
        QueryWrapper<musicLike> musicLikeQueryWrapper = new QueryWrapper<>();
        musicLikeQueryWrapper.eq("music_id",id);
        musicReturn.setLikeNum(musicLikeMapper.selectCount(musicLikeQueryWrapper));
        QueryWrapper<musicFavorite> musicFavoriteQueryWrapper = new QueryWrapper<>();
        musicFavoriteQueryWrapper.eq("music_id",id);
        musicReturn.setFavNum(musicFavoriteMapper.selectCount(musicFavoriteQueryWrapper));
        if(userId!=null){
            musicLikeQueryWrapper.eq("user_id",userId);
            musicLike musicLike = musicLikeMapper.selectOne(musicLikeQueryWrapper);
            if(musicLike!=null){
                musicReturn.setIsMyLike(1);
            }else {
                musicReturn.setIsMyLike(0);
            }
            musicFavoriteQueryWrapper.eq("user_id",userId);
            musicFavorite musicFavorite = musicFavoriteMapper.selectOne(musicFavoriteQueryWrapper);
            if(musicFavorite!=null){
                musicReturn.setIsMyFav(1);
            }else {
                musicReturn.setIsMyFav(0);
            }
        }
        //查出评论,先用一个函数查出评论列表，包括子评论，再用一个函数转为return值的列表
        musicReturn.setComments(getComments(id));

        return Result.success(musicReturn);
    }
    public MusicDetailReturn musicUploadToVo(musicUpload musicUpload){
        MusicDetailReturn musicDetailReturn = new MusicDetailReturn();
        musicDetailReturn.setAudioId(musicUpload.getId());
        musicDetailReturn.setAudioImg(musicImgParentPath+musicUpload.getPhotoPath());
        musicDetailReturn.setAudioName(musicUpload.getName());
        musicDetailReturn.setAudioTime(musicUpload.getUpTime());
        musicDetailReturn.setAudioUrl(qiniuUtils.url+musicParentPath+musicUpload.getSavePath());
        musicDetailReturn.setAuthorId(musicUpload.getUpUserId());
        musicDetailReturn.setDescription(musicUpload.getDescription());
        musicDetailReturn.setDuration(musicUpload.getDuration());
        return musicDetailReturn;
    }
    public List<MusicCommentReturn> getComments(Long id){
        QueryWrapper<musicComment> musicCommentQueryWrapper = new QueryWrapper<>();
        musicCommentQueryWrapper.eq("music_id",id).eq("level",1).orderByAsc("comment_time");
        List<musicComment> musicComments = musicCommentMapper.selectList(musicCommentQueryWrapper);
        //转return再查子评论
        ArrayList<MusicCommentReturn> musicCommentReturns = new ArrayList<>();
        for (musicComment musiccomment : musicComments) {
            musicCommentReturns.add(commentToVo(musiccomment));
        }
        return musicCommentReturns;
    }
    public MusicCommentReturn commentToVo(musicComment comment){
        MusicCommentReturn musiccommentReturn = new MusicCommentReturn();
        musiccommentReturn.setCommentTime(comment.getCommentTime());
        musiccommentReturn.setContent(comment.getContent());
        musiccommentReturn.setId(comment.getId());
        musiccommentReturn.setLevel(comment.getLevel());
        musiccommentReturn.setUserId(comment.getUserId());
        if(comment.getLevel()==1) {
            musiccommentReturn.setChildrenComment(getChildrenComment(comment.getMusicId(), comment.getId()));
        }
        return musiccommentReturn;
    }
    public List<MusicCommentReturn> getChildrenComment(Long musicId, Long parentId){
        QueryWrapper<musicComment> musicCommentQueryWrapper = new QueryWrapper<>();
        musicCommentQueryWrapper.eq("music_id",musicId).eq("parent_id",parentId).eq("level",2)
                .orderByAsc("comment_time");
        List<musicComment> musicComments = musicCommentMapper.selectList(musicCommentQueryWrapper);
        ArrayList<MusicCommentReturn> musicCommentReturns = new ArrayList<>();
        for (musicComment musiccomment : musicComments) {
            musicCommentReturns.add(commentToVo(musiccomment));
        }
        return musicCommentReturns;
    }

    /**
     * 获取我上传的音乐id
     * @param userId
     * @return
     */
    public Result getMyUpload(Long userId){
        //是否要对userid进行判断用户是否存在
        //或者可以用拦截器做到
        QueryWrapper<musicUpload> musicUploadQueryWrapper = new QueryWrapper<>();
        musicUploadQueryWrapper.eq("up_user_id",userId).orderByDesc("up_time");
        //可以改成自写sql使得直接返回id列表，这里暂时先查出所有记录
        List<musicUpload> musicUploads = musicUploadMapper.selectList(musicUploadQueryWrapper);
        ArrayList<MusicListReturn> musicListReturns = new ArrayList<>();
        for (musicUpload musicUpload : musicUploads) {
            MusicListReturn musicListReturn = new MusicListReturn();
            musicListReturn.setId(musicUpload.getId());
            musicListReturns.add(musicListReturn);
        }
        return Result.success(musicListReturns);
    }

    /**
     * 获取我点赞的音乐id
     * @param userId
     * @return
     */
    public Result getMyLike(Long userId){
        QueryWrapper<musicLike> musicLikeQueryWrapper = new QueryWrapper<>();
        musicLikeQueryWrapper.eq("user_id",userId).orderByDesc("like_time");
        List<musicLike> musicLikes = musicLikeMapper.selectList(musicLikeQueryWrapper);
        ArrayList<MusicListReturn> musicListReturns = new ArrayList<>();
        for (musicLike musicLike : musicLikes) {
            MusicListReturn musicListReturn = new MusicListReturn();
            musicListReturn.setId(musicLike.getMusicId());
            musicListReturns.add(musicListReturn);
        }
        return Result.success(musicListReturns);
    }

    /**
     * 获取我收藏的音乐id
     * @param userId
     * @return
     */
    public Result getMyFavorite(Long userId){
        QueryWrapper<musicFavorite> musicFavoriteQueryWrapper = new QueryWrapper<>();
        musicFavoriteQueryWrapper.eq("user_id",userId).orderByDesc("fav_time");
        List<musicFavorite> musicFavorites = musicFavoriteMapper.selectList(musicFavoriteQueryWrapper);
        ArrayList<MusicListReturn> musicListReturns = new ArrayList<>();
        for (musicFavorite musicFavorite : musicFavorites) {
            MusicListReturn musicListReturn = new MusicListReturn();
            musicListReturn.setId(musicFavorite.getMusicId());
            musicListReturns.add(musicListReturn);
        }
        return Result.success(musicListReturns);
    }

    public Result getInfoForGenerate(){
        List<musicUpload> musicUploads = musicUploadMapper.selectList(null);
        ArrayList<MusicForGenerateReturn> musicForGenerateReturns = new ArrayList<>();
        for (musicUpload musicUpload : musicUploads) {
            MusicForGenerateReturn musicForGenerateReturn = new MusicForGenerateReturn();
            musicForGenerateReturn.setAudioName(musicUpload.getName());
            musicForGenerateReturn.setDescription(musicUpload.getDescription());
            musicForGenerateReturn.setId(musicUpload.getId());
            musicForGenerateReturn.setDuration(musicUpload.getDuration());
            Long upUserId = musicUpload.getUpUserId();
            users user = userMapper.selectById(upUserId);
            musicForGenerateReturn.setAuthorName(user.getNickname());
            musicForGenerateReturn.setSavePath(musicUpload.getSavePath());
            musicForGenerateReturn.setUrl(qiniuUtils.url+musicParentPath+musicUpload.getSavePath());
            musicForGenerateReturns.add(musicForGenerateReturn);
        }
        return Result.success(musicForGenerateReturns);
    }
}
