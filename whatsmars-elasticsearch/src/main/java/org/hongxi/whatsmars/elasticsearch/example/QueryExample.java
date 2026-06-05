package org.hongxi.whatsmars.elasticsearch.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.hongxi.whatsmars.elasticsearch.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 常见查询示例：Match、Term、Range、Bool Query
 */
@Component
public class QueryExample {

    private static final Logger log = LoggerFactory.getLogger(QueryExample.class);

    private static final String INDEX_NAME = "product";

    private final ElasticsearchClient client;

    public QueryExample(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * 运行所有查询示例
     */
    public void runAll() throws IOException {
        log.info("========== 查询示例 ==========");

        // 刷新索引以确保最新文档可搜索
        client.indices().refresh(r -> r.index(INDEX_NAME));

        matchQuery();
        matchPhraseQuery();
        multiMatchQuery();
        termQuery();
        termsQuery();
        rangeQuery();
        boolQuery();
        queryStringQuery();

        log.info("========== 查询示例结束 ==========\n");
    }

    /**
     * Match Query：全文搜索，会对搜索词进行分词后与字段匹配
     * 示例：搜索 name 字段中包含 "MacBook" 的文档
     */
    public void matchQuery() throws IOException {
        log.info("--- Match Query: 搜索 name 包含 'MacBook' ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .match(m -> m
                                        .field("name")
                                        .query("MacBook")
                                )
                        ),
                Product.class
        );

        printResults(response);
    }

    /**
     * Match Phrase Query：精确短语匹配，要求搜索词按顺序完整出现
     * 示例：搜索 description 中包含 "noise canceling" 短语的文档
     */
    public void matchPhraseQuery() throws IOException {
        log.info("--- Match Phrase Query: 搜索 description 包含 'noise canceling' ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .matchPhrase(mp -> mp
                                        .field("description")
                                        .query("noise canceling")
                                )
                        ),
                Product.class
        );

        printResults(response);
    }

    /**
     * Multi-Match Query：同时在多个字段中搜索
     * 示例：在 name 和 description 中搜索 "Pro"
     */
    public void multiMatchQuery() throws IOException {
        log.info("--- Multi-Match Query: 在 name 和 description 中搜索 'Pro' ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .multiMatch(mm -> mm
                                        .fields("name", "description")
                                        .query("Pro")
                                )
                        ),
                Product.class
        );

        printResults(response);
    }

    /**
     * Term Query：精确值匹配，不进行分词，适用于 keyword 类型字段
     * 示例：搜索 category 精确等于 "Electronics" 的文档
     */
    public void termQuery() throws IOException {
        log.info("--- Term Query: category 精确等于 'Electronics' ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .term(t -> t
                                        .field("category")
                                        .value("Electronics")
                                )
                        ),
                Product.class
        );

        printResults(response);
    }

    /**
     * Terms Query：匹配多个精确值中的任意一个
     * 示例：category 为 "Electronics" 或 "Books" 的文档
     */
    public void termsQuery() throws IOException {
        log.info("--- Terms Query: category 为 'Electronics' 或 'Books' ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .terms(t -> t
                                        .field("category")
                                        .terms(ts -> ts.value(List.of(
                                                FieldValue.of("Electronics"),
                                                FieldValue.of("Books")
                                        )))
                                )
                        ),
                Product.class
        );

        printResults(response);
    }

    /**
     * Range Query：范围查询，支持 gt/gte/lt/lte
     * 示例：搜索价格在 1000 到 5000 之间的文档
     */
    public void rangeQuery() throws IOException {
        log.info("--- Range Query: 价格 1000 <= price <= 5000 ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .range(r -> r
                                        .number(n -> n
                                                .field("price")
                                                .gte(1000.0)
                                                .lte(5000.0)
                                        )
                                )
                        ),
                Product.class
        );

        printResults(response);
    }

    /**
     * Bool Query：组合多个子查询，支持 must/should/must_not/filter
     * 示例：
     * - must: category = "Electronics"
     * - must: price < 10000
     * - should: name 包含 "Pro"（加分项）
     * - must_not: name 包含 "Samsung"
     */
    public void boolQuery() throws IOException {
        log.info("--- Bool Query: category=Electronics AND price<10000 AND NOT Samsung ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .bool(b -> b
                                        // must：必须满足，影响相关性评分
                                        .must(Query.of(qu -> qu.term(t -> t.field("category").value("Electronics"))))
                                        .must(Query.of(qu -> qu.range(r -> r.number(n -> n.field("price").lt(10000.0)))))
                                        // should：可选条件，满足则加分
                                        .should(Query.of(qu -> qu.match(m -> m.field("name").query("Pro"))))
                                        // must_not：必须不满足
                                        .mustNot(Query.of(qu -> qu.match(m -> m.field("name").query("Samsung"))))
                                        // filter：必须满足但不影响评分，可缓存
                                        .filter(Query.of(qu -> qu.range(r -> r.number(n -> n.field("quantity").gte(50.0)))))
                                )
                        ),
                Product.class
        );

        printResults(response);
    }

    /**
     * Query String Query：使用查询语法字符串，适合动态构建查询
     * 示例：搜索 name 中包含 "MacBook" 或 category 为 "Books" 的文档
     */
    public void queryStringQuery() throws IOException {
        log.info("--- Query String Query: name:MacBook OR category:Books ---");

        SearchResponse<Product> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .queryString(qs -> qs
                                        .query("name:MacBook OR category:Books")
                                )
                        ),
                Product.class
        );

        printResults(response);
    }

    /**
     * 打印搜索结果
     */
    private void printResults(SearchResponse<Product> response) {
        log.info("  命中总数: {}, 耗时: {}ms", response.hits().total().value(), response.took());
        if (response.hits().hits().isEmpty()) {
            log.info("  （无结果）");
        }
        for (Hit<Product> hit : response.hits().hits()) {
            log.info("  - [score={}] id={}, {}", hit.score(), hit.id(), hit.source());
        }
    }
}
