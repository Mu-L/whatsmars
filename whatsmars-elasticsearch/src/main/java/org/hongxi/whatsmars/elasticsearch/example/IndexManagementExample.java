package org.hongxi.whatsmars.elasticsearch.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 索引管理示例：创建索引、删除索引、映射管理
 */
@Component
public class IndexManagementExample {

    private static final Logger log = LoggerFactory.getLogger(IndexManagementExample.class);

    private static final String INDEX_NAME = "product";

    private final ElasticsearchClient client;

    public IndexManagementExample(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * 运行所有索引管理示例
     */
    public void runAll() throws IOException {
        log.info("========== 索引管理示例 ==========");

        // 1. 删除索引（如果存在）
        deleteIndexIfExists();

        // 2. 创建索引（带 Settings 和 Mapping）
        createIndexWithMapping();

        // 3. 查看索引映射
        getMapping();

        // 4. 更新映射（新增字段）
        updateMapping();

        // 5. 查看更新后的映射
        getMapping();

        log.info("========== 索引管理示例结束 ==========\n");
    }

    /**
     * 删除索引（如果存在）
     */
    public void deleteIndexIfExists() throws IOException {
        boolean exists = client.indices().exists(e -> e.index(INDEX_NAME)).value();
        if (exists) {
            client.indices().delete(d -> d.index(INDEX_NAME));
            log.info("已删除索引: {}", INDEX_NAME);
        } else {
            log.info("索引不存在，无需删除: {}", INDEX_NAME);
        }
    }

    /**
     * 创建索引，同时定义 Settings 和 Mapping
     */
    public void createIndexWithMapping() throws IOException {
        client.indices().create(c -> c
                .index(INDEX_NAME)
                .settings(IndexSettings.of(s -> s
                        .numberOfShards("2")
                        .numberOfReplicas("1")
                        .refreshInterval(r -> r.time("5s"))
                ))
                .mappings(TypeMapping.of(m -> m
                        // id 字段：keyword 类型
                        .properties("id", Property.of(p -> p.keyword(k -> k)))
                        // name 字段：text 类型，支持分词搜索
                        .properties("name", Property.of(p -> p.text(t -> t
                                .analyzer("standard")
                                .fields("keyword", Property.of(kw -> kw.keyword(k -> k.ignoreAbove(256))))
                        )))
                        // category 字段：keyword 类型，用于精确匹配和聚合
                        .properties("category", Property.of(p -> p.keyword(k -> k)))
                        // price 字段：double 类型
                        .properties("price", Property.of(p -> p.double_(d -> d)))
                        // quantity 字段：integer 类型
                        .properties("quantity", Property.of(p -> p.integer(i -> i)))
                        // description 字段：text 类型
                        .properties("description", Property.of(p -> p.text(t -> t.analyzer("standard"))))
                        // tags 字段：keyword 数组
                        .properties("tags", Property.of(p -> p.keyword(k -> k)))
                ))
        );
        log.info("已创建索引: {} (2 shards, 1 replica)", INDEX_NAME);
    }

    /**
     * 获取索引映射
     */
    public void getMapping() throws IOException {
        GetMappingResponse response = client.indices().getMapping(g -> g.index(INDEX_NAME));
        IndexMappingRecord record = response.result().get(INDEX_NAME);
        if (record != null) {
            Map<String, Property> properties = record.mappings().properties();
            log.info("索引 {} 的字段列表:", INDEX_NAME);
            properties.forEach((field, prop) -> {
                String type = prop._kind().name();
                log.info("  - {}: {}", field, type);
            });
        }
    }

    /**
     * 更新映射：新增一个字段
     */
    public void updateMapping() throws IOException {
        client.indices().putMapping(p -> p
                .index(INDEX_NAME)
                .properties("status", Property.of(prop -> prop.keyword(k -> k)))
                .properties("createdAt", Property.of(prop -> prop.date(d -> d.format("yyyy-MM-dd HH:mm:ss||epoch_millis")))
                )
        );
        log.info("已更新索引 {} 的映射，新增字段: status, createdAt", INDEX_NAME);
    }
}

