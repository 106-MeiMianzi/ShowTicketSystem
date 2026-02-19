package com.mianzi.showticketsystem.service;

import com.mianzi.showticketsystem.mapper.ShowMapper;
import com.mianzi.showticketsystem.model.entity.Show;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地理位置服务
 * 
 * 作用：
 * - 根据用户IP返回附近的演出
 * - 使用Redis Geospatial存储和查询地理位置
 * 
 * 说明：
 * - 使用Redis的GEO数据结构存储演出的经纬度
 * - 根据用户位置查找附近的演出
 */
@Service
public class GeoLocationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ShowMapper showMapper;

    private static final String GEO_KEY = "shows:geo";

    /**
     * 添加演出的地理位置到Redis
     * 
     * @param showId 演出ID
     * @param longitude 经度
     * @param latitude 纬度
     */
    public void addShowLocation(Long showId, double longitude, double latitude) {
        redisTemplate.opsForGeo().add(GEO_KEY,
                new RedisGeoCommands.GeoLocation<>(
                        String.valueOf(showId),
                        new Point(longitude, latitude)
                ));
    }

    /**
     * 根据用户位置查找附近的演出
     * 
     * @param longitude 用户经度
     * @param latitude 用户纬度
     * @param radiusKm 搜索半径（公里）
     * @return 附近的演出ID列表
     */
    @SuppressWarnings("unchecked")
    public List<Long> findNearbyShows(double longitude, double latitude, double radiusKm) {
        Circle circle = new Circle(new Point(longitude, latitude),
                new Distance(radiusKm, Metrics.KILOMETERS));
        
        // RedisTemplate 的泛型是 Object，所以返回的也是 Object 类型
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                (GeoResults<RedisGeoCommands.GeoLocation<Object>>) redisTemplate.opsForGeo().radius(GEO_KEY, circle);

        if (results == null || results.getContent() == null) {
            return Collections.emptyList();
        }

        return results.getContent().stream()
                .map(geo -> {
                    Object name = geo.getContent().getName();
                    if (name instanceof String) {
                        return Long.valueOf((String) name);
                    }
                    return Long.valueOf(name.toString());
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据IP地址获取地理位置（简化版本）
     * 
     * 注意：实际项目中应该调用第三方IP定位服务（如高德地图、百度地图API）
     * 这里返回北京的默认坐标作为示例
     * 
     * @param ip IP地址
     * @return 经纬度数组 [经度, 纬度]
     */
    public double[] getLocationByIp(String ip) {
        // TODO: 实际项目中应该调用IP定位API
        // 这里简化处理，返回北京的默认坐标
        // 北京天安门：116.397128, 39.916527
        return new double[]{116.397128, 39.916527};
    }

    /**
     * 同步数据库中的演出地理位置到Redis
     * 
     * 注意：实际项目中，演出表应该包含经纬度字段
     * 这里简化处理，根据region字段映射到城市坐标
     */
    public void syncShowLocations() {
        List<Show> shows = showMapper.findAll();
        for (Show show : shows) {
            if (show.getRegion() != null) {
                double[] location = getLocationByRegion(show.getRegion());
                if (location != null) {
                    addShowLocation(show.getId(), location[0], location[1]);
                }
            }
        }
    }

    /**
     * 根据地区名称获取经纬度（简化版本）
     * 
     * 注意：实际项目中应该使用地理编码服务
     * 
     * @param region 地区名称
     * @return 经纬度数组 [经度, 纬度]，如果找不到则返回null
     */
    private double[] getLocationByRegion(String region) {
        // 主要城市的坐标（简化版本）
        switch (region) {
            case "北京":
                return new double[]{116.397128, 39.916527}; // 北京天安门
            case "上海":
                return new double[]{121.473701, 31.230416}; // 上海人民广场
            case "广州":
                return new double[]{113.264385, 23.129112}; // 广州天河
            case "深圳":
                return new double[]{114.057868, 22.543099}; // 深圳福田
            case "杭州":
                return new double[]{120.153576, 30.287459}; // 杭州西湖
            case "成都":
                return new double[]{104.066541, 30.572269}; // 成都天府广场
            case "西安":
                return new double[]{108.940175, 34.341568}; // 西安钟楼
            case "南京":
                return new double[]{118.796877, 32.060255}; // 南京新街口
            default:
                // 默认返回北京坐标
                return new double[]{116.397128, 39.916527};
        }
    }
}
