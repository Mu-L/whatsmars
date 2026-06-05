package org.hongxi.whatsmars.elasticsearch;

import org.hongxi.whatsmars.elasticsearch.example.AggregationExample;
import org.hongxi.whatsmars.elasticsearch.example.BulkOperationExample;
import org.hongxi.whatsmars.elasticsearch.example.DocumentCrudExample;
import org.hongxi.whatsmars.elasticsearch.example.IndexManagementExample;
import org.hongxi.whatsmars.elasticsearch.example.QueryExample;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Elasticsearch 示例入口，使用 ElasticsearchClient（co.elastic.clients）
 *
 * 示例包含：
 * 1. 索引的创建/删除/映射管理
 * 2. 文档的 CRUD 操作（Index, Get, Update, Delete）
 * 3. 批量操作（Bulk API）
 * 4. 常见查询示例（Match, Term, Range, Bool Query）
 * 5. 聚合查询（Aggregation）
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        // 非 Web 应用，CommandLineRunner 执行完后主动关闭容器，
        // 触发 RestClient 的 destroyMethod 释放连接池线程，使 JVM 正常退出
        SpringApplication.exit(context);
    }

    @Bean
    CommandLineRunner commandLineRunner(IndexManagementExample indexManagement,
                                        DocumentCrudExample documentCrud,
                                        BulkOperationExample bulkOperation,
                                        QueryExample queryExample,
                                        AggregationExample aggregationExample) {
        return args -> {
            // 1. 索引管理：创建/删除/映射
            indexManagement.runAll();

            // 2. 文档 CRUD：Index/Get/Update/Delete
            documentCrud.runAll();

            // 3. 批量操作：Bulk API
            bulkOperation.runAll();

            // 4. 查询示例：Match/Term/Range/Bool Query
            queryExample.runAll();

            // 5. 聚合查询：Terms/Metric/Range/Nested/Filter Aggregation
            aggregationExample.runAll();
        };
    }
}