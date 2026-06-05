package org.hongxi.whatsmars.elasticsearch.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import org.hongxi.whatsmars.elasticsearch.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 批量操作（Bulk API）示例：批量索引、批量更新、批量删除
 */
@Component
public class BulkOperationExample {

    private static final Logger log = LoggerFactory.getLogger(BulkOperationExample.class);

    private static final String INDEX_NAME = "product";

    private final ElasticsearchClient client;

    public BulkOperationExample(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * 运行所有批量操作示例
     */
    public void runAll() throws IOException {
        log.info("========== 批量操作（Bulk API）示例 ==========");

        // 1. 批量索引文档
        bulkIndex();

        // 2. 批量更新文档
        bulkUpdate();

        // 3. 批量删除文档
        bulkDelete();

        // 4. 混合操作（同时包含 index、update、delete）
        mixedBulkOperations();

        log.info("========== 批量操作示例结束 ==========\n");
    }

    /**
     * 批量索引多个文档
     */
    public void bulkIndex() throws IOException {
        List<Product> products = List.of(
                new Product("P002", "iPhone 16 Pro Max", "Electronics", 9999.0, 200,
                        "6.9 inch Super Retina XDR display, A18 Pro chip",
                        List.of("phone", "apple", "flagship")),
                new Product("P003", "Sony WH-1000XM5", "Electronics", 2499.0, 100,
                        "Industry-leading noise canceling wireless headphones",
                        List.of("headphone", "sony", "wireless")),
                new Product("P004", "Nike Air Max 270", "Sports", 1099.0, 300,
                        "Lifestyle sneakers with visible Max Air unit",
                        List.of("shoes", "nike", "running")),
                new Product("P005", "The Art of Programming", "Books", 199.0, 500,
                        "Classic computer science textbook by Donald Knuth",
                        List.of("book", "programming", "classic")),
                new Product("P006", "Samsung Galaxy S25 Ultra", "Electronics", 9699.0, 150,
                        "6.9 inch Dynamic AMOLED, Snapdragon 8 Elite, S Pen",
                        List.of("phone", "samsung", "flagship"))
        );

        List<BulkOperation> operations = new ArrayList<>();
        for (Product product : products) {
            operations.add(BulkOperation.of(op -> op
                    .index(idx -> idx
                            .index(INDEX_NAME)
                            .id(product.getId())
                            .document(product)
                    )
            ));
        }

        BulkResponse response = client.bulk(b -> b.operations(operations));

        if (response.errors()) {
            logErrors(response);
        } else {
            log.info("批量索引成功，共 {} 条文档", response.items().size());
        }
    }

    /**
     * 批量更新多个文档
     */
    public void bulkUpdate() throws IOException {
        List<BulkOperation> operations = List.of(
                BulkOperation.of(op -> op
                        .update(u -> u
                                .index(INDEX_NAME)
                                .id("P002")
                                .action(a -> a.doc(Map.of("price", 9499.0)))
                        )
                ),
                BulkOperation.of(op -> op
                        .update(u -> u
                                .index(INDEX_NAME)
                                .id("P003")
                                .action(a -> a.doc(Map.of("quantity", 80)))
                        )
                )
        );

        BulkResponse response = client.bulk(b -> b.operations(operations));

        if (response.errors()) {
            logErrors(response);
        } else {
            log.info("批量更新成功，共 {} 条文档", response.items().size());
        }
    }

    /**
     * 批量删除多个文档
     */
    public void bulkDelete() throws IOException {
        // 先插入两个临时文档
        client.index(i -> i.index(INDEX_NAME).id("P_DEL_1").document(
                new Product("P_DEL_1", "待删除商品1", "Other", 1.0, 0, "临时", List.of("temp"))));
        client.index(i -> i.index(INDEX_NAME).id("P_DEL_2").document(
                new Product("P_DEL_2", "待删除商品2", "Other", 1.0, 0, "临时", List.of("temp"))));

        List<BulkOperation> operations = List.of(
                BulkOperation.of(op -> op
                        .delete(d -> d.index(INDEX_NAME).id("P_DEL_1"))
                ),
                BulkOperation.of(op -> op
                        .delete(d -> d.index(INDEX_NAME).id("P_DEL_2"))
                )
        );

        BulkResponse response = client.bulk(b -> b.operations(operations));

        if (response.errors()) {
            logErrors(response);
        } else {
            log.info("批量删除成功，共 {} 条文档", response.items().size());
        }
    }

    /**
     * 混合批量操作：在同一个 Bulk 请求中同时执行 index、update、delete
     */
    public void mixedBulkOperations() throws IOException {
        // 先插入一个待删除的文档
        client.index(i -> i.index(INDEX_NAME).id("P_MIX_DEL").document(
                new Product("P_MIX_DEL", "混合操作-待删除", "Other", 1.0, 0, "临时", List.of("temp"))));

        Product newProduct = new Product("P007", "Kindle Paperwhite", "Electronics", 1099.0, 200,
                "6.8 inch display, adjustable warm light, waterproof",
                List.of("ereader", "amazon", "kindle"));

        List<BulkOperation> operations = List.of(
                // index 操作：新增文档
                BulkOperation.of(op -> op
                        .index(idx -> idx.index(INDEX_NAME).id(newProduct.getId()).document(newProduct))
                ),
                // update 操作：更新已有文档
                BulkOperation.of(op -> op
                        .update(u -> u
                                .index(INDEX_NAME)
                                .id("P004")
                                .action(a -> a.doc(java.util.Map.of("price", 899.0, "quantity", 250)))
                        )
                ),
                // delete 操作：删除文档
                BulkOperation.of(op -> op
                        .delete(d -> d.index(INDEX_NAME).id("P_MIX_DEL"))
                )
        );

        BulkResponse response = client.bulk(b -> b.operations(operations));

        if (response.errors()) {
            logErrors(response);
        } else {
            log.info("混合批量操作成功，共 {} 条操作", response.items().size());
            for (BulkResponseItem item : response.items()) {
                log.info("  - 操作类型: {}, 文档ID: {}, 状态: {}",
                        item.operationType(), item.id(), item.status());
            }
        }
    }

    /**
     * 打印批量操作中的错误
     */
    private void logErrors(BulkResponse response) {
        log.error("批量操作存在错误:");
        for (BulkResponseItem item : response.items()) {
            if (item.error() != null) {
                log.error("  - 文档ID: {}, 错误: {}", item.id(), item.error().reason());
            }
        }
    }
}
