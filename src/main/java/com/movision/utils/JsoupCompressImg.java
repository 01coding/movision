package com.movision.utils;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import com.movision.facade.boss.PostFacade;
import com.movision.facade.upload.UploadFacade;
import com.movision.fsearch.utils.StringUtil;
import com.movision.mybatis.compressImg.entity.CompressImg;
import com.movision.utils.file.FileUtil;
import com.movision.utils.oss.AliOSSClient;
import com.movision.utils.oss.MovisionOssClient;
import com.movision.utils.oss.UploadUtil;
import com.movision.utils.propertiesLoader.PropertiesLoader;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

/**
 * @Author shuxf
 * @Date 2017/3/15 16:09
 * 通过jsoup解析帖子表中的content字段，解析html标签，处理帖子中的img，进行压缩处理
 */
@Service
public class JsoupCompressImg {

    @Autowired
    AliOSSClient aliossClient;

    @Autowired
    private PostFacade postFacade;

    @Autowired
    private MovisionOssClient movisionOssClient;

    @Autowired
    private UploadFacade uploadFacade;

    private static final Logger log = LoggerFactory.getLogger(JsoupCompressImg.class);

    /**
     * jsoup解析html字符串，处理img
     *
     * @param content
     * @input 富文本标签带压缩前图片img
     * @return  富文本标签带压缩后图片img
     */
    public Map<String, Object> compressImg(HttpServletRequest request, String content) {//content为带有img和html标签的富文本内容

        //通过jsoup解析html
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            Document doc = Jsoup.parse(content);
            Elements titleElms = doc.getElementsByTag("img");

            String compress_dir_path = uploadFacade.getConfigVar("post.tempimg.domain");//压缩图片路径url

            String compress_dir_local_path = PropertiesLoader.getValue("post.img.local.domain");//获取项目根目录/WWW/tomcat-8100/apache-tomcat-7.0.73/webapps/movision

            String savedDir = request.getSession().getServletContext().getRealPath("");
            String tempDir = savedDir.substring(0, savedDir.lastIndexOf("/")) + compress_dir_local_path;
            log.info("测试获取的压缩图片服务器路径>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + savedDir.substring(0, savedDir.lastIndexOf("/")));

            List<String> existFileList = getExistFiles(compress_dir_path);//定义一个已处理集合

            for (int i = 0; i < titleElms.size(); i++) {
                //遍历帖子中的所有图片列表

                //从img标签中获取src属性
                String imgurl = titleElms.get(i).attr("src");
                log.info("压缩前的原图url，imgurl=" + imgurl);

                //从阿里云服务器下载到本地
                String tempvideodir = uploadFacade.getConfigVar("post.tempimg.domain");//下载到本地的图片临时存放目录
                log.info("本地图片临时存放目录>>>>>>>>>" + tempvideodir);
                File dirFile = new File(tempvideodir);
                if(!dirFile.exists()){//文件路径不存在时，自动创建目录
                    dirFile.mkdir();
                }
                String filename = FileUtil.getPicName(imgurl);//获取图片文件名
                log.info("filename=" + filename);
                String PATH = tempvideodir + filename;//服务器上下载的临时图片路径（待处理文件）
                //从服务器上获取文件并保存
                URL theURL = new URL(imgurl);//下载链接
                URLConnection connection = theURL.openConnection();
                InputStream in = connection.getInputStream();
                FileOutputStream os = new FileOutputStream(PATH);
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    os.write(buffer, 0, read);
                }
                os.close();
                in.close();

                //通过帖子中的imgurl查询图片压缩关系表中是否存在该图的压缩记录
                int sum = postFacade.queryIsHaveCompress(imgurl);

                //获取原图绝对路径和图片大小
                File file = new File(PATH);
                //获取原图大小
                FileInputStream fis = new FileInputStream(file);
                int s = fis.available();
                DecimalFormat df = new DecimalFormat("######0.00");
                String filesize = df.format((double) s / 1024 / 1024);
                log.info("测试原图的文件大小>>>>>>>>>>>>>>>>>>>>>>>>" + filesize + "M");

                if (sum == 0 && s > 800 * 1024) {
                    //如果没压缩过且图片大小超过400kb就进行压缩，压缩过的或大小<=400kb不处理（防止修改帖子时对压缩过的图片进行重复压缩，同时也保证了低质量图片的品质）

                    boolean compressFlag = false;

                    //根据原图尺寸计算压缩后的图片尺寸
                    BufferedImage image = ImageIO.read(fis);
                    int w = (int) (image.getWidth()*0.20);//对原图尺寸的宽高比压缩20%
                    int h = (int) (image.getHeight()*0.20);

                    if (StringUtils.isNotEmpty(imgurl)) {

                        // 1 生成压缩后的图片的url
                        String tempfilename = UUID.randomUUID().toString() + filename.substring(filename.lastIndexOf("."), filename.length()-1);
                        String compress_file_path = compress_dir_path + tempfilename;
                        File compressfile = new File(compress_file_path);
                        log.info("压缩后的图片url，compress_file_path=" + compress_file_path);

                        // 2 判断该文件夹下是否有同名的图片，若有则不处理，若没有则进行处理
                        if (CollectionUtils.isEmpty(existFileList) || !existFileList.contains(filename)) {
                            // 压缩核心算法
                            compressFlag = compressJpgOrPng(w, h, compressFlag, filename, PATH, tempDir);
                            // 处理过的图片加入到已处理集合，防止重复压缩图片
                            existFileList.add(filename);
                        } else {
                            compressFlag = true;
                            log.info("该图片已存在，不需要压缩，filename=" + filename);
                        }
                        if (compressFlag) {
                            //上传压缩图到阿里云OSS
                            Map m = movisionOssClient.uploadFileObject(file, "img", "postCompressImg");//----file----->compressfile------shuxf 20170619
                            String compressurl = String.valueOf(m.get("url"));//压缩图片上传阿里云的返回url

                            String newimgurl = "";
                            //拿实际url第三个斜杠后面的内容和formal.img.domain进行拼接
                            for(int j = 0; j < 3; j++){
                                compressurl = compressurl.substring(compressurl.indexOf("/")+1 );
                            }
//                            newimgurl = PropertiesLoader.getValue("formal.img.domain") + "/" + compressurl;//拿实际url第三个斜杠后面的内容和formal.img.domain进行拼接，如："http://pic.mofo.shop" + "/upload/postCompressImg/img/yDi0T2nY1496812117357.png"
                            newimgurl = FileUtil.getDomain(imgurl) + "/" + compressurl;//动态获取原图链接的前缀域名（确保保存到数据库中的测试数据和正式数据url的区别）

                            //如果压缩保存成功，这里替换文章中的第i个img标签中的src属性
                            titleElms.get(i).attr("src", newimgurl);

                            //保存缩略图和原图的映射关系到数据库中yw_compress_img
                            CompressImg compressImg = new CompressImg();
                            compressImg.setCompressimgurl(newimgurl);
                            compressImg.setProtoimgurl(imgurl);
                            compressImg.setProtoimgsize(filesize);
                            int count = postFacade.queryCount(compressImg);
                            if (count == 0) {
                                //如果已经保存过就不再保存
                                postFacade.addCompressImg(compressImg);
                            } else {
                                log.info("原图:" + imgurl + "已进行过压缩，且已存在映射关系");
                            }
                        }

                        //压缩上传存储完成后，删除ECS下载的原图和压缩图的临时文件 compress_file_path 和 PATH
                        //实际原图的路径和文件名与压缩后的图片路径和文件名一模一样，所以会被覆盖，因此下面方法执行一个即可
                        //compressfile.delete();//删除压缩图
                        file.delete();//删除原图
                    }
                } else if (sum != 0) {
                    //虽然图片大于800kb，但是图片压缩过，存在映射关系
                    log.info("帖子中包含的已处理过的图片（压缩图片表存在映射）>>>>>>>>>>>>>>>>>>>>>>>>>" + imgurl);

                } else if (sum == 0 && s <= 800 * 1024) {
                    //图片未压缩过，也不存在映射关系，但是图片大小低于800kb

                    //不做图片的压缩，直接使用原图的url当做压缩后的url来存
                    CompressImg compressImg = new CompressImg();
                    compressImg.setCompressimgurl(imgurl);
                    compressImg.setProtoimgurl(imgurl);
                    compressImg.setProtoimgsize(filesize);
                    int count = postFacade.queryCount(compressImg);
                    if (count == 0) {
                        //如果已经保存过就不再保存
                        postFacade.addCompressImg(compressImg);//保存缩略图和原图的映射关系到数据库中yw_compress_img
                    } else {
                        log.info("原图:" + imgurl + "已进行过压缩，且已存在映射关系");
                    }
                }
            }
            String a = doc.html().replaceAll("\\n", "").replaceAll("\\\\", "").replaceAll("<html>", "").replaceAll("<head>", "").replaceAll("<body>", "").replaceAll("</html>", "").replaceAll("</head>", "").replaceAll("</body>", "");
            log.info("测试返回的content字符串:::::::::>" + a);
            map.put("code", 200);
            map.put("msg", "帖子所有图片压缩完成");
            map.put("content", a);//压缩后的帖子内容html标签字符串

        } catch (Exception e) {
            map.put("code", 300);
            map.put("msg", "帖子所有图片压缩失败");
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 解析json字符串，压缩替换img
     * @param request 请求体
     * @param content 存储帖子正文的josn字符串
     * @input [{"orderid": 0,"type": 0,"value": "测试发帖","wh": "","dir": ""},{"orderid": 1,"type": 1,"value": "http://pic.mofo.shop/upload/post/img/UUKD5Qws1497260964776.jpg","wh": "750x440","dir": "/var/temp/fime0/tewsdvsd/test.jpg"},{"orderid": 2,"type": 2,"value": "36e7fea5ddc347588fc19c0b75d7930c","wh": "","dir": "/var/temp/fime0/tewsdvsd/test.mov"}]
     * @output [{"orderid":0,"type":0,"value":"测试发帖","wh":"","dir":""},{"wh":"750x440","orderid":1,"type":1,"dir":"/var/temp/fime0/tewsdvsd/test.jpg","value":"http://pic.mofo.shop/upload/postCompressImg/img/nowuIRJV1497527940181.jpg"},{"orderid":2,"type":2,"value":"36e7fea5ddc347588fc19c0b75d7930c","wh":"","dir":"/var/temp/fime0/tewsdvsd/test.mov"}]
     * @return
     */
    public Map<String, Object> newCompressImg(HttpServletRequest request, String content) {//content为存储帖子正文的josn字符串

        log.info("帖子内图片压缩工具接收到的正文json内容>>>>>>>>" + content);
        //转json字符串为json对象
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            //定义是否是视频贴的标志位
            int flag = 0;//0 图文贴 >0 视频贴（视频的个数）
            JSONArray moduleArray = JSONArray.fromObject(content);

            String compress_dir_path = uploadFacade.getConfigVar("post.tempimg.domain");//压缩图片路径url

            String compress_dir_local_path = PropertiesLoader.getValue("post.img.local.domain");//获取项目根目录/WWW/tomcat-8100/apache-tomcat-7.0.73/webapps/movision

            String savedDir = request.getSession().getServletContext().getRealPath("");
            String tempDir = savedDir.substring(0, savedDir.lastIndexOf("/")) + compress_dir_local_path;
            log.info("测试获取的压缩图片服务器路径>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + savedDir.substring(0, savedDir.lastIndexOf("/")));

            List<String> existFileList = getExistFiles(compress_dir_path);//定义一个已处理集合

            for (int i = 0; i < moduleArray.size(); i++) {
                //遍历帖子中的所有图片列表

                //从img中获取type属性
                JSONObject moduleobj = JSONObject.parseObject(moduleArray.get(i).toString());
                Integer orderid = (Integer) moduleobj.get("orderid");
                Integer type = (Integer) moduleobj.get("type");
                String value = (String) moduleobj.get("value");
                String wh = (String) moduleobj.get("wh");
                String dir = (String) moduleobj.get("dir");
                //0 文字 1 图片 2 视频
                if (type == 1) {

                    String imgurl = value;
                    log.info("压缩前的原图url，imgurl=" + imgurl);

                    //从阿里云服务器下载到本地
                    String tempvideodir = uploadFacade.getConfigVar("post.tempimg.domain");//下载到本地的图片临时存放目录
                    log.info("本地图片临时存放目录>>>>>>>>>" + tempvideodir);
                    File dirFile = new File(tempvideodir);
                    if (!dirFile.exists()) {//文件路径不存在时，自动创建目录
                        dirFile.mkdir();
                    }
                    String filename = FileUtil.getPicName(imgurl);//获取图片文件名
                    log.info("filename=" + filename);
                    String PATH = tempvideodir + filename;//服务器上下载的临时图片路径（待处理文件）
                    //从服务器上获取文件并保存
                    URL theURL = new URL(imgurl);//下载链接
                    URLConnection connection = theURL.openConnection();
                    InputStream in = connection.getInputStream();
                    FileOutputStream os = new FileOutputStream(PATH);
                    byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = in.read(buffer)) > 0) {
                        os.write(buffer, 0, read);
                    }
                    os.close();
                    in.close();

                    //通过帖子中的imgurl查询图片压缩关系表中是否存在该图的压缩记录
                    int sum = postFacade.queryIsHaveCompress(imgurl);

                    //获取原图绝对路径和图片大小
                    File file = new File(PATH);//获取原图大小
                    FileInputStream fis = new FileInputStream(file);
                    int s = fis.available();
                    DecimalFormat df = new DecimalFormat("######0.00");
                    String filesize = df.format((double) s / 1024 / 1024);
                    log.info("第一次测试原图存储路径>>>>>>>>>>>>>>>>>>>>" + PATH);
                    log.info("测试原图的文件大小>>>>>>>>>>>>>>>>>>>>>>>>" + filesize + "M");

                    if (sum == 0 && s > 800 * 1024) {
                        //1.如果没压缩过 && 2.图片大小超过800kb 就进行压缩，压缩过的或大小<=800kb不处理（防止修改帖子时对压缩过的图片进行重复压缩，同时也保证了低质量图片的品质）

                        boolean compressFlag = false;

                        //根据原图尺寸计算压缩后的图片尺寸
                        BufferedImage image = ImageIO.read(fis);
                        int w = (int) (image.getWidth()*0.20);//对原图尺寸的宽高比压缩20%
                        int h = (int) (image.getHeight()*0.20);

                        if (StringUtils.isNotEmpty(imgurl)) {

                            // 1 生成压缩后的图片的url
                            String tempfilename = UUID.randomUUID().toString() + filename.substring(filename.lastIndexOf("."), filename.length());
                            String compress_file_path = compress_dir_path + tempfilename;
                            File compressfile = new File(compress_file_path);
                            log.info("压缩后的图片url，compress_file_path=" + compress_file_path);

                            // 2 判断该文件夹下是否有同名的图片，若有则不处理，若没有则进行处理
                            if (CollectionUtils.isEmpty(existFileList) || !existFileList.contains(filename)) {
                                // 压缩核心算法
                                compressFlag = compressJpgOrPng(w, h, compressFlag, filename, PATH, tempDir);
                                // 处理过的图片加入到已处理集合，防止重复压缩图片
                                existFileList.add(filename);
                            } else {
                                compressFlag = true;
                                log.info("该图片已存在，不需要压缩，filename=" + filename);
                            }
                            if (compressFlag) {
                                //上传压缩图到阿里云OSS
                                Map m = movisionOssClient.uploadFileObject(file, "img", "postCompressImg");
                                String compressurl = String.valueOf(m.get("url"));//压缩图片上传阿里云的返回url

                                String newimgurl = "";
                                //拿实际url第三个斜杠后面的内容和formal.img.domain进行拼接
                                for (int j = 0; j < 3; j++) {
                                    compressurl = compressurl.substring(compressurl.indexOf("/") + 1);
                                }
                                newimgurl = PropertiesLoader.getValue("formal.img.domain") + "/" + compressurl;//拿实际url第三个斜杠后面的内容和formal.img.domain进行拼接，如："http://pic.mofo.shop" + "/upload/postCompressImg/img/yDi0T2nY1496812117357.png"

                                moduleArray.remove(i);
                                Map<String, Object> res = new HashMap<>();
                                res.put("orderid", orderid);
                                res.put("type", type);
                                res.put("value", newimgurl);
                                res.put("wh", wh);
                                res.put("dir", dir);
                                moduleArray.add(i, res);
                                log.info("测试替换后的json字符串>>>>>>>"+moduleArray.toString());

                                //保存缩略图和原图的映射关系到数据库中yw_compress_img
                                CompressImg compressImg = new CompressImg();
                                compressImg.setCompressimgurl(newimgurl);
                                compressImg.setProtoimgurl(imgurl);
                                compressImg.setProtoimgsize(filesize);
                                compressImg.setIntime(new Date());
                                int count = postFacade.queryCount(compressImg);
                                if (count == 0) {
                                    //如果已经保存过就不再保存
                                    postFacade.addCompressImg(compressImg);
                                } else {
                                    log.info("原图:" + imgurl + "已进行过压缩，且已存在映射关系");
                                }
                            }

                            //压缩上传存储完成后，删除ECS下载的原图和压缩图的临时文件 compress_file_path 和 PATH
                            //实际原图的路径和文件名与压缩后的图片路径和文件名一模一样，所以会被覆盖，因此下面方法执行一个即可
                            //compressfile.delete();//删除压缩图
                            file.delete();//删除原图
                        }
                    } else if (sum != 0) {
                        //虽然图片大于800kb，但是图片压缩过，存在映射关系
                        log.info("帖子中包含的已处理过的图片（压缩图片表存在映射）>>>>>>>>>>>>>>>>>>>>>>>>>" + imgurl);

                    } else if (sum == 0 && s <= 800 * 1024) {
                        //图片未压缩过且图片大小<=800kb

                        //不做图片的压缩，直接使用原图的url当做压缩后的url来存
                        CompressImg compressImg = new CompressImg();
                        compressImg.setCompressimgurl(imgurl);
                        compressImg.setProtoimgurl(imgurl);
                        compressImg.setProtoimgsize(filesize);
                        compressImg.setIntime(new Date());
                        int count = postFacade.queryCount(compressImg);
                        if (count == 0) {
                            //如果已经保存过就不再保存
                            postFacade.addCompressImg(compressImg);//保存缩略图和原图的映射关系到数据库中yw_compress_img
                        } else {
                            log.info("原图:" + imgurl + "已进行过压缩，且已存在映射关系");
                        }
                    }
                }else if (type == 2){
                    flag = flag + 1;
                }
            }
            String a = moduleArray.toString();
            log.info("测试返回的content字符串:::::::::>" + a);
            map.put("code", 200);
            map.put("flag", flag);
            map.put("msg", "帖子所有图片压缩完成");
            map.put("content", a);//压缩后的帖子内容为json字符串，json数组模块化

        } catch (Exception e) {
            map.put("code", 300);
            map.put("msg", "帖子所有图片压缩失败");
            e.printStackTrace();
        }
        return map;
    }


    public boolean compressJpgOrPng(int w, int h, boolean compressFlag, String filename, String filePath, String tempDir) {
        if (filename.toLowerCase().endsWith(".jpg")) {

            log.info("压缩jpg图片，filepath=" + filePath);
            compressFlag = ImgCompressUtil.ImgCompress(filePath, tempDir, w, h);
        }
        if (filename.toLowerCase().endsWith(".png")) {
            log.info("压缩png图片，filepath=" + filePath);
            compressFlag = ImgCompressUtil.ImgCompress(filePath, tempDir, w, h);
        }
        if (filename.toLowerCase().endsWith(".jpeg")) {

            log.info("压缩jpeg图片，filepath=" + filePath);
            compressFlag = ImgCompressUtil.ImgCompress(filePath, tempDir, w, h);
        }
        if (filename.toLowerCase().endsWith(".bmp")) {

            log.info("压缩bmp图片，filepath=" + filePath);
            compressFlag = ImgCompressUtil.ImgCompress(filePath, tempDir, w, h);
        }
        return compressFlag;
    }

    /**
     * 获取指定文件夹下的文件 不分类型，即png,jpg都有
     *
     * @param compress_dir_path
     * @return
     */
    public List<String> getExistFiles(String compress_dir_path) {
        List<String> existFileList = new ArrayList<>();
        try {
            existFileList = FileUtil.readfileName(compress_dir_path, null);
            log.info("指定路径下的图片有：" + existFileList.toArray().toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return existFileList;
    }
}
