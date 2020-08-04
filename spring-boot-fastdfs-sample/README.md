# 为什么使用分布式存储系统

单点存储系统依赖于web中间件，如：tomcat，nginx等。所以会有如下问题：

- 单向存储
- 不支持集群
- 文件数据冗余
- 可扩展性差

# 什么是fastDFS

FastDFS是一个开源的轻量级分布式文件系统，它对文件进行管理，功能包括：文件存储、文件同步、文件访问（文件上传、文件下载）等，解决了大容量存储和负载均衡的问题。特别适合以文件为载体的在线服务，如相册网站、视频网站等等。

**简介**

FastDFS服务端有两个角色：跟踪器（tracker）和存储节点（storage）。跟踪器主要做调度工作，在访问上起负载均衡的作用。

存储节点存储文件，完成文件管理的所有功能：就是这样的存储、同步和提供存取接口，FastDFS同时对文件的metadata进行管理。所谓文件的meta data就是文件的相关属性，以键值对（key value）方式表示，如：width=1024，其中的key为width，value为1024。文件metadata是文件属性列表，可以包含多个键值对。

跟踪器和存储节点都可以由一台或多台服务器构成。跟踪器和存储节点中的服务器均可以随时增加或下线而不会影响线上服务。其中跟踪器中的所有服务器都是对等的，可以根据服务器的压力情况随时增加或减少。

为了支持大容量，存储节点（服务器）采用了分卷（或分组）的组织方式。存储系统由一个或多个卷组成，卷与卷之间的文件是相互独立的，所有卷的文件容量累加就是整个存储系统中的文件容量。一个卷可以由一台或多台存储服务器组成，一个卷下的存储服务器中的文件都是相同的，卷中的多台存储服务器起到了冗余备份和负载均衡的作用。

在卷中增加服务器时，同步已有的文件由系统自动完成，同步完成后，系统自动将新增服务器切换到线上提供服务。

当存储空间不足或即将耗尽时，可以动态添加卷。只需要增加一台或多台服务器，并将它们配置为一个新的卷，这样就扩大了存储系统的容量。

FastDFS中的文件标识分为两个部分：卷名和文件名，二者缺一不可。

# 安装

## 使用的系统软件

| 名称                 | 说明                          |
| -------------------- | ----------------------------- |
| centos               | 7.x                           |
| libfastcommon        | FastDFS分离出的一些公用函数包 |
| FastDFS              | FastDFS本体                   |
| fastdfs-nginx-module | FastDFS和nginx的关联模块      |
| nginx                | nginx1.15.4                   |

准备测试机器

- tracker节点
  - 192.168.2.56
- storaged节点
  - 192.168.2.57
  - 需要准备nginx

## 编译环境

同时在tracker节点、storaged节点执行。

```
yum install git gcc gcc-c++ make automake autoconf libtool pcre pcre-devel zlib zlib-devel openssl-devel wget vim -y
```

## 安装libfastcommon

同时在tracker节点、storaged节点执行。

```
# 没用git则直接下载tar包，解压安装
git clone https://github.com/happyfish100/libfastcommon.git --depth 1
cd libfastcommon/
./make.sh && ./make.sh install #编译安装
```

## 安装FastDFS

同时在tracker节点、storaged节点执行

```
cd ../ #返回上一级目录
# 没用git则直接下载tar包，解压安装
git clone https://github.com/happyfish100/fastdfs.git --depth 1
cd fastdfs/
./make.sh && ./make.sh install #编译安装
#配置文件准备
cp conf/* /etc/fdfs/
```

## 配置nginx

在storaged节点执行。

1.解压`fastdfs-nginx-module`模块

```
tar -zxvf fastdfs-nginx-module-1.22
```

2.在原先的nginx上安装新模块

```
# 查看原来nginx的版本，然后需要下载一个相同的版本执行编译操作
nginx -V
# 添加模块
./configure --add-module=/home/ys2/fastdfs/fastdfs-nginx-module-1.22/src
# 编译
make
# 备份nginx
mv /usr/local/nginx/sbin/nginx /usr/local/nginx/sbin/nginx.bak
# 复制objs下的nginx到/usr/local/nginx/sbin下
cp -y /home/nginx-1.18.0/objs/nginx /usr/local/nginx/sbin/nginx
```

> 注意！不要执行make install操作

2.复制配置文件

```
cp /home/ys2/fastdfs/fastdfs-nginx-module-1.22/src/mod_fastdfs.conf /etc/fdfs
```

## 单机部署

1.tracker节点和storage节点都执行

```
#将配置文件复制/etc/fdfs目录下
cp /home/fastdfs-6.06/conf/* /etc/fdfs
```

### tracker配置

1.创建目录

```
mkdir -p /usr/local/fastdfs/tracker
```

2.修改配置文件

```
vim /etc/fdfs/tracker.conf
#需要修改的内容如下
port=22122  # tracker服务器端口（默认22122,一般不修改）
base_path = /usr/local/fastdfs/tracker  # 存储日志和数据的根目录
```

3.启动tracker

```
/usr/bin/fdfs_trackerd /etc/fdfs/tracker.conf
```

4.检查

```
ps -ef |grep tracker
```

### storage配置

1.创建目录

```
mkdir -p /usr/local/fastdfs/tmp /usr/local/fastdfs/storage
```

2.修改`storage.conf`

```
vim /etc/fdfs/storage.conf
#需要修改的内容如下
port=23000  # storage服务端口（默认23000,一般不修改）
base_path = /usr/local/fastdfs/storage  # 数据和日志文件存储根目录
store_path0 = /usr/local/fastdfs/storage  # 第一个存储目录
tracker_server=192.168.2.56:22122  # tracker服务器IP和端口
http.server_port=8888  # http访问文件的端口(默认8888,看情况修改,和nginx中保持一致)
```

3.修改`mod_fastdfs.conf`

```
# 日志存放目录
base_path=/usr/local/fastdfs/tmp
# tracker服务器IP和端口
tracker_server=192.168.2.56:22122
# url包含组名
url_have_group_name = true
# 第一个存储目录，和storage.conf保持一致
store_path0=/usr/local/fastdfs/storage
```

4.启动

```
/usr/bin/fdfs_storaged /etc/fdfs/storage.conf
```

5.检查

```
ps -ef |grep storage
```

### client测试

client在storage节点操作。

1.创建目录

```
mkdir -p /usr/local/fastdfs/client
```

2.修改`client.conf`

```
vim /etc/fdfs/client.conf
#需要修改的内容如下
base_path=/usr/local/fastdfs/client
tracker_server=192.168.2.56:22122    #tracker服务器IP和端口
#保存后测试,返回ID表示成功 如：group1/M00/00/00/anti-steal.jpg
/usr/bin/fdfs_test /etc/fdfs/client.conf upload /etc/fdfs/anti-steal.jpg
```

### 配置nginx访问

```
#配置nginx.config
vim /usr/local/nginx/conf/nginx.conf
#添加如下配置
server {
    listen       8888;    ## 该端口为storage.conf中的http.server_port相同
    server_name  localhost;
    location ~/group[0-9]/ {
        ngx_fastdfs_module;
    }
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
    root   html;
    }
}
#测试下载，用外部浏览器访问刚才已传过的nginx安装包,引用返回的ID
http://192.168.2.56:8888/group1/M00/00/00/wKgAQ1pysxmAaqhAAA76tz-dVgg.jpg
#弹出下载单机部署全部跑通
```

# springboot集成

1.引入maven依赖

```
        <dependency>
            <groupId>com.github.tobato</groupId>
            <artifactId>fastdfs-client</artifactId>
            <version>1.27.2</version>
        </dependency>
```

2.`UploadService`代码

```java
@Service
public class UploadServiceImpl implements UploadService {
    @Autowired
    private FastFileStorageClient fastClient;

    @Override
    public String upload(MultipartFile file, String fileExtName) throws Exception {
        StorePath storePath = fastClient.uploadFile(file.getInputStream(), file.getSize(), fileExtName, null);
        return storePath.getFullPath();
    }

    @Override
    public byte[] download(String groupName, String path) {
        return fastClient.downloadFile(groupName, path, new DownloadByteArray());
    }

    @Override
    public void deleteFile(String groupName, String path) {
        fastClient.deleteFile(groupName, path);
    }

}
```

3.`UploadController`代码

```java
@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @GetMapping("hello")
    public String hello() {
        return "hello,world";
    }

    @PostMapping("upload")
    public String upload(MultipartFile file) throws Exception {
        if (file != null) {
            String filename = file.getOriginalFilename();
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            if (!suffix.equalsIgnoreCase("jpg") &&
                    !suffix.equalsIgnoreCase("png") &&
                    !suffix.equalsIgnoreCase("jpeg")
            ) {
                return "图片格式不正确";
            }
            String uploadPath = uploadService.upload(file, suffix);
            System.out.println("uploadPath = " + uploadPath);
            return uploadPath;
        }
        return "文件不能为空";
    }

    /**
     * 下载文件
     *
     * @param groupName 组名
     * @param path      fastDFS的文件相对路径
     * @param suffix    下载文件的后缀名
     * @param response
     * @throws Exception
     */
    @GetMapping("download")
    public void download(String groupName,
                         String path,
                         String suffix,
                         HttpServletResponse response) throws Exception {
        String fileName = UUID.randomUUID().toString() + "." + suffix;
        byte[] bytes1 = uploadService.download(groupName, path);
        // 设置响应头，控制浏览器下载该文件
        response.setHeader("content-disposition", "attachment;filename="
                + URLEncoder.encode(fileName, "UTF-8"));
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Connection", "close");
        response.setHeader("Accept-Ranges", "bytes");
        try (ServletOutputStream os = response.getOutputStream()) {
            os.write(bytes1);
        }
    }

    /**
     * 删除文件
     *
     * @param groupName 组名
     * @param path      fastDFS的文件相对路径
     */
    @GetMapping("delete")
    public void deleteFile(String groupName, String path) {
        uploadService.deleteFile(groupName, path);
    }
}
```

