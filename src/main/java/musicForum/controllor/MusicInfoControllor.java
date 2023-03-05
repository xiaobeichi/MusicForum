package musicForum.controllor;

import musicForum.service.MusicInfoService;
import musicForum.utils.Result;
import musicForum.vo.param.login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 获取设置音频信息
 */
@RestController
@RequestMapping("music/musicInfo")
public class MusicInfoControllor {
    @Autowired
    MusicInfoService musicInfoService;

    /**
     * 获取特定音乐的详细信息
     * @param id
     * @param userId
     * @return
     */
    @GetMapping("detail/{id}")
    public Result getMusicDetail(@PathVariable("id") Long id,@RequestParam(value = "userId",required = false) Long userId){
        //id，音频链接（远程），图片链接，发布时间，音频名称，发布者id，点赞数，评论内容
        return musicInfoService.getMusicDetail(id,userId);
    }

    /**
     * 返回我上传的所有音乐id
     * @param userId
     * @return
     */
    @GetMapping("myUpload/{id}")
    public Result getMyUpload(@PathVariable("id") Long userId){
        //id，音频链接（远程），图片链接，发布时间，音频名称，发布者id，点赞数，评论内容
        return musicInfoService.getMyUpload(userId);
    }
    /**
     * 返回我点赞的所有音乐id
     * @param userId
     * @return
     */
    @GetMapping("myLike/{id}")
    public Result getMyLike(@PathVariable("id") Long userId){
        //id，音频链接（远程），图片链接，发布时间，音频名称，发布者id，点赞数，评论内容
        return musicInfoService.getMyLike(userId);
    }
    /**
     * 返回我收藏的所有音乐id
     * @param userId
     * @return
     */
    @GetMapping("myFavorite/{id}")
    public Result getMyFavorite(@PathVariable("id") Long userId){
        //id，音频链接（远程），图片链接，发布时间，音频名称，发布者id，点赞数，评论内容
        return musicInfoService.getMyFavorite(userId);
    }
    //获取音频时长？

    /**
     * 获取合成音频需要的子音频信息
     * @return
     */
    @GetMapping("infoForGenerate")
    public Result getInfoForGenerate(){
        return musicInfoService.getInfoForGenerate();
    }
}
