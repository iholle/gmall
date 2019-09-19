package com.atguigu.gmall.gmalllistservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    JestClient jestClient;
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo){
        Index.Builder builder = new Index.Builder(skuLsInfo);
        builder.index("gmall_sku_info").type("doc").id(skuLsInfo.getId());
        Index index = builder.build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();


        if (skuLsParams.getKeyword()!=null){
            //商品名称检索
            boolQueryBuilder.must(new MatchQueryBuilder("skuName",skuLsParams.getKeyword()));
            //高亮
            searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red'>" ).postTags("</span>"));
        }

        if(skuLsParams.getCatalog3Id()!=null){
            //三级分类检索
            boolQueryBuilder.filter(new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id()));
        }
        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0) {
            //平台属性过滤
            String[] valueIds = skuLsParams.getValueId();

            for (int i = 0; i < valueIds.length; i++) {
                String valueId = valueIds[i];
                boolQueryBuilder.filter(new TermQueryBuilder("skuAttrValueList.valueId", valueId));
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //起始行
        searchSourceBuilder.from((skuLsParams.getPageNo()-1)*skuLsParams.getPageSize());

        //页行数
        searchSourceBuilder.size(skuLsParams.getPageSize());



        //聚合
        TermsBuilder aggsBuilder = AggregationBuilders.terms("groupby_value_id").field("skuAttrValueList.valueId").size(1000);
        searchSourceBuilder.aggregation(aggsBuilder);

        //排序

        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        System.out.println(searchSourceBuilder.toString());

        Search.Builder searchBuilder = new Search.Builder(searchSourceBuilder.toString());

        Search search = searchBuilder.addIndex("gmall_sku_info").addType("doc").build();
        SkuLsResult skuLsResult = new SkuLsResult();
        try {
            SearchResult searchResult = jestClient.execute(search);
            //商品信息列表

            ArrayList<SkuLsInfo> skuLsInfoList = new ArrayList<>();

            List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo skuLsInfo = hit.source;

                    String skuNameHL = hit.highlight.get("skuName").get(0);
                    skuLsInfo.setSkuName(skuNameHL);
                    skuLsInfoList.add(skuLsInfo);

            }
            skuLsResult.setSkuLsInfoList(skuLsInfoList);

            //总数
            Long total = searchResult.getTotal();
            skuLsResult.setTotal(total);

            //总页数 =  （总数+ 每页行数 -1） /每页行数
            long totalPage = (total+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
            skuLsResult.setTotalPages(totalPage);

            //聚合部分 商品设计的平台属性
            ArrayList<String> attrValueIdList = new ArrayList<>();
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_value_id").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                attrValueIdList.add(bucket.getKey());
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);



        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();

        //每次执行在redis中+1

        //设计key     type    String  key sku:101:hotscore value hotscore
        String hotScoreKey = "sku:"+skuId+":hotScoreKey";
        Long hotScore = jedis.incr(hotScoreKey);

        //计数可以被10整出时 更新es
        if(hotScore%10==0){

        }

    }
    //更新es
    public void updateHotScoreEs(String skuId,Long hotScore){
        String updateString="{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":"+hotScore+"\n" +
                "  }\n" +
                "}";
        Update update = new Update.Builder(updateString).index("gmall_sku_info").type("doc").id(skuId).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
