package com.atguigu.gmall.gmallmanageweb;

import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void testUpload() throws Exception {
        // 使用全局对象加载配置文件
        ClientGlobal.init("tracker.conf");
        // 创建 TrackerClient 对象，直接 new 就可以了
        TrackerClient trackerClient = new TrackerClient();
        // 通过 TrackerClient 获得一个 TrackerServer 对象
        TrackerServer trackerServer = trackerClient.getConnection();
        // 创建一个 StorageServer 的引用，可以是 null
        StorageServer storageServer = null;
        // 创建一个 StorageClient，参数需要 TrackerServer，StorageServer。相当于拿到服务器的 IP 和端口号
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        // 使用 StorageClient 上传文件，返回保存信息。upload_file 方法中的三个参数，分别是文件路径，文件后缀名以及元数据
        String[] strings = storageClient.upload_file("D:\\1.jpg", "jpg", null);
        // 打印结果
        for (String string : strings) {
            System.out.println(string);
        }
    }

}
