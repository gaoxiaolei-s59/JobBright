package org.puregxl.site.infra.embedding;

import java.util.List;

public interface EmbeddingService {

    /**
     * 对单个文本进行向量化（Embedding）
     * <p>
     * 说明：
     * - 通常用于查询向量生成（Query Embedding）
     * - 输出为 Float 向量列表，例如：[0.123f, -0.078f, ...]
     *
     * @param text 待向量化文本
     * @return 文本对应的向量（长度固定，如 4096）
     */
    List<Float> embed(String text);

    /**
     * 指定模型对单个文本进行向量化（不进行重试或降级）
     *
     * @param text    待向量化文本
     * @param modelId 指定的模型ID
     * @return 文本对应的向量
     */
    List<Float> embed(String text, String modelId);

    /**
     * 对多个文本进行批量向量化
     * <p>
     * 说明：
     * - 常用于文档索引构建（Indexing），性能优于单次调用 embed()
     * - 返回结果与输入 texts 顺序一致
     * - 实现类可利用模型的批量计算能力提升吞吐
     *
     * @param texts 文本列表
     * @return 向量列表，每项对应输入文本的向量
     */
    List<List<Float>> embedBatch(List<String> texts);

    /**
     * 指定模型对多个文本进行批量向量化（不进行重试或降级）
     *
     * @param texts   文本列表
     * @param modelId 指定的模型ID
     * @return 向量列表
     */
    List<List<Float>> embedBatch(List<String> texts, String modelId);



    /**
     * 返回向量维度（Embedding Dimension）
     * <p>
     * 说明：
     * - 根据底层模型决定（如 Qwen3-Embedding 维度为 4096）
     * - 用于校验向量长度、向量库 schema 定义等
     *
     * @return 向量维度（如 4096、768 等）
     */
    default int dimension() {
        return 0;
    }
}
