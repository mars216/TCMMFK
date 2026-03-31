package com.example.springboot.controller;

import com.example.springboot.common.AuthAccess;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.server.HttpServerResponse;
import com.example.springboot.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${ip:localhost}")
    String ip;
    @Value("${server.port}")
    String port;

    private static final String ROOT_PATH = System.getProperty("user.dir") + File.separator + "files";  // D:\B站\小白做毕设2024\代码\小白做毕设2024\files

    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();  // Original file name
        // aaa.png
        String mainName = FileUtil.mainName(originalFilename);  // aaa
        String extName = FileUtil.extName(originalFilename);   // png

        if (!FileUtil.exist(ROOT_PATH)) {
            FileUtil.mkdir(ROOT_PATH);  // Create parent directory if it doesn't exist
        }

        if (FileUtil.exist(ROOT_PATH + File.separator + originalFilename)) {
            // If file already exists, rename with timestamp
            originalFilename = System.currentTimeMillis() + "_" + mainName + "." + extName;
        }

        File saveFile = new File(ROOT_PATH + File.separator + originalFilename);
        file.transferTo(saveFile);  // Save file to local disk

        String url = "http://" + ip + ":" + port + "/file/download/" + originalFilename;
        return Result.success(url);  // Return file download link
    }

    @AuthAccess
    @GetMapping("download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        String filePath = ROOT_PATH + File.separator + fileName;
        if (!FileUtil.exist(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("File not found");
            return;
        }
        byte[] bytes = FileUtil.readBytes(filePath);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(bytes);  // bytes array contains file content
        outputStream.flush();
        outputStream.close();
    }
}