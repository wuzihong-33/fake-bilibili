package com.bilibili.service.util;

import com.bilibili.exception.ConditionException;
import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.mysql.cj.util.StringUtils;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.*;

@Component
public class FastDFSUtil {
    @Autowired
    private FastFileStorageClient fastFileStorageClient; // 适用于中小文件的上传

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DEFAULT_GROUP = "group1";
    private static final int DEFAULT_SLICE_SIZE = 1024 * 1024 * 2;// 2M
    private static final String PATH_KEY = "path-key:";
    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key:";
    private static final String  UPLOADED_NO_KEY = "uploaded-no-key:";

//    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;
    
    /**
     * 上传文件
     */
    public String uploadCommonFile(MultipartFile file) throws Exception {
        Set<MetaData> metaDataSet = new HashSet<>();
        String fileType = this.getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        return storePath.getPath();// 存储路径
    }

    /**
     * 以分片的形式上传文件
     * @param file
     * @param fileMd5 md5
     * @param sliceNo 第几块分片
     * @param totalSliceNo 总的分片数
     * @return
     * @throws Exception
     */
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        String pathKey = PATH_KEY + fileMd5; // 文件的存储路径
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5; // 偏移量是通过已上传的文件大小来区分
        String uploadedNoKey = UPLOADED_NO_KEY + fileMd5; // 已经上传了多少个分配，用于结束上传流程
        
        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = 0L;
        if (!StringUtils.isNullOrEmpty(uploadedSizeStr)) {
            uploadedSize = Long.valueOf(uploadedSizeStr);
        }
        if (sliceNo == 1) {
            // 第一个分配需要调用upload，后续才能调用modify；开发中一般使用modify，避免重复上传
            String filePath = this.uploadCommonFile(file);
            if(StringUtil.isNullOrEmpty(filePath)){
                throw new ConditionException("上传失败！");
            }
            redisTemplate.opsForValue().set(pathKey, filePath);
            redisTemplate.opsForValue().set(uploadedNoKey, "1");
        } else {
            String filePath = redisTemplate.opsForValue().get(pathKey);
            if (StringUtil.isNullOrEmpty(filePath)) {
                throw new ConditionException("上传失败！");
            }
            this.modifyAppenderFile(file, filePath, uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        // 修改历史上传分片文件大小
        uploadedSize  += file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize));
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if (uploadedNo.equals(totalSliceNo)) {
            //如果所有分片全部上传完毕，则清空redis里面相关的key和value
            resultPath = redisTemplate.opsForValue().get(pathKey);
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            redisTemplate.delete(keyList); // 批量执行key的删除操作
        }
        return resultPath;
    }


    /**
     * 删除文件
     */
    public void deleteFile(String filePath){
        fastFileStorageClient.deleteFile(filePath);
    }

    
    
    /**
     * 学习使用
     * 将大文件切成小片，实现文件的分配存储
     * 理论上由客户端实现，这里为了测试和学习
     * @param multipartFile
     * @throws Exception
     */
    public void convertFileToSlicesAndStore(MultipartFile multipartFile) throws Exception{
        String fileType = this.getFileType(multipartFile);
        //生成临时文件，将MultipartFile转为File
        File file = this.multipartFileToFile(multipartFile);
        long fileLength = file.length();
        int count = 1;
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        for(int i = 0; i < fileLength; i += DEFAULT_SLICE_SIZE){
            randomAccessFile.seek(i);// 随机访问文件中的任意位置
            byte[] bytes = new byte[DEFAULT_SLICE_SIZE];
            int len = randomAccessFile.read(bytes);
            String sliceStorePath = "C:\\Users\\123123\\tmpFile\\" + count + "." + fileType; 
            File slice = new File(sliceStorePath);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes, 0, len);
            fos.close();
            count++;
        }
        randomAccessFile.close();
        file.delete();
    }

    
    private File multipartFileToFile(MultipartFile multipartFile) throws Exception{
        String originalFileName = multipartFile.getOriginalFilename();
        String[] fileName = originalFileName.split("\\.");
        File file = File.createTempFile(fileName[0], "." + fileName[1]);
        multipartFile.transferTo(file);
        return file;
    }
    

    private String uploadAppenderFile(MultipartFile file) throws Exception{
        String fileType = this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    // 从offset开始去append文件
    private void modifyAppenderFile(MultipartFile file, String filePath, long offset) throws Exception{
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }

    
    public String getFileType(MultipartFile file){
        if(file == null){
            throw new ConditionException("非法文件！");
        }
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index+1);
    }

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String path) throws Exception {
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path);
        long totalFileSize = fileInfo.getFileSize();
        String url = httpFdfsStorageAddr + path; // 文件在文件服务器上的路径
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        // 将Enumeration<String> 转换成 Map<String, Object>
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        // 截取分片的起始字节和结束字节位置
        String rangeStr = request.getHeader("Range"); // 由浏览器自动实现切分
        String[] range;
        if (StringUtil.isNullOrEmpty(rangeStr)) {
            rangeStr = "bytes=0-" + (totalFileSize-1);
        }
        range = rangeStr.split("bytes=|-");
        long begin = 0;
        if(range.length >= 2){
            begin = Long.parseLong(range[1]);
        }
        long end = totalFileSize-1;
        if(range.length >= 3){
            end = Long.parseLong(range[2]);
        }
        long len = (end - begin) + 1;
        // 格式：Content-Range: bytes 2893548-3091400/9709197
        String contentRange = "bytes " + begin + "-" + end + "/" + totalFileSize;
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int) len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        // 视频内容以流的形式存储在响应包里边
        HttpUtil.get(url, headers, response);
    }
    

//    public static void main(String[] args) {
//        String range = "bytes=2893548-3091400";
//        String[] ran = range.split("bytes=|-");
//        System.out.println(Arrays.toString(ran)); // [, 2893548, 3091400]
//    }
}
