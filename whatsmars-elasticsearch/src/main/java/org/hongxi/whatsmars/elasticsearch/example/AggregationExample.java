package org.hongxi.whatsmars.elasticsearch.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationRange;
import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.hongxi.whatsmars.elasticsearch.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 聚合查询（Aggregation）示例：Terms 聚合、Avg/Sum/Min/Max 聚合、Range 聚合、嵌套聚合
 */
@Component
public class AggregationExample {

    private static final Logger log = LoggerFactory.getLogger(AggregationExample.class);

    private static final String INDEX_NAME = "product";

    private final ElasticsearchClient client;

    public AggregationExample(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * 运行所有聚合查询示例
     */
    public void runAll() throws IOException {
        log.info("========== 聚合查询示例 ==========");

        // 刷新索引以确保最新文档可搜索
        client.indices().refresh(r -> r.index(INDEX_NAME));

        termsAggregation();
        metricAggregations();
        rangeAggregation();
        nestedAggregation();
        filterAggregation();

        log.info("========== 聚合查询示例结束 ==========\n");
    }

    /**
     * Terms 聚合：按 category 分组统计各分类的文档数量
     */
    public void termsAggregation() throws IOException {
        log.info("--- Terms 聚合：按 category 分组 ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .size(0) // 不需要返回文档，只要聚合结果
                        .aggregations("by_category", a -> a
                                .terms(t -> t
                                        .field("category")
                                        .size(10)
                                )
                        ),
                Product.class
        );

        Aggregate termsAgg = response.aggregations().get("by_category");
        log.info("  按 category 分组结果:");
        for (StringTermsBucket bucket : termsAgg.sterms().buckets().array()) {
            log.info("    - {}: {} 条", bucket.key().stringValue(), bucket.docCount());
        }
    }

    /**
     * 指标聚合：计算 price 的平均值、总和、最小值、最大值
     */
    public void metricAggregations() throws IOException {
        log.info("--- 指标聚合：price 的 avg/sum/min/max ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .size(0)
                        .aggregations("avg_price", a -> a.avg(avg -> avg.field("price")))
                        .aggregations("sum_price", a -> a.sum(sum -> sum.field("price")))
                        .aggregations("min_price", a -> a.min(min -> min.field("price")))
                        .aggregations("max_price", a -> a.max(max -> max.field("price")))
                        .aggregations("total_count", a -> a.valueCount(vc -> vc.field("price"))),
                Product.class
        );

        Map<String, Aggregate> aggs = response.aggregations();
        log.info("  平均价格: {}", aggs.get("avg_price").avg().value());
        log.info("  价格总和: {}", aggs.get("sum_price").sum().value());
        log.info("  最低价格: {}", aggs.get("min_price").min().value());
        log.info("  最高价格: {}", aggs.get("max_price").max().value());
        log.info("  文档总数: {}", aggs.get("total_count").valueCount().value());
    }

    /**
     * Range 聚合：按价格区间分组统计
     * 区间：[0, 1000), [1000, 5000), [5000, +∞)
     */
    public void rangeAggregation() throws IOException {
        log.info("--- Range 聚合：按价格区间分组 ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .size(0)
                        .aggregations("price_ranges", a -> a
                                .range(r -> r
                                        .field("price")
                                        .ranges(
                                                AggregationRange.of(r1 -> r1.key("低价").to(1000.0)),
                                                AggregationRange.of(r2 -> r2.key("中价").from(1000.0).to(5000.0)),
                                                AggregationRange.of(r3 -> r3.key("高价").from(5000.0))
                                        )
                                )
                        ),
                Product.class
        );

        Aggregate rangeAgg = response.aggregations().get("price_ranges");
        log.info("  按价格区间分组结果:");
        for (RangeBucket bucket : rangeAgg.range().buckets().array()) {
            log.info("    - {} ({} <= price < {}): {} 条",
                    bucket.key(),
                    bucket.from(), bucket.to(),
                    bucket.docCount());
        }
    }

    /**
     * 嵌套聚合：先按 category 分组，再计算每组的平均价格和最高价格
     */
    public void nestedAggregation() throws IOException {
        log.info("--- 嵌套聚合：按 category 分组，再算每组的 avg_price 和 max_price ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .size(0)
                        .aggregations("by_category", a -> a
                                .terms(t -> t.field("category"))
                                .aggregations("avg_price", sub -> sub.avg(avg -> avg.field("price")))
                                .aggregations("max_price", sub -> sub.max(max -> max.field("price")))
                        ),
                Product.class
        );

        Aggregate termsAgg = response.aggregations().get("by_category");
        log.info("  嵌套聚合结果:");
        for (StringTermsBucket bucket : termsAgg.sterms().buckets().array()) {
            double avgPrice = bucket.aggregations().get("avg_price").avg().value();
            double maxPrice = bucket.aggregations().get("max_price").max().value();
            log.info("    - {} ({}条): 平均价格={}, 最高价格={}",
                    bucket.key().stringValue(), bucket.docCount(), avgPrice, maxPrice);
        }
    }

    /**
     * Filter 聚合：对满足特定条件的子集进行聚合
     * 示例：只对 Electronics 分类的文档计算价格统计
     */
    public void filterAggregation() throws IOException {
        log.info("--- Filter 聚合：只对 Electronics 分类统计价格 ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .size(0)
                        .aggregations("electronics_only", a -> a
                                .filter(f -> f.term(t -> t.field("category").value("Electronics")))
                                .aggregations("avg_price", sub -> sub.avg(avg -> avg.field("price")))
                                .aggregations("min_price", sub -> sub.min(min -> min.field("price")))
                                .aggregations("max_price", sub -> sub.max(max -> max.field("price")))
                        ),
                Product.class
        );

        Aggregate filterAgg = response.aggregations().get("electronics_only");
        log.info("  Electronics 分类统计:");
        log.info("    文档数: {}", filterAgg.filter().docCount());
        log.info("    平均价格: {}", filterAgg.filter().aggregations().get("avg_price").avg().value());
        log.info("    最低价格: {}", filterAgg.filter().aggregations().get("min_price").min().value());
        log.info("    最高价格: {}", filterAgg.filter().aggregations().get("max_price").max().value());
    }
}
