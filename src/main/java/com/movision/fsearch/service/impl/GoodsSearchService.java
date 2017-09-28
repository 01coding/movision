package com.movision.fsearch.service.impl;

import com.movision.fsearch.pojo.ProductGroup;
import com.movision.fsearch.pojo.spec.GoodsSearchSpec;
import com.movision.fsearch.service.IGoodsSearchService;
import com.movision.fsearch.service.IWordService;
import com.movision.fsearch.service.Searcher;
import com.movision.fsearch.service.exception.ServiceException;
import com.movision.fsearch.utils.*;
import com.movision.mybatis.goods.entity.GoodsSearchEntity;
import com.movision.utils.DateUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author zhuangyuhao
 * @Date 2017/3/20 16:02
 */
@Service
public class GoodsSearchService implements IGoodsSearchService {

    @Autowired
    private IWordService wordService;

    @Override
    public Map<String, Object> search(GoodsSearchSpec spec)
            throws ServiceException {
        Map<String, Map<String, Object>> query = new HashMap<String, Map<String, Object>>();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("spec", spec);

        int protype = spec.getProtype();
        if (protype > 0) {
            Searcher.wrapEqualQuery(query, "protype", protype);
        }
        int brandid = spec.getBrandid();
        if (brandid > 0) {
            Searcher.wrapEqualQuery(query, "brandid", brandid);
        }

        //如果搜索的关键词不为空，则入库保存
        // TODO: 2017/9/7 搜素记录插入mongoDB
        /*if (StringUtil.isNotBlank(spec.getQ())) {
            searchGoodsRecordService.add(spec.getQ());
        }*/

        // 向query中添加新的键值对：key=_s
        spec.setQ(StringUtil.emptyToNull(spec.getQ()));
        wrapQuery(spec, query, result);
        //设置排序字段和排序顺序（正序/倒序）
        List<Map<String, Object>> sortFields = this.setSortFields(spec, result);
        /**
         *  发起搜索请求
         *  table：对应fsearch中的movision_product.ini中的name;
         *  query: 表示上面封装的query;
         *  sort: 表示排序字段；
         *  offset: 分页起始值；
         *  limit: 每页数量；
         */
        Map<?, ?> psAsMap = (Map<?, ?>) Searcher.request(
                "search",
                CollectionUtil.arrayAsMap("table", "movision_product",
                        "query", JSONUtil.toJSONString(query),
                        "sort", JSONUtil.toJSONString(sortFields),
                        "offset", spec.getOffset(),
                        "limit", spec.getLimit()));

        //解析搜索的结果, 最终获取分页结果
        List<?> list = (List<?>) psAsMap.get("items");
        Pagination<GoodsSearchEntity, ProductGroup> ps = null;
        if (list.isEmpty()) {
            ps = Pagination.getEmptyInstance();
        } else {

            List<GoodsSearchEntity> products = new ArrayList<GoodsSearchEntity>(list.size());
            for (Object item : list) {
                Map<?, ?> itemAsMap = (Map<?, ?>) item;
                //封装搜索结果中的商品参数：id,name,onlinetime,price
                /**
                 * id,name,subname,attribute, price, origprice, protype, sales, img, onlinetime1
                 */
                GoodsSearchEntity product = new GoodsSearchEntity();
                {
                    //此处处理应该展示的字段
                    product.setId(FormatUtil.parseInteger(itemAsMap.get("id")));
                    product.setName(FormatUtil.parseString(itemAsMap.get("name")));
                    product.setSubname(FormatUtil.parseString(itemAsMap.get("subname")));
                    product.setOnlinetime(DateUtils.str2Date(String.valueOf(itemAsMap.get("onlinetime1")), "yyyyMMddHHmmss"));
//                    product.setOnlinetime(FormatUtil.parseDate(String.valueOf(itemAsMap.get("onlinetime1")), TimeZone.getDefault()));
                    product.setPrice(FormatUtil.parseDouble(itemAsMap.get("price")));
                    product.setOrigprice(FormatUtil.parseDouble(itemAsMap.get("origprice")));
                    product.setAttribute(FormatUtil.parseString(itemAsMap.get("attribute")));

                    product.setProtype(FormatUtil.parseInteger(itemAsMap.get("protype")));
                    product.setProtype_name(FormatUtil.parseString(itemAsMap.get("protype_name")));

                    product.setBrandid(FormatUtil.parseString(itemAsMap.get("brandid")));
                    product.setBrand_CNName(FormatUtil.parseString(itemAsMap.get("brandname")));

                    product.setUrl(FormatUtil.parseString(itemAsMap.get("img_url")));
                    product.setSales(FormatUtil.parseInteger(itemAsMap.get("sales")));

                }
                products.add(product);
            }
            @SuppressWarnings("unchecked")
            List<ProductGroup> productGroups = (List<ProductGroup>) psAsMap.get("groups");

            ps = new Pagination<GoodsSearchEntity, ProductGroup>(products, productGroups,
                    FormatUtil.parseInteger(psAsMap.get("total")),
                    FormatUtil.parseInteger(psAsMap.get("offset")),
                    FormatUtil.parseInteger(psAsMap.get("limit")));

        }
        result.put("ps", ps);
        return result;
    }

    private void wrapQuery(GoodsSearchSpec spec, Map<String, Map<String, Object>> query, Map<String, Object> result) {
        if (spec.getQ() != null) {
            String q = spec.getQ();
            result.put("q", q);
            //分词
            List<String> words = wordService.segWords(q);
            if (!words.isEmpty()) {
                //以空格为分隔符，形成新的list<String>
                String formatQ = StringUtil.join(words, " ");
                query.put("_s", CollectionUtil.arrayAsMap("type", "phrase",
                        "value", formatQ));
            }
        }
    }

    /**
     * 设置排序字段和排序顺序（正序/倒序）
     *
     * @param spec
     * @param result
     * @return
     */
    private List<Map<String, Object>> setSortFields(GoodsSearchSpec spec, Map<String, Object> result) {
        List<Map<String, Object>> sortFields = new ArrayList<Map<String, Object>>(1);
        Map<String, Object> sortField = new HashMap<String, Object>(3);

        if (StringUtil.isNotEmpty(spec.getSort())) {

            String sort = spec.getSort();
            result.put("sort", spec.getSort());
            //这里设置是正序，还是倒序
            String sortorder = "true";
            if (StringUtil.isNotEmpty(spec.getSortorder())) {
                sortorder = spec.getSortorder();
                result.put("sortorder", spec.getSortorder());
            }
            /**
             * 若指定的排序字段sort不为空，那么就按照指定的排序字段排序，
             */
            if (sort.equals("price1")) {
                sortField.put("field", sort);
                sortField.put("type", "FLOAT");
                sortField.put("reverse",
                        FormatUtil.parseBoolean(sortorder));

            } else if (sort.equals("onlinetime1")) {
                sortField.put("field", sort);
                sortField.put("type", "LONG");
                sortField.put("reverse",
                        FormatUtil.parseBoolean(sortorder));

            } else {
                sortField.put("field", "id");
                sortField.put("type", "INT");
                sortField.put("reverse", FormatUtil.parseBoolean(true));
            }

        } else {
            //默认以id排序
            sortField.put("field", "id");
            sortField.put("type", "INT");
            sortField.put("reverse", FormatUtil.parseBoolean(true));
        }

        sortFields.add(sortField);
        return sortFields;
    }

    /**
     * 获取商品热门搜索词和搜索历史记录
     *
     * @return
     */
    public Map<String, Object> getHotwordAndHistory() {

        Map map = new HashedMap();

        // TODO: 2017/9/7 热门词，查询mongoDB，按照频次排序
//        map.put("hotWordList", searchGoodsRecordService.selectPostSearchHotWord());

        // TODO: 2017/9/7 历史搜索记录，查询mongoDB 
//        map.put("historyList", searchGoodsRecordService.selectHistoryRecord(ShiroUtil.getAppUserID()));

        return map;
    }


    public Integer updateSearchIsdel(Integer userid) {
        // TODO: 2017/9/7 清除商品搜索记录，修改mongoDB中的数据
//        return
        return 0;
    }

}
