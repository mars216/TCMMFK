import os
import re
import json
from dotenv import load_dotenv
from docx import Document
import requests

load_dotenv()

# 全局配置
CONFIG = {
    "chunk_size": int(os.getenv("CHUNK_SIZE", 1000)),
    "deepseek_api_key": os.getenv("DEEPSEEK_API_KEY"),
    "deepseek_api_url": os.getenv("DEEPSEEK_API_URL"),
    "neo4j_uri": os.getenv("NEO4J_URI"),
    "neo4j_user": os.getenv("NEO4J_USER"),
    "neo4j_password": os.getenv("NEO4J_PASSWORD")
}

# 允许的实体类型（对应 Neo4j 标签）
ALLOWED_TYPES = {"Herb", "Ingredients", "Target", "Disease"}

# 实体标识属性映射（根据实际情况调整）
ENTITY_ID_PROP = {
    "Herb": "herb_cn_name",
    "Ingredients": "name",
    "Target": "name",
    "Disease": "name"
}


# ------------------------------
# Neo4j 数据库操作
# ------------------------------
class Neo4jClient:
    def __init__(self):
        self.driver = None
        try:
            from neo4j import GraphDatabase, basic_auth
            self.driver = GraphDatabase.driver(
                CONFIG["neo4j_uri"],
                auth=basic_auth(CONFIG["neo4j_user"], CONFIG["neo4j_password"]),
                max_connection_lifetime=30,
                max_connection_pool_size=10,
                connection_acquisition_timeout=20,
            )
            self.driver.verify_connectivity()
            self.init_db()
        except Exception as e:
            raise Exception(f"Neo4j连接失败：{e}")

    def init_db(self):
        """为每种实体类型创建唯一约束（如果失败，只警告，不中断）"""
        if not self.driver:
            raise Exception("Neo4j驱动未初始化")
        try:
            from neo4j.exceptions import ClientError
            with self.driver.session() as session:
                for label in ALLOWED_TYPES:
                    try:
                        session.run(f"CREATE CONSTRAINT IF NOT EXISTS FOR (n:{label}) REQUIRE n.name IS UNIQUE;")
                    except ClientError as e:
                        error_msg = str(e)
                        # 忽略因索引已存在、约束已存在或数据重复导致的失败
                        if any(keyword in error_msg for keyword in ["IndexAlreadyExists", "ConstraintAlreadyExists", "ConstraintCreationFailed"]):
                            print(f"警告：无法为 {label} 创建唯一约束，原因：{error_msg[:200]}")
                        else:
                            # 其他类型的 ClientError 仍然抛出
                            raise
                print("Neo4j数据库初始化完成（约束创建已容错处理）")
        except Exception as e:
            # 这里只打印警告，不抛出异常，避免应用启动失败
            print(f"警告：Neo4j初始化过程中出现非致命错误：{e}")
    
    def update_entity_attributes(self, entity_name, entity_type, attributes):
        """更新或创建实体，使用对应标签，并根据实体类型选择标识属性"""
        if not self.driver:
            return False, "Neo4j驱动未初始化"
        if entity_type not in ALLOWED_TYPES:
            return False, f"不支持的实体类型：{entity_type}"

        id_prop = ENTITY_ID_PROP.get(entity_type, "name")

        try:
            with self.driver.session() as session:
                set_items = []
                params = {"name": entity_name, "attrs": attributes}
                for key in attributes:
                    set_items.append(f"n.`{key}` = $attrs.`{key}`")
                set_clause = ", ".join(set_items) if set_items else ""

                cypher = f"""
                    MERGE (n:{entity_type} {{`{id_prop}`: $name}})
                    ON CREATE SET n.created_at = timestamp()
                    SET {set_clause}, n.updated_at = timestamp()
                    RETURN n
                """
                session.run(cypher, **params)
            return True, f"实体 {entity_name} 属性保存成功"
        except Exception as e:
            return False, f"保存实体属性失败：{e}"

    def find_herb_by_name(self, name):
        """根据名称查找草药节点，返回节点属性字典"""
        with self.driver.session() as session:
            result = session.run(
                "MATCH (h:Herb) WHERE h.herb_cn_name = $name OR h.alias CONTAINS $name RETURN h",
                name=name
            ).single()
            return result["h"] if result else None

    def find_protein_by_entity_id(self, entity_id):
        """根据实体ID查找蛋白质节点"""
        with self.driver.session() as session:
            result = session.run(
                "MATCH (p:Protein) WHERE p.hti2_id = $entity_id RETURN p",
                entity_id=entity_id
            ).single()
            return result["p"] if result else None

    def save_triples(self, document_name, triples, entity_type_map=None):
        """保存三元组，使用实体类型映射确定节点标签和标识属性"""
        if not self.driver:
            return False, "Neo4j驱动未初始化"
        if not triples or not document_name:
            return False, "三元组或文档名不能为空"
        if entity_type_map is None:
            entity_type_map = {}

        try:
            with self.driver.session() as session:
                for triple in triples:
                    entity1 = triple.get("entity1", "").strip()
                    relation = triple.get("relation", "").strip()
                    entity2 = triple.get("entity2", "").strip()
                    source_text = triple.get("source_text", "").strip()
                    if not entity1 or not relation or not entity2:
                        continue

                    type1 = entity_type_map.get(entity1)
                    type2 = entity_type_map.get(entity2)

                    if not type1 or not type2:
                        print(f"警告：实体 '{entity1}' 或 '{entity2}' 缺少类型，跳过该三元组")
                        continue
                    if type1 not in ALLOWED_TYPES or type2 not in ALLOWED_TYPES:
                        print(f"警告：实体类型 {type1} 或 {type2} 不在允许列表中，跳过")
                        continue

                    id_prop1 = ENTITY_ID_PROP.get(type1, "name")
                    id_prop2 = ENTITY_ID_PROP.get(type2, "name")
                    relation_clean = relation.replace(" ", "_").replace("-", "_")

                    cypher = f"""
                        MERGE (e1:{type1} {{`{id_prop1}`: $entity1}})
                        ON CREATE SET e1.document = $document, e1.created_at = timestamp()
                        MERGE (e2:{type2} {{`{id_prop2}`: $entity2}})
                        ON CREATE SET e2.document = $document, e2.created_at = timestamp()
                        MERGE (e1)-[r:{relation_clean}]->(e2)
                        SET r.document = $document, r.source_text = $source_text, r.updated_at = timestamp()
                    """
                    params = {
                        "entity1": entity1,
                        "entity2": entity2,
                        "document": document_name,
                        "source_text": source_text,
                    }
                    session.run(cypher, **params)
            return True, f"成功保存 {len(triples)} 条三元组到 Neo4j"
        except Exception as e:
            return False, f"保存失败：{str(e)}"

    def close(self):
        if self.driver:
            try:
                self.driver.close()
                self.driver = None
                print("Neo4j驱动已安全关闭")
            except Exception as e:
                print(f"关闭Neo4j驱动失败：{e}")


# ------------------------------
# 文件处理逻辑（以下保持不变）
# ------------------------------
def validate_file(file):
    """校验docx文件（仅用扩展名+简单二进制校验）"""
    allowed_extensions = {"docx"}
    filename = file.filename
    if "." not in filename:
        return False, "文件无扩展名"
    ext = filename.rsplit(".", 1)[1].lower()
    if ext not in allowed_extensions:
        return False, f"仅支持docx格式，当前扩展名：{ext}"
    try:
        header = file.read(2)
        file.seek(0)
        if header != b'PK':
            return False, "文件不是有效的docx格式（文件头校验失败）"
        return True, "文件类型校验通过"
    except Exception as e:
        return False, f"文件校验失败：{e}"

def read_docx_file(file_path):
    """读取docx文件文本内容"""
    try:
        doc = Document(file_path)
        text = "\n".join([para.text for para in doc.paragraphs if para.text.strip()])
        return text.strip()
    except Exception as e:
        raise Exception(f"读取docx失败：{e}")

def chunk_text(text):
    """按固定长度分块（避免切断句子）"""
    chunks = []
    start = 0
    text_length = len(text)
    chunk_size = CONFIG["chunk_size"]
    while start < text_length:
        end = start + chunk_size
        if end < text_length:
            sep_pos = text.rfind(".", start, end)
            if sep_pos == -1:
                sep_pos = text.rfind("\n", start, end)
            if sep_pos != -1 and sep_pos > start:
                end = sep_pos + 1
        chunk = text[start:end].strip()
        if chunk:
            chunks.append(chunk)
        start = end
    return chunks

def extract_kg_from_text(text_chunk):
    """调用DeepSeek API抽取三元组和实体属性"""
    if not text_chunk.strip():
        return [], []

    prompt = f"""
    请从以下文本中抽取**中药知识图谱**所需的知识，仅关注以下实体类型：
    - 草药（Herb）：中药材名称
    - 成分（Ingredient）：草药中的化学/有效成分
    - 靶点（Target）：成分作用的生物靶点（如蛋白质、基因）
    - 疾病（Disease）：相关疾病名称

    任务要求：
    1. 抽取上述实体之间的**三元组关系**，格式为 {{"entity1": "实体A", "relation": "关系描述", "entity2": "实体B"}}，确保 entity1 和 entity2 属于上述类型。
    2. 对每个抽取出的实体，提取其**属性**（如草药的功效、性味归经、用法；靶点的功能；疾病描述等），格式为 {{"entity": "实体名称", "type": "实体类型（Herb/Ingredient/Target/Disease）", "attributes": {{"属性名": "属性值", ...}}}}。
    3. 输出一个 JSON 对象，包含两个字段： "triples" 和 "entities"。
       - "triples" 是三元组数组。
       - "entities" 是实体属性数组。
    4. 如果某段文本没有相关内容，输出空数组。
    5. 确保只使用文本中明确提到的信息，不要自行推测。
    6.如果时英文文档，不要翻译成中文，不要自行推测，只提取文章中有的内容。
    7.如果是英文文档，属性也是英文的话，也是直接返回英文内容，不要中文，关系三元组也是。

    文本内容：
    {text_chunk}
    """

    headers = {
        "Authorization": f"Bearer {CONFIG['deepseek_api_key']}",
        "Content-Type": "application/json"
    }
    data = {
        "model": "deepseek-chat",
        "messages": [{"role": "user", "content": prompt}],
        "temperature": 0.1,
        "max_tokens": 2000
    }

    try:
        response = requests.post(CONFIG["deepseek_api_url"], headers=headers, json=data, timeout=600)
        response.raise_for_status()
        result = response.json()
        print(f"DeepSeek API 调用成功，返回结果：{result}")
        content = result["choices"][0]["message"]["content"].strip()
        content = re.sub(r"```json|\n```", "", content)
        kg_data = json.loads(content)

        triples = kg_data.get("triples", [])
        entities = kg_data.get("entities", [])

        valid_triples = []
        for triple in triples:
            if all(k in triple for k in ["entity1", "relation", "entity2"]):
                valid_triples.append({
                    "entity1": triple["entity1"].strip(),
                    "relation": triple["relation"].strip(),
                    "entity2": triple["entity2"].strip(),
                    "source_text": text_chunk
                })

        valid_entities = []
        for ent in entities:
            if "entity" in ent and "type" in ent and "attributes" in ent:
                etype = ent["type"].strip()
                if etype in ALLOWED_TYPES:
                    valid_entities.append({
                        "name": ent["entity"].strip(),
                        "type": etype,
                        "attributes": ent["attributes"]
                    })
                else:
                    print(f"忽略不支持的实体类型：{etype}")

        return valid_triples, valid_entities
    except Exception as e:
        print(f"API调用失败：{e}")
        return [], []