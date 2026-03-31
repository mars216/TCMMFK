import os
import json
import torch
import torch.nn as nn
import torch.nn.functional as F
from torch_geometric.data import HeteroData
from torch_geometric.nn import RGCNConv


# ================== 固定路径 ==================
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# 修改文件路径为相对于脚本目录的绝对路径
GRAPH_PATH = os.path.join(BASE_DIR, "pyg_hetero_graph22_1.pt")
CKPT_PATH  = os.path.join(BASE_DIR, "rgcn_mlp_interaction_fold1_best.pt")
HERB_E2ID_PATH = os.path.join(BASE_DIR, "Herb_entity2id.json")
PROT_E2ID_PATH = os.path.join(BASE_DIR, "Protein_entity2id.json")
SAVE_CSV_PATH = os.path.join(BASE_DIR, "pred_novel_targets.csv")

# 👉 改这里即可
TARGET_HERB = "H0342"
TOPK = 50
BATCH_SIZE = 4096

EMBED_DIM = 128
DROPOUT = 0.5

HERB_NODE_TYPE = "Herb"
PROTEIN_NODE_TYPE = "Protein"
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"


# ================== Init Embedding ==================
class InitEmbedding(nn.Module):
    def __init__(self, data: HeteroData, embed_dim: int):
        super().__init__()
        self.embeddings = nn.ModuleDict()
        self.x_params = nn.ParameterDict()
        self.has_x = {}

        for ntype in data.node_types:
            if "x" in data[ntype]:
                x = data[ntype].x.float()
                x = F.normalize(x, p=2, dim=1, eps=1e-12)
                self.has_x[ntype] = True
                self.x_params[ntype] = nn.Parameter(x.clone(), requires_grad=False)
            else:
                self.has_x[ntype] = False
                emb = nn.Embedding(data[ntype].num_nodes, embed_dim)
                nn.init.xavier_uniform_(emb.weight)
                self.embeddings[ntype] = emb

    def forward(self, data: HeteroData):
        x_dict = {}
        device = next(self.parameters()).device
        for ntype in data.node_types:
            if self.has_x[ntype]:
                x_dict[ntype] = self.x_params[ntype].to(device)
            else:
                idx = torch.arange(data[ntype].num_nodes, device=device)
                x_dict[ntype] = self.embeddings[ntype](idx)
        return x_dict


# ================== MLP Interaction Scorer ==================
class MLPInteractionScorer(nn.Module):
    def __init__(self, dim: int, hidden: int, drop: float):
        super().__init__()
        self.mlp = nn.Sequential(
            nn.Linear(dim * 4, hidden),
            nn.ReLU(),
            nn.Dropout(drop),
            nn.Linear(hidden, 1),
        )

    def forward(self, h, p):
        feat = torch.cat([h, p, h * p, (h - p).abs()], dim=-1)
        return self.mlp(feat).view(-1)


# ================== RGCN + MLPInteraction ==================
class RGCNLinkPredictor(nn.Module):
    def __init__(self, data: HeteroData, dim: int, drop: float, num_bases: int = 30):
        super().__init__()

        self.init_emb = InitEmbedding(data, dim)

        self.edge_index, self.edge_type = self._build_relational_edges(data)
        self.num_relations = len(data.edge_types)
        self.node_mapping = self._build_node_mapping(data)

        self.rgcn1 = RGCNConv(dim, dim, self.num_relations, num_bases=num_bases)
        self.rgcn2 = RGCNConv(dim, dim, self.num_relations, num_bases=num_bases)

        self.dropout = nn.Dropout(drop)
        self.scorer = MLPInteractionScorer(dim, dim, drop)

    def _build_relational_edges(self, data: HeteroData):
        edges, edge_types = [], []
        node_offset = 0
        node_offsets = {}

        for ntype in data.node_types:
            node_offsets[ntype] = node_offset
            node_offset += data[ntype].num_nodes

        rel_type_to_id = {et: i for i, et in enumerate(data.edge_types)}

        for (src, rel, dst), edge_index in data.edge_index_dict.items():
            src_off = node_offsets[src]
            dst_off = node_offsets[dst]
            offset = torch.tensor([[src_off], [dst_off]])
            edges.append(edge_index.cpu() + offset)
            edge_types.append(
                torch.full((edge_index.size(1),), rel_type_to_id[(src, rel, dst)], dtype=torch.long)
            )

        return torch.cat(edges, dim=1), torch.cat(edge_types)

    def _build_node_mapping(self, data: HeteroData):
        mapping = {}
        offset = 0
        for ntype in data.node_types:
            mapping[ntype] = torch.arange(offset, offset + data[ntype].num_nodes)
            offset += data[ntype].num_nodes
        return mapping

    def forward(self, data: HeteroData, h_idx, p_idx):
        x_dict = self.init_emb(data)
        x = torch.cat([x_dict[t] for t in data.node_types], dim=0)

        device = x.device
        edge_index = self.edge_index.to(device)
        edge_type = self.edge_type.to(device)

        x = F.relu(self.rgcn1(x, edge_index, edge_type))
        x = self.dropout(x)
        x = self.rgcn2(x, edge_index, edge_type)

        h_global = self.node_mapping[HERB_NODE_TYPE].to(device)[h_idx]
        p_global = self.node_mapping[PROTEIN_NODE_TYPE].to(device)[p_idx]

        return self.scorer(x[h_global], x[p_global])

class HerbTargetPredictor:
    """草药靶点预测器，加载模型和图数据，提供预测方法"""
    def __init__(self, data_dir=None, device=None):
        # 如果没有指定 data_dir，则默认使用当前文件所在目录
        if data_dir is None:
            data_dir = os.path.dirname(__file__)
        self.data_dir = data_dir
        self.device = device or torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self._load_resources()

    def _load_resources(self):
        # 加载实体映射
        with open(os.path.join(self.data_dir, "Herb_entity2id.json"), "r", encoding="utf-8") as f:
            self.herb_e2id = json.load(f)
        with open(os.path.join(self.data_dir, "Protein_entity2id.json"), "r", encoding="utf-8") as f:
            self.prot_e2id = json.load(f)
        self.prot_id2e = {v: k for k, v in self.prot_e2id.items()}

        # 加载异构图
        graph_path = os.path.join(self.data_dir, "pyg_hetero_graph22_1.pt")
        self.data = torch.load(graph_path, map_location="cpu", weights_only=False).to(self.device)

        # 构建已知正边集合（用于过滤已知靶点）
        ek = ("Herb", "interacts_with", "Protein")
        pos_edges = self.data[ek].edge_index.t().cpu().numpy()
        self.known_pairs = set((int(h), int(p)) for h, p in pos_edges)

        # 初始化模型
        self.model = RGCNLinkPredictor(data=self.data, dim=128, drop=0.5).to(self.device)
        ckpt_path = os.path.join(self.data_dir, "rgcn_mlp_interaction_fold1_best.pt")
        self.model.load_state_dict(torch.load(ckpt_path, map_location=self.device, weights_only=False))
        self.model.eval()

    def predict(self, herb_id, top_k=50, batch_size=4096):
        # ... 预测逻辑保持不变 ...
        # 注意：这里的 herb_id 是外部传入的实体 ID（如 "H0342"），不是索引
        if herb_id not in self.herb_e2id:
            raise ValueError(f"草药ID {herb_id} 不存在于映射文件中")
        herb_idx = self.herb_e2id[herb_id]

        num_proteins = self.data["Protein"].num_nodes
        all_p = torch.arange(num_proteins, device=self.device)
        scores = []

        for start in range(0, num_proteins, batch_size):
            end = min(start + batch_size, num_proteins)
            p_idx = all_p[start:end]
            h_idx = torch.full((p_idx.size(0),), herb_idx, device=self.device)
            logit = self.model(self.data, h_idx, p_idx)   # raw logit
            scores.append(logit.cpu())

        scores = torch.cat(scores).detach().numpy()

        # 过滤已知靶点
        candidates = []
        for pid, sc in enumerate(scores):
            if (herb_idx, pid) in self.known_pairs:
                continue
            candidates.append((pid, sc))

        candidates.sort(key=lambda x: x[1], reverse=True)
        return candidates[:top_k]
# ================== Main ==================
def main():
    print("Loading mappings...")
    herb_e2id = json.load(open(HERB_E2ID_PATH))
    prot_e2id = json.load(open(PROT_E2ID_PATH))
    herb_id2e = {v: k for k, v in herb_e2id.items()}
    prot_id2e = {v: k for k, v in prot_e2id.items()}

    if TARGET_HERB not in herb_e2id:
        raise ValueError(f"{TARGET_HERB} not in Herb_entity2id.json")

    print("Loading graph...")
    data: HeteroData = torch.load(GRAPH_PATH, map_location="cpu").to(DEVICE)

    # ==== 已知正边集合（用于过滤已知靶点） ====
    ek = (HERB_NODE_TYPE, "interacts_with", PROTEIN_NODE_TYPE)
    pos_edges = data[ek].edge_index.t().cpu().numpy()
    known_pairs = set((int(h), int(p)) for h, p in pos_edges)

    print("Building model...")
    model = RGCNLinkPredictor(data=data, dim=EMBED_DIM, drop=DROPOUT).to(DEVICE)

    print("Loading checkpoint...")
    model.load_state_dict(torch.load(CKPT_PATH, map_location=DEVICE))
    model.eval()

    herb_id = herb_e2id[TARGET_HERB]
    print(f"\nPredicting NOVEL targets for Herb {TARGET_HERB} (id={herb_id})")

    # ==== 对所有蛋白打 raw logit 分数 ====
    num_p = data[PROTEIN_NODE_TYPE].num_nodes
    all_p = torch.arange(num_p, device=DEVICE)

    scores = []
    for s in range(0, num_p, BATCH_SIZE):
        e = min(s + BATCH_SIZE, num_p)
        p_idx = all_p[s:e]
        h_idx = torch.full((p_idx.size(0),), herb_id, device=DEVICE)

        logit = model(data, h_idx, p_idx)   
        # ⚠ 不用 sigmoid
        prob = torch.sigmoid(logit)  
        scores.append(prob.cpu())

    scores = torch.cat(scores).detach().numpy()

    # ==== 过滤已知靶点 ====
    candidates = []
    for pid, sc in enumerate(scores):
        if (herb_id, pid) in known_pairs:
            continue
        candidates.append((pid, sc))

    # 按 logit 排序
    candidates.sort(key=lambda x: x[1], reverse=True)
    topk = candidates[:TOPK]

    print("\nRank\tProteinEntity\tProteinID\tLogitScore")
    rows = []
    for i, (pid, sc) in enumerate(topk, 1):
        pe = prot_id2e.get(pid, "UNK")
        print(f"{i}\t{pe}\t{pid}\t{sc:.4f}")
        rows.append((i, pe, pid, sc))

    print("\nSaving CSV to:", SAVE_CSV_PATH)
    with open(SAVE_CSV_PATH, "w") as f:
        f.write("rank,herb_entity,herb_id,protein_entity,protein_id,logit_score\n")
        for i, pe, pid, sc in rows:
            f.write(f"{i},{TARGET_HERB},{herb_id},{pe},{pid},{sc:.8f}\n")

    print("Done ✅ (only novel targets, using raw logits)")
# def main():
#     print("Loading mappings...")
#     herb_e2id = json.load(open(HERB_E2ID_PATH))
#     prot_e2id = json.load(open(PROT_E2ID_PATH))
#     herb_id2e = {v: k for k, v in herb_e2id.items()}
#     prot_id2e = {v: k for k, v in prot_e2id.items()}

#     if TARGET_HERB not in herb_e2id:
#         raise ValueError(f"{TARGET_HERB} not in Herb_entity2id.json")

#     print("Loading graph...")
#     data: HeteroData = torch.load(GRAPH_PATH, map_location="cpu").to(DEVICE)

#     # ==== 已知正边集合（用于打标签，不再过滤） ====
#     ek = (HERB_NODE_TYPE, "interacts_with", PROTEIN_NODE_TYPE)
#     pos_edges = data[ek].edge_index.t().cpu().numpy()
#     known_pairs = set((int(h), int(p)) for h, p in pos_edges)

#     print("Building model...")
#     model = RGCNLinkPredictor(data=data, dim=EMBED_DIM, drop=DROPOUT).to(DEVICE)

#     print("Loading checkpoint...")
#     model.load_state_dict(torch.load(CKPT_PATH, map_location=DEVICE))
#     model.eval()

#     herb_id = herb_e2id[TARGET_HERB]
#     print(f"\nPredicting targets for Herb {TARGET_HERB} (id={herb_id})")

#     # ==== 对所有蛋白打 raw logit 分数 ====
#     num_p = data[PROTEIN_NODE_TYPE].num_nodes
#     all_p = torch.arange(num_p, device=DEVICE)

#     scores = []
#     for s in range(0, num_p, BATCH_SIZE):
#         e = min(s + BATCH_SIZE, num_p)
#         p_idx = all_p[s:e]
#         h_idx = torch.full((p_idx.size(0),), herb_id, device=DEVICE)

#         logit = model(data, h_idx, p_idx)   # raw logit
#         scores.append(logit.cpu())

#     scores = torch.cat(scores).detach().numpy()

#     # ==== 构建带标签的候选列表（不删除已知边） ====
#     candidates = []
#     for pid, sc in enumerate(scores):
#         label = 1 if (herb_id, pid) in known_pairs else 0
#         candidates.append((pid, sc, label))

#     # 按 logit 排序
#     candidates.sort(key=lambda x: x[1], reverse=True)
#     topk = candidates[:TOPK]

#     print("\nRank\tProteinEntity\tProteinID\tLogitScore\tLabel(1=known)")
#     rows = []
#     for i, (pid, sc, lb) in enumerate(topk, 1):
#         pe = prot_id2e.get(pid, "UNK")
#         print(f"{i}\t{pe}\t{pid}\t{sc:.4f}\t{lb}")
#         rows.append((i, pe, pid, sc, lb))

#     print("\nSaving CSV to:", SAVE_CSV_PATH)
#     with open(SAVE_CSV_PATH, "w") as f:
#         f.write("rank,herb_entity,herb_id,protein_entity,protein_id,logit_score,label\n")
#         for i, pe, pid, sc, lb in rows:
#             f.write(f"{i},{TARGET_HERB},{herb_id},{pe},{pid},{sc:.8f},{lb}\n")

#     print("Done ✅ (all targets, labeled known vs predicted)")


if __name__ == "__main__":
    main()
