package com.movision.utils.oss;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.BucketInfo;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.movision.common.constant.MsgCodeConstant;
import com.movision.exception.BusinessException;
import com.movision.mybatis.systemLayout.service.SystemLayoutService;
import com.movision.utils.GetImgToWH;
import com.movision.utils.file.FileUtil;
import com.movision.utils.propertiesLoader.PropertiesLoader;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云OSS
 *
 * @author zhuangyuhao
 * @since 16/6/21.
 */
@Service
public class AliOSSClient {

    @Autowired
    private SystemLayoutService systemLayoutService;


    private static final Logger log = LoggerFactory.getLogger(AliOSSClient.class);

    // endpoint是访问OSS的域名。如果您已经在OSS的控制台上 创建了Bucket，请在控制台上查看域名。
    // 如果您还没有创建Bucket，endpoint选择请参看文档中心的“开发人员指南 > 基本概念 > 访问域名”，
    // 链接地址是：https://help.aliyun.com/document_detail/oss/user_guide/oss_concept/endpoint.html?spm=5176.docoss/user_guide/endpoint_region
    // endpoint的格式形如“http://oss-cn-hangzhou.aliyuncs.com/”，注意http://后不带bucket名称，
    // 比如“http://bucket-name.oss-cn-hangzhou.aliyuncs.com”，是错误的endpoint，请去掉其中的“bucket-name”。
    private static String endpoint = "http://oss-cn-shenzhen.aliyuncs.com";//"http://mofo.oss-cn-shenzhen.aliyuncs.com";MOFO

    // accessKeyId和accessKeySecret是OSS的访问密钥，您可以在控制台上创建和查看，
    // 创建和查看访问密钥的链接地址是：https://ak-console.aliyun.com/#/。
    // 注意：accessKeyId和accessKeySecret前后都没有空格，从控制台复制时请检查并去除多余的空格。
    private static String accessKeyId = "LTAITI0FOKgMZQty";//MOFO
    private static String accessKeySecret = "5x12H1yASg31OlPanhTCg0z0PzNr3t";//MOFO

    // Bucket用来管理所存储Object的存储空间，详细描述请参看“开发人员指南 > 基本概念 > OSS基本概念介绍”。
    // Bucket命名规范如下：只能包括小写字母，数字和短横线（-），必须以小写字母或者数字开头，长度必须在3-63字节之间。
    private static String bucketName = "mofo";//MOFO

    // Object是OSS存储数据的基本单元，称为OSS的对象，也被称为OSS的文件。详细描述请参看“开发人员指南 > 基本概念 > OSS基本概念介绍”。
    // Object命名规范如下：使用UTF-8编码，长度必须在1-1023字节之间，不能以“/”或者“\”字符开头。
    private static String firstKey = "my-first-key";


    /**
     * 初始化client
     *
     * @return
     */
    private OSSClient init() {
        // 创建ClientConfiguration实例，按照您的需要修改默认参数
        ClientConfiguration conf = new ClientConfiguration();
        // 开启支持CNAME选项
        conf.setSupportCname(true);
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret, conf);

        try {
            // 判断Bucket是否存在。详细请参看“SDK手册 > Java-SDK > 管理Bucket”。
            // 链接地址是：https://help.aliyun.com/document_detail/oss/sdk/java-sdk/manage_bucket.html?spm=5176.docoss/sdk/java-sdk/init
            if (ossClient.doesBucketExist(bucketName)) {
                log.debug("您已经创建Bucket：" + bucketName + "。");
            }else {
                log.debug("您的Bucket不存在，创建Bucket：" + bucketName + "。");
                // 创建Bucket。详细请参看“SDK手册 > Java-SDK > 管理Bucket”。
                // 链接地址是：https://help.aliyun.com/document_detail/oss/sdk/java-sdk/manage_bucket.html?spm=5176.docoss/sdk/java-sdk/init
                ossClient.createBucket(bucketName);
            }

            // 查看Bucket信息。详细请参看“SDK手册 > Java-SDK > 管理Bucket”。
            // 链接地址是：https://help.aliyun.com/document_detail/oss/sdk/java-sdk/manage_bucket.html?spm=5176.docoss/sdk/java-sdk/init
            BucketInfo info = ossClient.getBucketInfo(bucketName);
            log.debug("Bucket " + bucketName + "的信息如下：");
            log.debug("\t数据中心：" + info.getBucket().getLocation());
            log.debug("\t创建时间：" + info.getBucket().getCreationDate());
            log.debug("\t用户标志：" + info.getBucket().getOwner());

        } catch (ClientException ce) {
            ce.printStackTrace();
            log.error(ce.getErrorCode() + ":" + ce.getErrorMessage());
        }
        return ossClient;
    }


    /**
     * 上传字符串
     *
     * @param content
     * @return
     */
    public Map<String, String> uploadString(String content) {
        Map<String, String> result = new HashMap<>();
        log.info("阿里云OSS上传Started");
        OSSClient ossClient = init();
        try {

            ossClient.putObject(bucketName, firstKey, new ByteArrayInputStream(content.getBytes()));

        } catch (OSSException oe) {
            oe.printStackTrace();
            result.put("status", "fail");
            return result;
        } finally {
            ossClient.shutdown();
        }
        log.info("阿里云OSS上传Completed");
        result.put("status", "success");
        return result;
    }

    /**
     * 上传文件流
     *
     * @param file
     * @param type
     * @param chann 频道
     * @return
     */
    public Map<String, String> uploadStream(MultipartFile file, long maxSize, String type, String chann) {
        Map<String, String> result = new HashMap<>();

        log.info("阿里云OSS上传Started");
        OSSClient ossClient = init();
        InputStream in = null;

        try {
            long size = file.getSize();

            if (size > maxSize) {
                throw new BusinessException(MsgCodeConstant.SYSTEM_ERROR, "文件大小超过最大限制");
            }
            // 上传文件流
//            String domain;
            in = file.getInputStream();
            String fileName = file.getOriginalFilename();
            String fileKey;
            String fileName2 = FileUtil.renameFile(fileName);
            fileKey = defineFileKey(type, chann, fileName2);

            String data = "";
            if (type.equals("img")) {
                bucketName = PropertiesLoader.getValue("img.bucket");
//                domain = PropertiesLoader.getValue("img.domain");
                data = fileKey;


            } else if (type.equals("doc")) {
                bucketName = PropertiesLoader.getValue("file.bucket");
//                domain = PropertiesLoader.getValue("file.domain");
                data = fileName2;
            }

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setCacheControl("no-cache");
            ossClient.putObject(bucketName, fileKey, in, objectMetadata);

            log.debug("Object：" + fileKey + "存入OSS成功。");
            result.put("status", "success");
            result.put("data", data);

        } catch (OSSException oe) {
            oe.printStackTrace();
            result.put("status", "fail");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "fail");
            return result;
        } finally {
            ossClient.shutdown();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        log.info("阿里云OSS上传Completed");

        return result;
    }


    /**
     * 上传本地切割文件流
     *
     * @param file
     * @param type
     * @param chann 频道
     * @return
     */
    public Map<String, Object> uploadInciseStream(String file, String type, String chann) {
        Map<String, Object> result = new HashMap<>();

        log.info("阿里云OSS上传Started");
        OSSClient ossClient = init();
        InputStream is = null;

        File fil = new File(file);
        try {
            // 文件存储入OSS，Object的名称为fileKey。详细请参看“SDK手册 > Java-SDK > 上传文件”。
            // 链接地址是：https://help.aliyun.com/document_detail/oss/sdk/java-sdk/upload_object.html?spm=5176.docoss/user_guide/upload_object
            String fileKey;
            String domain;
            String fileName = FileUtil.renameFile(fil).getName();
            fileKey = defineFileKey(type, chann, fileName);

            String data = "";
            if (type.equals("img")) {
                //获取文件名称头部
                bucketName = systemLayoutService.queryImgBucket("img_bucket");
                //bucketName = PropertiesLoader.getValue("img.bucket");
                domain = systemLayoutService.queryServiceUrl("file_service_url");
                //domain = PropertiesLoader.getValue("formal.img.domain");//http://pic.mofo.shop
                data = domain + "/" + fileKey;
                //返回图片的宽高
                is = new FileInputStream(file);
                BufferedImage src = ImageIO.read(is);
                result.put("width", src.getWidth());
                result.put("height", src.getHeight());

            } else if (type.equals("doc")) {
                //获取文件名称头部
                bucketName = systemLayoutService.queryImgBucket("file_bucket");
                //bucketName = PropertiesLoader.getValue("file.bucket");
                data = fileName;
            }

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setCacheControl("no-cache");
            ossClient.putObject(bucketName, fileKey, fil, objectMetadata);
            log.debug("Object：" + fileKey + "存入OSS成功。");
            log.info("【上传Alioss的返回值】：" + result.toString());
            result.put("status", "success");

            result.put("url", data);

        } catch (OSSException oe) {
            oe.printStackTrace();
            log.error(oe.getErrorCode() + ":" + oe.getErrorMessage());
            result.put("status", "fail");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "fail");
            return result;
        } finally {
            ossClient.shutdown();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        log.info("阿里云OSS上传Completed");

        return result;
    }


    /**
     * 上传文件流
     *
     * @param file
     * @param type
     * @param chann 频道
     * @return
     */
    public Map<String, Object> uploadFileStream(MultipartFile file, String type, String chann, String domain) {
        //返回值
        Map<String, Object> result = new HashMap<>();

        log.info("阿里云OSS上传Started");
        OSSClient ossClient = init();
        InputStream in = null;
        FileInputStream b = null;
        try {
            long size = file.getSize();
            // 上传文件流
//            String domain;
            in = file.getInputStream();
            String fileName = file.getOriginalFilename();
            String fileKey;
            String fileName2 = FileUtil.renameFile(fileName);
            //获取fileKey
            fileKey = getFileKey(type, chann, size, fileName2);

            String data = "";
            if (type.equals("img")) {
                //bucketName = PropertiesLoader.getValue("img.bucket");
                bucketName = systemLayoutService.queryImgBucket("img_bucket");
//                domain = PropertiesLoader.getValue("ali.domain");
//                domain = PropertiesLoader.getValue("formal.img.domain");
                data = domain + "/" + fileKey;
                String maxSize = PropertiesLoader.getValue("uploadPicMaxPostSize");
                if (size > Long.valueOf(maxSize)) {
                    throw new BusinessException(MsgCodeConstant.SYSTEM_ERROR, "文件大小超过最大限制");
                }
                //返回图片的宽高
                //BufferedImage bi = ImageIO.read(file.getInputStream());
                //result.put("width", bi.getWidth());
                //result.put("height", bi.getHeight());
                CommonsMultipartFile cf = (CommonsMultipartFile) file;
                //这个myfile是MultipartFile的
                DiskFileItem fi = (DiskFileItem) cf.getFileItem();
                File fs = fi.getStoreLocation();

                b = new FileInputStream(fs);
                GetImgToWH imageInfo = new GetImgToWH(b);
                System.out.println(imageInfo);
                // Getting image data from a file
                imageInfo = new GetImgToWH(fs);
                System.out.println("ˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇ高" + imageInfo.getHeight());
                System.out.println("ˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇ宽" + imageInfo.getWidth());
                result.put("width", imageInfo.getWidth());
                result.put("height", imageInfo.getHeight());

            } else if (type.equals("doc")) {
                //bucketName = PropertiesLoader.getValue("file.bucket");
                bucketName = systemLayoutService.queryImgBucket("file_bucket");
                data = fileName2;
                String maxSize = PropertiesLoader.getValue("uploadDocMaxPostSize");
                if (size > Long.valueOf(maxSize)) {
                    throw new BusinessException(MsgCodeConstant.SYSTEM_ERROR, "文件大小超过最大限制");
                }
            }
            //oss api
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setCacheControl("no-cache");
            ossClient.putObject(bucketName, fileKey, in, objectMetadata);

            log.debug("Object：" + fileKey + "存入OSS成功。");
            log.info("【上传Alioss的返回值】：" + result.toString());
            result.put("status", "success");
            result.put("url", data);

        } catch (OSSException oe) {
            oe.printStackTrace();
            result.put("status", "fail");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "fail");
            return result;
        } finally {
            ossClient.shutdown();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (b != null) {
                b = null;
            }
            System.gc();
        }

        log.info("阿里云OSS上传Completed");

        return result;
    }

    private String getFileKey(String type, String chann, long size, String fileName2) {
        String fileKey;
        if (chann != null) {
            fileKey = "upload/" + chann + "/" + type + "/" + fileName2;

            if (type.equals("doc") && chann.equals("tech")) {
                String maxSize = PropertiesLoader.getValue("uploadTechMaxPostSize");
                if (size > Long.valueOf(maxSize)) {
                    throw new BusinessException(MsgCodeConstant.SYSTEM_ERROR, "文件大小超过最大限制");
                }
            }
        } else {
            fileKey = "upload/" + fileName2;
        }
        return fileKey;
    }

    /**
     * 上传本地文件
     *
     * @param file  本地文件名
     * @param type  文件类型 IMG:图片 | FILE:其他文件
     * @param chann 频道
     * @return
     */
    public Map<String, Object> uploadLocalFile(File file, String type, String chann, String domain) {
        Map<String, Object> result = new HashMap<>();

        log.info("阿里云OSS上传Started");
        OSSClient ossClient = init();
        InputStream is = null;

        try {
            // 文件存储入OSS，Object的名称为fileKey。详细请参看“SDK手册 > Java-SDK > 上传文件”。
            // 链接地址是：https://help.aliyun.com/document_detail/oss/sdk/java-sdk/upload_object.html?spm=5176.docoss/user_guide/upload_object
            String fileKey;
//            String domain;
            String fileName = FileUtil.renameFile(file).getName();
            fileKey = defineFileKey(type, chann, fileName);

            String data = "";
            if (type.equals("img")) {
                //获取文件名称头部
                bucketName = systemLayoutService.queryServiceUrl("img_bucket");
                //bucketName = PropertiesLoader.getValue("img.bucket");
//                domain = PropertiesLoader.getValue("ali.domain");
                data = domain + "/" + fileKey;
                //返回图片的宽高
                /*is = new FileInputStream(file);
                BufferedImage src = ImageIO.read(is);*/
                FileInputStream b = new FileInputStream(file);
                GetImgToWH imageInfo = new GetImgToWH(b);
                System.out.println(imageInfo);
                // Getting image data from a file
                imageInfo = new GetImgToWH(file);
                System.out.println("ˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇ高" + imageInfo.getHeight());
                System.out.println("ˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇ宽" + imageInfo.getWidth());
                result.put("width", imageInfo.getWidth());
                result.put("height", imageInfo.getHeight());

            } else if (type.equals("doc")) {
                //bucketName = PropertiesLoader.getValue("file.bucket");
                //获取文件名称头部
                bucketName = systemLayoutService.queryServiceUrl("file_bucket");
                data = fileName;
            }

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setCacheControl("no-cache");
            ossClient.putObject(bucketName, fileKey, file, objectMetadata);
            log.debug("Object：" + fileKey + "存入OSS成功。");
            result.put("status", "success");
            result.put("url", data);
            log.info("【上传Alioss的返回值】：" + result.toString());

        } catch (OSSException oe) {
            oe.printStackTrace();
            log.error(oe.getErrorCode() + ":" + oe.getErrorMessage());
            result.put("status", "fail");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "fail");
            return result;
        } finally {
            ossClient.shutdown();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        log.info("阿里云OSS上传Completed");

        return result;
    }


    /**
     * 上传本地文件
     *
     * @param file  本地文件名
     * @param type  文件类型 IMG:图片 | FILE:其他文件
     * @param chann 频道
     * @return
     */
    public Map<String, Object> uploadLocalFileByPersonPhoto(File file, String type, String chann) {
        Map<String, Object> result = new HashMap<>();

        log.info("阿里云OSS上传Started");
        OSSClient ossClient = init();

        try {
            // 文件存储入OSS，Object的名称为fileKey。详细请参看“SDK手册 > Java-SDK > 上传文件”。
            // 链接地址是：https://help.aliyun.com/document_detail/oss/sdk/java-sdk/upload_object.html?spm=5176.docoss/user_guide/upload_object
            String fileKey;
            String domain;
            String fileName = FileUtil.renameFile(file).getName();
            fileKey = defineFileKey(type, chann, fileName);

            String data = "";
            if (type.equals("img")) {
                bucketName = systemLayoutService.queryImgBucket("img_bucket");
                domain = systemLayoutService.queryServiceUrl("file_service_url");
                //bucketName = PropertiesLoader.getValue("img.bucket");
                //domain = PropertiesLoader.getValue("formal.img.domain");    //正式服 http://pic.mofo.shop
                data = domain + "/" + fileKey;
            } else if (type.equals("doc")) {
                bucketName = systemLayoutService.queryImgBucket("file_bucket");
                //bucketName = PropertiesLoader.getValue("file.bucket");
                data = fileName;
            }
            //核心api
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setCacheControl("no-cache");
            ossClient.putObject(bucketName, fileKey, file, objectMetadata);
            log.debug("Object：" + fileKey + "存入OSS成功。");
            result.put("status", "success");
            result.put("url", data);
            log.info("【上传Alioss的返回值】：" + result.toString());

        } catch (OSSException oe) {
            oe.printStackTrace();
            log.error(oe.getErrorCode() + ":" + oe.getErrorMessage());
            result.put("status", "fail");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "fail");
            return result;
        } finally {
            ossClient.shutdown();
        }
        log.info("阿里云OSS上传Completed");
        return result;
    }

    /**
     * 定义fileKey的值，即oss上的存储路径
     *
     * @param type
     * @param chann
     * @param fileName
     * @return
     */
    private String defineFileKey(String type, String chann, String fileName) {
        String fileKey;
        if (chann != null) {
            fileKey = "upload/" + chann + "/" + type + "/" + fileName;
        } else {
            fileKey = "upload/" + fileName;
        }
        return fileKey;
    }


    /**
     * 下载文件
     *
     * @param fileName
     * @param type
     * @param chann
     * @return
     */
    public Map<String, Object> downloadStream(String fileName, String type, String chann) {
        Map<String, Object> result = new HashMap<>();
        log.info("阿里云OSS下载Started");
        OSSClient ossClient = init();
        InputStream is = null;
        try {
            if (type.equals("img")) {
                //bucketName = PropertiesLoader.getValue("img.bucket");
                bucketName = systemLayoutService.queryImgBucket("img_bucket");

            } else if (type.equals("doc")) {
                //bucketName = PropertiesLoader.getValue("file.bucket");
                bucketName = systemLayoutService.queryImgBucket("file_bucket");
            }

            String objKey;
            objKey = defineFileKey(type, chann, fileName);


            OSSObject ossObject = ossClient.getObject(bucketName, objKey);
            is = ossObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(is);

            result.put("status", "success");
            result.put("data", bytes);

        } catch (OSSException oe) {
            oe.printStackTrace();
            log.error(oe.getErrorCode() + ":" + oe.getErrorMessage());
            result.put("status", "fail");
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            result.put("status", "fail");
            return result;
        } finally {
            ossClient.shutdown();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        log.info("阿里云OSS下载Completed");
        return result;
    }

    //test
    public static void main(String[] args) {
        AliOSSClient client = new AliOSSClient();
        String fileName = "/Users/zhuangyuhao/Downloads/1111.jpg";
        File file = new File(fileName);
        Map<String, Object> result = client.uploadLocalFile(file, "doc", null, "movision");
        System.out.println(result);

//        String name = file.getName();
//        String a = FileUtil.renameFile(name);
//        System.out.println(a);
//        Map<String, Object> map = client.downloadStream(fileName, "doc", "job");
//        System.out.println(map);
    }

}
