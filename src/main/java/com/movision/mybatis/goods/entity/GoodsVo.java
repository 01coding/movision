package com.movision.mybatis.goods.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author shuxf
 * @Date 2017/2/13 11:35
 */
public class GoodsVo implements Serializable {
    private Integer id;//商品编号

    private String name;//商品名称

    private String subname;

    private Double price;//商品折后价

    private Double origprice;//商品原价

    private Double comprice;//套餐原价

    private String attribute;//商品标签

    private String provincecode;

    private String citycode;

    private Integer isself;

    private Integer collect;//收藏

    private Integer isdamage;

    private Integer isquality;

    private String promotions;

    private Integer protype;//商品分类

    private String brandid;

    private String brandsupple;

    private Integer sales;//总销量

    private Integer stock;//库存

    private Integer goodsposition;

    private Integer assess;//评价

    private Integer shopid;

    private Integer iscombo;

    private Integer isspecial;//是否为特卖商品：0 否 1 是

    private Date specialendtime;//特卖截止时间

    private Integer isend;//特卖是否结束：0 未结束 1 已结束

    private Integer isseckill;

    private Integer ishomepage;//是否为推荐：0 否 1 是

    private Date mintime;

    private Date maxtime;

    private Date recommenddate;//推荐日期

    private String typename;//类别名

    private Date onlinetime;

    private Integer isdel;//商品状态

    private Integer ishot;//是否为热门：0 否 1 是

    private Integer isessence;//是否为精选：0 否 1 是

    private String imgurl;//商品缩略小方图

    private String brandname;//品牌名称

    public Double getComprice() {
        return comprice;
    }

    public void setComprice(Double comprice) {
        this.comprice = comprice;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Integer getCollect() {
        return collect;
    }

    public void setCollect(Integer collect) {
        this.collect = collect;
    }

    public Integer getAssess() {
        return assess;
    }

    public void setAssess(Integer assess) {
        this.assess = assess;
    }

    public Date getMintime() {
        return mintime;
    }

    public void setMintime(Date mintime) {
        this.mintime = mintime;
    }

    public Date getMaxtime() {
        return maxtime;
    }

    public void setMaxtime(Date maxtime) {
        this.maxtime = maxtime;
    }

    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getSubname() {
        return subname;
    }

    public void setSubname(String subname) {
        this.subname = subname == null ? null : subname.trim();
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getOrigprice() {
        return origprice;
    }

    public void setOrigprice(Double origprice) {
        this.origprice = origprice;
    }

    public String getProvincecode() {
        return provincecode;
    }

    public void setProvincecode(String provincecode) {
        this.provincecode = provincecode == null ? null : provincecode.trim();
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode == null ? null : citycode.trim();
    }

    public Integer getIsself() {
        return isself;
    }

    public void setIsself(Integer isself) {
        this.isself = isself;
    }

    public Integer getIsdamage() {
        return isdamage;
    }

    public void setIsdamage(Integer isdamage) {
        this.isdamage = isdamage;
    }

    public Integer getIsquality() {
        return isquality;
    }

    public void setIsquality(Integer isquality) {
        this.isquality = isquality;
    }

    public String getPromotions() {
        return promotions;
    }

    public void setPromotions(String promotions) {
        this.promotions = promotions == null ? null : promotions.trim();
    }

    public Integer getProtype() {
        return protype;
    }

    public void setProtype(Integer protype) {
        this.protype = protype;
    }

    public String getBrandid() {
        return brandid;
    }

    public void setBrandid(String brandid) {
        this.brandid = brandid == null ? null : brandid.trim();
    }

    public String getBrandsupple() {
        return brandsupple;
    }

    public void setBrandsupple(String brandsupple) {
        this.brandsupple = brandsupple == null ? null : brandsupple.trim();
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getGoodsposition() {
        return goodsposition;
    }

    public void setGoodsposition(Integer goodsposition) {
        this.goodsposition = goodsposition;
    }

    public Integer getIscombo() {
        return iscombo;
    }

    public void setIscombo(Integer iscombo) {
        this.iscombo = iscombo;
    }

    public Integer getIsspecial() {
        return isspecial;
    }

    public Date getSpecialendtime() {
        return specialendtime;
    }

    public void setSpecialendtime(Date specialendtime) {
        this.specialendtime = specialendtime;
    }

    public Integer getIsend() {
        return isend;
    }

    public void setIsend(Integer isend) {
        this.isend = isend;
    }

    public void setIsspecial(Integer isspecial) {
        this.isspecial = isspecial;
    }

    public Integer getIsseckill() {
        return isseckill;
    }

    public void setIsseckill(Integer isseckill) {
        this.isseckill = isseckill;
    }

    public Integer getIshomepage() {
        return ishomepage;
    }

    public void setIshomepage(Integer ishomepage) {
        this.ishomepage = ishomepage;
    }

    public Date getOnlinetime() {
        return onlinetime;
    }

    public void setOnlinetime(Date onlinetime) {
        this.onlinetime = onlinetime;
    }

    public Integer getIsdel() {
        return isdel;
    }

    public void setIsdel(Integer isdel) {
        this.isdel = isdel;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public Date getRecommenddate() {
        return recommenddate;
    }

    public void setRecommenddate(Date recommenddate) {
        this.recommenddate = recommenddate;
    }

    public Integer getIshot() {
        return ishot;
    }

    public void setIshot(Integer ishot) {
        this.ishot = ishot;
    }

    public Integer getIsessence() {
        return isessence;
    }

    public void setIsessence(Integer isessence) {
        this.isessence = isessence;
    }

    public Integer getShopid() {
        return shopid;
    }

    public void setShopid(Integer shopid) {
        this.shopid = shopid;
    }
}
