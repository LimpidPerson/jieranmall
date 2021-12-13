package com.ygnn.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ygnn.common.utils.PageUtils;
import com.ygnn.common.utils.Query;
import com.ygnn.gulimall.product.dao.CategoryDao;
import com.ygnn.gulimall.product.entity.CategoryEntity;
import com.ygnn.gulimall.product.service.CategoryBrandRelationService;
import com.ygnn.gulimall.product.service.CategoryService;
import com.ygnn.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查询所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1、找到所有的一级分类
        return entities.stream().filter(entitie -> entitie.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getChildrens(menu, entities));
                    return menu;
                }).sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
                ).collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到categoryId的完整路径
     * [父/子/孙]
     * [2/25/255]
     *
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @CacheEvict: 失效模式
     * @param category
     */
    // @CacheEvict(value = "category", key = "'getLevel1Categorys'")
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = "category", key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        /**
         * 1、将数据库的多次查询变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //1、查出所有1级分类
        List<CategoryEntity> level1Categorys = getCategoryEntities(selectList, 0L);

        //2、封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类,查到这一级分类的二级分类
            List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
            //2、封装上面的结果
            List<Catelog2Vo> collect = null;
            if (categoryEntities != null) {
                collect = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找到当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3catelog = getCategoryEntities(selectList, l2.getCatId());
                    if (categoryEntities != null) {
                        List<Catelog2Vo.Catalog3VO> collect1 = level3catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Catalog3VO catalog3VO = new Catelog2Vo.Catalog3VO(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3VO;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect1);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return collect;
        }));
        return parent_cid;
    }

    /**
     * TODO 产生对外内存溢出: OutOfDirectMemoryError
     *
     * @return
     */
    // @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        // 给缓存中放入JSON字符串,拿出的JSON字符串,还能逆转为能用的对象类型;【序列化和反序列化】

        /**
         * 1、空结果缓存: 解决缓存穿透
         * 2、设置过期时间(加随机值): 解决缓存雪崩
         * 3、加锁: 解决缓存击穿
         */

        //1、加入缓存逻辑,缓存中存的数据是JSON字符串
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            //2、缓存中没有,查询数据库
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();
            //3、查到的数据再放入缓存中,将对象转为JSON放在缓存中
            stringRedisTemplate.opsForValue().set("catalogJSON", JSON.toJSONString(catalogJsonFromDB), 1, TimeUnit.DAYS);
        }
        //4、转化为我们指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        //1、占分布式锁，去Redis占坑
        RLock lock = redissonClient.getLock("CatalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromBD;
        try {
            // 加锁成功。。。执行业务
            dataFromBD = getDataFromBD();
        } finally {
            lock.unlock();
        }
        return dataFromBD;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {

        //1、占分布式锁，去Redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            Map<String, List<Catelog2Vo>> dataFromBD = null;
            try {
                // 加锁成功。。。执行业务
                dataFromBD = getDataFromBD();
            } finally {
//                if (uuid.equals(stringRedisTemplate.opsForValue().get("lock"))) {
//                // 删除我自己的锁
//                    stringRedisTemplate.delete("lock");
//                }
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromBD;
        } else {
            // 加锁失败。。。重试。
            // 优化: 休眠100ms再重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }
    }

        private Map<String, List<Catelog2Vo>> getDataFromBD() {
            String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
            if (!StringUtils.isEmpty(catalogJSON)) {
                Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
                return result;
            }
            /**
             * 1、将数据库的多次查询变为一次
             */
            List<CategoryEntity> selectList = baseMapper.selectList(null);

            //1、查出所有1级分类
            List<CategoryEntity> level1Categorys = getCategoryEntities(selectList, 0L);

            //2、封装数据
            Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                //1、每一个的一级分类,查到这一级分类的二级分类
                List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
                //2、封装上面的结果
                List<Catelog2Vo> collect = null;
                if (categoryEntities != null) {
                    collect = categoryEntities.stream().map(l2 -> {
                        Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                        //1、找到当前二级分类的三级分类封装成vo
                        List<CategoryEntity> level3catelog = getCategoryEntities(selectList, l2.getCatId());
                        if (categoryEntities != null) {
                            List<Catelog2Vo.Catalog3VO> collect1 = level3catelog.stream().map(l3 -> {
                                //2、封装成指定格式
                                Catelog2Vo.Catalog3VO catalog3VO = new Catelog2Vo.Catalog3VO(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                return catalog3VO;
                            }).collect(Collectors.toList());
                            catelog2Vo.setCatalog3List(collect1);
                        }
                        return catelog2Vo;
                    }).collect(Collectors.toList());
                }
                return collect;
            }));
            stringRedisTemplate.opsForValue().set("catalogJSON", JSON.toJSONString(parent_cid), 1, TimeUnit.DAYS);
            return parent_cid;
        }

        /**
         * 从数据库查询并封装分类数据
         *
         * @Override
         */
        public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock () {
            //TODO 本地锁: synchronized, JUC(lock), 在分布式情况下, 想要锁住所有线程, 必须使用分布式锁
            synchronized (this) {
                // 得到锁之后再去缓存中确认一次，如果还是没有再继续查询
                return getDataFromBD();
            }
        }

        private List<CategoryEntity> getCategoryEntities (List < CategoryEntity > selectList, Long parent_cid){
            // return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            return selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
        }

        /**
         * 225/25/2
         *
         * @param catelogId
         * @param paths
         * @return
         */
        private List<Long> findParentPath (Long catelogId, List < Long > paths){
            // 收集当前节点id
            paths.add(catelogId);
            CategoryEntity byId = this.getById(catelogId);
            if (byId.getParentCid() != 0) {
                findParentPath(byId.getParentCid(), paths);
            }
            return paths;
        }

        private List<CategoryEntity> getChildrens (CategoryEntity root, List < CategoryEntity > all){
            return all.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                    .map(categoryEntity -> {
                        //1、找到子菜单
                        categoryEntity.setChildren(getChildrens(categoryEntity, all));
                        return categoryEntity;
                    }).sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                    .collect(Collectors.toList());
        }

    }