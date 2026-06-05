package org.hongxi.whatsmars.elasticsearch.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import org.hongxi.whatsmars.elasticsearch.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 文档 CRUD 操作示例：Index（创建/覆盖）、Get（查询）、Update（更新）、Delete（删除）
 */
@Component
public class DocumentCrudExample {

    private static final Logger log = LoggerFactory.getLogger(DocumentCrudExample.class);

    private static final String INDEX_NAME = "product";

    private final ElasticsearchClient client;

    public DocumentCrudExample(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * 运行所有文档 CRUD 示例
     */
    public void runAll() throws IOException {
        log.info("========== 文档 CRUD 示例 ==========");

        // 1. 索引单个文档（指定 ID）
        indexDocument();

        // 2. 获取文档
        getDocument();

        // 3. 更新文档（部分更新）
        updateDocument();

        // 4. 获取更新后的文档
        getDocument();

        // 5. 删除文档
        deleteDocument();

        // 6. 确认删除后获取文档（应返回 not found）
        getDocumentAfterDelete();

        log.info("========== 文档 CRUD 示例结束 ==========\n");
    }

    /**
     * 索引（写入）文档，指定文档 ID
     */
    public void indexDocument() throws IOException {
        Product product = new Product(
                "P001",
                "MacBook Pro 14",
                "Electronics",
                14999.0,
                50,
                "Apple M3 Pro chip, 18GB unified memory, 512GB SSD",
                List.of("laptop", "apple", "high-end")
        );

        IndexResponse response = client.index(i -> i
                .index(INDEX_NAME)
                .id(product.getId())
                .document(product)
        );

        log.info("文档已索引 - index: {}, id: {}, version: {}",
                response.index(), response.id(), response.version());
    }

    /**
     * 根据 ID 获取文档
     */
    public void getDocument() throws IOException {
        GetResponse<Product> response = client.get(g -> g
                        .index(INDEX_NAME)
                        .id("P001"),
                Product.class
        );

        if (response.found()) {
            Product product = response.source();
            log.info("文档已找到 - id: {}, source: {}", response.id(), product);
        } else {
            log.info("文档未找到 - id: P001");
        }
    }

    /**
     * 部分更新文档（只更新指定字段）
     */
    public void updateDocument() throws IOException {
        // 使用 Map 进行部分字段更新
        UpdateResponse<Product> response = client.update(u -> u
                        .index(INDEX_NAME)
                        .id("P001")
                        .doc(Map.of(
                                "price", 13999.0,
                                "quantity", 80,
                                "tags", List.of("laptop", "apple", "high-end", "discount")
                        )),
                Product.class
        );

        log.info("文档已更新 - id: {}, version: {}", response.id(), response.version());
    }

    /**
     * 删除文档
     */
    public void deleteDocument() throws IOException {
        // 先插入一个临时文档用于删除演示
        Product tempProduct = new Product(
                "P_TEMP",
                "临时商品",
                "Other",
                1.0,
                0,
                "用于删除演示",
                List.of("temp")
        );
        client.index(i -> i.index(INDEX_NAME).id(tempProduct.getId()).document(tempProduct));
        log.info("临时文档已索引 - id: P_TEMP");

        // 删除文档
        DeleteResponse response = client.delete(d -> d
                .index(INDEX_NAME)
                .id("P_TEMP")
        );

        log.info("文档已删除 - id: {}, result: {}", response.id(), response.result());
    }

    /**
     * 删除后尝试获取文档
     */
    public void getDocumentAfterDelete() throws IOException {
        GetResponse<Product> response = client.get(g -> g
                        .index(INDEX_NAME)
                        .id("P_TEMP"),
                Product.class
        );

        log.info("删除后查询 P_TEMP - found: {}", response.found());
    }
}
