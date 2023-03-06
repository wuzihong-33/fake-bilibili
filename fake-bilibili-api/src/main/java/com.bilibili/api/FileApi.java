package com.bilibili.api;

import com.bilibili.domain.JsonResponse;
import com.bilibili.exception.ConditionException;
import com.bilibili.service.FileService;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class FileApi {
    @Autowired
    private FileService fileService;
    
    @PostMapping("/md5files")
    public JsonResponse<String> getFileMD5(MultipartFile file) throws Exception {
        if (file == null) {
            throw new ConditionException("bad request");
        }
        String fileMD5 = fileService.getFileMD5(file);
        return new JsonResponse<>(fileMD5);
    }

    /**
     * 以切片的形式上传文件
     * @param slice
     * @param fileMd5
     * @param sliceNo
     * @param totalSliceNo
     * @return
     * @throws Exception
     */
    @PutMapping("/file-slices")
    public JsonResponse<String> uploadFileBySlices(MultipartFile slice,
                                                   String fileMd5,
                                                   Integer sliceNo,
                                                   Integer totalSliceNo) throws Exception {
        if (slice == null || StringUtil.isNullOrEmpty(fileMd5) || sliceNo == null || totalSliceNo == null) {
            throw new ConditionException("bad request");
        }
        String filePath = fileService.uploadFileBySlices(slice, fileMd5, sliceNo, totalSliceNo);
        return new JsonResponse<>(filePath);
    }
}
