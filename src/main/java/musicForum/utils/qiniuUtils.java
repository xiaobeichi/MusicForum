package musicForum.utils;

import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSON;

@Component
public class qiniuUtils {
    //@Value("${user.default-avatar}")
    private static String dafaultAvatorPath="/music/photo/avator/default";
    //@Value("${qiniu.AccessKey}")
    private static String accessKey= "ClKlr0st1nn2IrLIEI846K8VG06aShpRazIFrSDl";
    //@Value("${qiniu.SecretKey}")
    private static String secretKey= "0PlgSkJwR9OUV7Q9GiPEwYEhIcrN8tuBoaAl7Izl";

    public static final String url = "http://rcf6dbqdd.hd-bkt.clouddn.com/";

    public static boolean upload(MultipartFile file, String fileName){
            //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huadong());
            //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
            //...生成上传凭证，然后准备上传
        String bucket = "music20222";
            //默认不指定key的情况下，以文件内容的hash值作为文件名
        try {
            byte[] uploadBytes = file.getBytes();
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            Response response = uploadManager.put(uploadBytes, fileName, upToken);
                //解析上传成功的结果
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean uploadLocal(String filePath,String savePath){
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huadong());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传
        String bucket = "music20222";
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        try {
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            Response response = uploadManager.put(filePath, savePath, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
