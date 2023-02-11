package com.bilibili.service.util;

import com.bilibili.exception.ConditionException;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.mysql.cj.util.StringUtils;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FastDFSUtil {
    @Autowired
    private FastFileStorageClient fastFileStorageClient; // 适用于中小文件的上传

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DEFAULT_GROUP = "group1";
    private static final int SLICE_SIZE = 1024 * 1024 * 2;// 2M



    public String getFileType(MultipartFile file){
        if(file == null){
            throw new ConditionException("非法文件！");
        }
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index+1);
    }

    //上传
    public String uploadCommonFile(MultipartFile file) throws Exception {
        Set<MetaData> metaDataSet = new HashSet<>();
        String fileType = this.getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        return storePath.getPath();// 存储路径
    }

    //删除
    public void deleteFile(String filePath){
        fastFileStorageClient.deleteFile(filePath);
    }

    
    private static final String PATH_KEY = "path-key:";
    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key:";
    private static final String  UPLOADED_NO_KEY = "uploaded-no-key:";
    

    // 通过分片来上传文件
    // no：上传的分片是第几片
    
    
    
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        if((file == null || sliceNo == null || totalSliceNo == null) || sliceNo > totalSliceNo) {
            throw new ConditionException("参数异常！");
        }
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
            // 存储到数据库中
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
        //如果所有分片全部上传完毕，则清空redis里面相关的key和value
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if (uploadedNo.equals(totalSliceNo)) {
            resultPath = redisTemplate.opsForValue().get(pathKey);
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            redisTemplate.delete(keyList); // 批量执行key的删除操作
        }
        return resultPath;
    }
    
    // 文件分片实现
    // 理论上由客户端实现；这里方便测试
    public void convertFileToSlices(MultipartFile multipartFile) throws Exception{
        String fileType = this.getFileType(multipartFile);
        //生成临时文件，将MultipartFile转为File
        File file = this.multipartFileToFile(multipartFile);
        long fileLength = file.length();
        int count = 1;
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        for(int i = 0; i < fileLength; i += SLICE_SIZE){
            randomAccessFile.seek(i);// 随机访问文件中的任意位置
            byte[] bytes = new byte[SLICE_SIZE];
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


}
