package com.ygnn.gulimall.product;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ygnn.gulimall.product.dao.AttrGroupDao;
import com.ygnn.gulimall.product.entity.BrandEntity;
import com.ygnn.gulimall.product.service.BrandService;
import com.ygnn.gulimall.product.service.CategoryService;
import com.ygnn.gulimall.product.service.SkuSaleAttrValueService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    private static BrandService brandService;

    @Autowired
    private OSSClient ossClient;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    void contextLoads() {
    }

    @Test
    void testSkuSaleAttrValueService(){
        System.out.println("testSkuSaleAttrValueService-Result --> " + skuSaleAttrValueService.getSaleAttrsBySpuId(4l));
    }

    @Test
    void testAttrGroupDao(){
        System.out.println("testAttrGroupDao-Result --> " + attrGroupDao.getAttrGroupWithAttrsBySpuId(4l, 225l));
    }

    @Test
    void testRedissonClient(){
        System.out.println(redissonClient);
    }

    @Test
    void testStringRedisTemplate(){
        // 保存
        stringRedisTemplate.opsForValue().set("hello", "world" + UUID.randomUUID());

        //查询
        System.out.println("之前保存的数据是: --> " + stringRedisTemplate.opsForValue().get("hello"));
    }

    @Test
    void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.error("完整路径: --> " + Arrays.asList(catelogPath));
    }

    @Test
    void insertTest(){
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("meta");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

    @Test
    void updateTest(){
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1l);
        brandEntity.setDescript("yyds");
        brandService.updateById(brandEntity);
        System.out.println("更新成功");
    }

    @Test
    void queryListTest(){
        brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1l)).forEach((s) -> System.out.println(s));
        System.out.println("查询成功");
    }

    @Test
    void testUpload(){
//        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//        String endpoint = "oss-cn-shenzhen.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "LTAI5tPKyRnZR4PoVqdrAwUD";
//        String accessKeySecret = "rPRbkAMAmDfULqOIYRcU5RkMWI2LIy";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 创建PutObjectRequest对象。
        // 依次填写Bucket名称（例如examplebucket）、Object完整路径（例如exampledir/exampleobject.txt）和本地文件的完整路径。Object完整路径中不能包含Bucket名称。
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
        PutObjectRequest putObjectRequest = new PutObjectRequest("gulimall-1008", "微信图片_20181009081055.jpg", new File("C:\\Users\\FangKun\\Pictures\\Saved Pictures\\微信图片_20181009081054.jpg"));

        // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // metadata.setObjectAcl(CannedAccessControlList.Private);
        // putObjectRequest.setMetadata(metadata);

        // 上传文件。
        ossClient.putObject(putObjectRequest);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传成功!!!");
    }

}
