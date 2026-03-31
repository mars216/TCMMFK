<template>
  <div class="container">
    <!-- Header -->
    <h1 class="page-title">
      <i class="fa fa-sitemap"></i> Knowledge Graph Extraction Tool
    </h1>

    <!-- Alert message -->
    <div v-if="message" :class="['alert', `alert-${message.type}`]">
      <i :class="['fa', message.type === 'success' ? 'fa-check-circle' : message.type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle']"></i> 
      {{ message.text }}
    </div>

    <!-- Step 1: File upload -->
    <div class="card step" :class="{ active: currentStep === 1 }">
      <h2 class="card-title">
        <i class="fa fa-file-text-o"></i> Upload DOCX Document
      </h2>
      <div class="form-group">
        <label class="form-label" for="file">Select DOCX file</label>
        <input 
          type="file" 
          class="form-control" 
          id="file" 
          accept=".docx" 
          @change="handleFileChange"
        >
        <small class="text-muted">Only .docx format is supported, file size recommended ≤10MB</small>
      </div>
      <button 
        class="btn" 
        @click="uploadFile" 
        :disabled="!selectedFile || isUploading"
      >
        <span v-if="isUploading" class="loading"></span>
        {{ isUploading ? 'Uploading...' : 'Upload and Extract Knowledge Graph' }}
      </button>
    </div>

    <!-- Step 2: Edit knowledge graph -->
    <div class="card step" :class="{ active: currentStep === 2 }">
      <h2 class="card-title">
        <i class="fa fa-edit"></i> Edit Knowledge Graph - {{ fileName }}
      </h2>

      <div class="alert alert-info">
        <i class="fa fa-lightbulb-o"></i> Please verify and edit the extracted triples and entity attributes, then click the save button to submit to Neo4j.
      </div>

      <!-- Batch actions -->
      <div class="batch-actions">
        <button class="btn btn-outline" @click="addTriple">
          <i class="fa fa-plus"></i> Add Triple
        </button>
        <button class="btn btn-outline" @click="addEntity">
          <i class="fa fa-plus"></i> Add Entity
        </button>
        <!-- <div> -->
          <button 
            class="btn btn-success" 
            @click="saveData" 
            :disabled="isSaving || (triples.length === 0 && entities.length === 0)"
          >
            <span v-if="isSaving" class="loading"></span>
            {{ isSaving ? 'Saving...' : 'Confirm Save to Neo4j' }}
          </button>
          <button class="btn btn-outline" @click="backToUpload">
            <i class="fa fa-arrow-left"></i> Back to Upload
          </button>
        <!-- </div> -->
      </div>

      <!-- Tab: Triples / Entity Attributes -->
      <div class="tab-header">
        <span 
          class="tab-item" 
          :class="{ active: activeTab === 'triples' }" 
          @click="activeTab = 'triples'"
        >
          Triples ({{ triples.length }})
        </span>
        <span 
          class="tab-item" 
          :class="{ active: activeTab === 'entities' }" 
          @click="activeTab = 'entities'"
        >
          Entity Attributes ({{ entities.length }})
        </span>
      </div>

      <!-- Triples list -->
      <div v-show="activeTab === 'triples'" id="triples-container">
        <div 
          class="triple-item" 
          v-for="(triple, index) in triples" 
          :key="'triple-' + index"
        >
          <input 
            type="text" 
            class="form-control" 
            v-model="triple.entity1" 
            placeholder="Entity 1"
            @blur="validateTriple(triple)"
          >
          <span class="triple-relation">→</span>
          <input 
            type="text" 
            class="form-control" 
            v-model="triple.relation" 
            placeholder="Relation"
            @blur="validateTriple(triple)"
          >
          <span class="triple-relation">→</span>
          <input 
            type="text" 
            class="form-control" 
            v-model="triple.entity2" 
            placeholder="Entity 2"
            @blur="validateTriple(triple)"
          >
          <button class="btn btn-danger btn-sm" @click="deleteTriple(index)">
            <i class="fa fa-trash"></i> Delete
          </button>
          <div class="triple-source">
            Source text: {{ triple.source_text.length > 150 ? triple.source_text.substring(0, 150) + '...' : triple.source_text }}
          </div>
        </div>

        <div v-if="triples.length === 0" class="empty-tip">
          <i class="fa fa-info-circle"></i> No triple data yet. Click "Add Triple" to create one.
        </div>
      </div>

      <!-- Entity attributes list -->
      <div v-show="activeTab === 'entities'" id="entities-container">
        <div 
          class="entity-item" 
          v-for="(entity, index) in entities" 
          :key="'entity-' + index"
        >
          <div class="entity-header">
            <input 
              type="text" 
              class="form-control entity-name" 
              v-model="entity.name" 
              placeholder="Entity name"
            >
            <select class="form-control entity-type" v-model="entity.type">
              <option value="Herb">Herb</option>
              <option value="Ingredient">Ingredient</option>
              <option value="Target">Target</option>
              <option value="Disease">Disease</option>
            </select>
            <button class="btn btn-danger btn-sm" @click="deleteEntity(index)">
              <i class="fa fa-trash"></i> Delete
            </button>
          </div>

          <!-- Attribute key-value editing -->
          <div class="attributes-container">
            <div 
              v-for="(attrValue, attrKey, attrIdx) in entity.attributes" 
              :key="attrIdx"
              class="attribute-row"
            >
              <input 
                type="text" 
                class="form-control attr-key" 
                :value="attrKey" 
                @input="updateAttributeKey(entity, attrKey, $event.target.value)"
                placeholder="Attribute name"
              >
              <input 
                type="text" 
                class="form-control attr-value" 
                :value="attrValue" 
                @input="updateAttributeValue(entity, attrKey, $event.target.value)"
                placeholder="Attribute value"
              >
              <button class="btn btn-danger btn-sm" @click="removeAttribute(entity, attrKey)">
                <i class="fa fa-times"></i>
              </button>
            </div>
            <button class="btn btn-outline btn-sm" @click="addAttribute(entity)">
              <i class="fa fa-plus"></i> Add Attribute
            </button>
          </div>
        </div>

        <div v-if="entities.length === 0" class="empty-tip">
          <i class="fa fa-info-circle"></i> No entity attribute data yet. Click "Add Entity" to create one.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

// API base URL
const API_BASE = '/flask';

const currentStep = ref(1);
const selectedFile = ref(null);
const fileName = ref('');
const triples = ref([]);
const entities = ref([]);
const isUploading = ref(false);
const isSaving = ref(false);
const message = ref(null);
const activeTab = ref('triples');

const showMessage = (text, type = 'info') => {
  message.value = { text, type };
  setTimeout(() => {
    message.value = null;
  }, 3000);
};

const handleFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    if (file.name.endsWith('.docx')) {
      selectedFile.value = file;
      fileName.value = file.name;
      showMessage(`File selected: ${file.name}`, 'success');
    } else {
      showMessage('Only .docx files are supported!', 'error');
      selectedFile.value = null;
    }
  }
};

const uploadFile = async () => {
  if (!selectedFile.value) {
    showMessage('Please select a .docx file first!', 'error');
    return;
  }

  isUploading.value = true;
  showMessage('Uploading and extracting knowledge graph...', 'info');

  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);

    const response = await axios.post(`${API_BASE}/extract`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });

    if (response.data.code === 200) {
      triples.value = response.data.data.triples || [];
      entities.value = response.data.data.entities || [];
      fileName.value = response.data.data.filename || fileName.value;
      isUploading.value = false;
      currentStep.value = 2;
      activeTab.value = 'triples';
      
      if (triples.value.length === 0 && entities.value.length === 0) {
        showMessage('File uploaded successfully, but no triples or entity attributes were extracted (you can add manually)', 'info');
      } else {
        showMessage(`Extraction successful! ${triples.value.length} triple(s), ${entities.value.length} entity(ies)`, 'success');
      }
    } else {
      isUploading.value = false;
      showMessage(`Extraction failed: ${response.data.msg}`, 'error');
    }
  } catch (error) {
    isUploading.value = false;
    const errorMsg = error.response?.data?.msg || error.message || 'Network error';
    showMessage(`Upload failed: ${errorMsg}`, 'error');
    console.error('Upload error:', error);
  }
};

const addTriple = () => {
  triples.value.push({
    entity1: '',
    relation: '',
    entity2: '',
    source_text: 'Manually added'
  });
};

const deleteTriple = (index) => {
  if (confirm('Are you sure you want to delete this row?')) {
    triples.value.splice(index, 1);
    showMessage('Triple deleted', 'info');
  }
};

const validateTriple = (triple) => {
  if (!triple.entity1 || !triple.relation || !triple.entity2) {
    showMessage('Entity and relation cannot be empty!', 'error');
    return false;
  }
  return true;
};

const addEntity = () => {
  entities.value.push({
    name: '',
    type: 'Herb',
    attributes: {}
  });
};

const deleteEntity = (index) => {
  if (confirm('Are you sure you want to delete this entity?')) {
    entities.value.splice(index, 1);
    showMessage('Entity deleted', 'info');
  }
};

const addAttribute = (entity) => {
  if (!entity.attributes) {
    entity.attributes = {};
  }
  const tempKey = `attribute_${Date.now()}`;
  entity.attributes[tempKey] = '';
};

const removeAttribute = (entity, key) => {
  delete entity.attributes[key];
  entity.attributes = { ...entity.attributes };
};

const updateAttributeKey = (entity, oldKey, newKey) => {
  if (oldKey === newKey || !newKey.trim()) return;
  const value = entity.attributes[oldKey];
  delete entity.attributes[oldKey];
  entity.attributes[newKey] = value;
  entity.attributes = { ...entity.attributes };
};

const updateAttributeValue = (entity, key, newValue) => {
  entity.attributes[key] = newValue;
  entity.attributes = { ...entity.attributes };
};

const saveData = async () => {
  for (let triple of triples.value) {
    if (!validateTriple(triple)) return;
  }
  for (let entity of entities.value) {
    if (!entity.name || !entity.type) {
      showMessage('Entity name and type cannot be empty!', 'error');
      return;
    }
  }

  isSaving.value = true;
  showMessage('Saving to Neo4j database...', 'info');

  try {
    const response = await axios.post(`${API_BASE}/save`, {
      document_name: fileName.value,
      triples: triples.value,
      entities: entities.value
    });

    isSaving.value = false;
    if (response.data.code === 200) {
      showMessage('Data successfully saved to Neo4j!', 'success');
      setTimeout(() => {
        backToUpload();
      }, 1500);
    } else {
      showMessage(`Save failed: ${response.data.msg}`, 'error');
    }
  } catch (error) {
    isSaving.value = false;
    const errorMsg = error.response?.data?.msg || error.message || 'Network error';
    showMessage(`Save failed: ${errorMsg}`, 'error');
    console.error('Save error:', error);
  }
};

const backToUpload = () => {
  currentStep.value = 1;
  selectedFile.value = null;
  triples.value = [];
  entities.value = [];
  showMessage('Returned to upload page', 'info');
  document.getElementById('file').value = '';
};
</script>

<style scoped>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Microsoft Yahei", "PingFang SC", sans-serif;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  margin: 20px 0;
  color: #2c3e50;
}

/* 卡片样式 */
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  padding: 30px;
  margin-bottom: 20px;
  display: none;
}

.card.active {
  display: block;
}

.card-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #2c3e50;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

.form-control {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

/* 按钮样式 */
.btn {
  padding: 12px 24px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
  margin-right: 10px;
}

.btn:hover {
  background: #2980b9;
}

.btn-success {
  background: #2ecc71;
}

.btn-success:hover {
  background: #27ae60;
}

.btn-danger {
  background: #e74c3c;
}

.btn-danger:hover {
  background: #c0392b;
}

.btn-outline {
  background: transparent;
  border: 1px solid #3498db;
  color: #3498db;
}

.btn-outline:hover {
  background: #f5f9ff;
}

.btn:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

/* 提示消息 */
.alert {
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-size: 14px;
}

.alert-success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.alert-error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.alert-info {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

/* 三元组编辑项 */
.triple-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 12px;
  background: #f9f9f9;
  flex-wrap: wrap;
}

.triple-item .form-control {
  flex: 1;
  margin: 0 8px;
  min-width: 150px;
}

.triple-relation {
  font-weight: 600;
  color: #3498db;
  margin: 0 8px;
}

.triple-source {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  max-height: 80px;
  overflow-y: auto;
  width: 100%;
}

/* 实体属性编辑 */
.tab-header {
  display: flex;
  border-bottom: 1px solid #ddd;
  margin-bottom: 20px;
}
.tab-item {
  padding: 10px 20px;
  cursor: pointer;
  font-weight: 500;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab-item.active {
  color: #3498db;
  border-bottom-color: #3498db;
}
.entity-item {
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 16px;
  background: #fafafa;
}
.entity-header {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  align-items: center;
}
.entity-name {
  flex: 2;
}
.entity-type {
  flex: 1;
}
.attributes-container {
  margin-left: 20px;
  padding-left: 10px;
  border-left: 2px solid #3498db;
}
.attribute-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}
.attr-key {
  flex: 1;
}
.attr-value {
  flex: 2;
}
/* 无数据提示 */
.empty-tip {
  text-align: center;
  padding: 20px;
  color: #999;
}
/* 加载动画 */
.loading {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255,255,255,.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s ease-in-out infinite;
  margin-right: 8px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
/* 响应式适配 */
@media (max-width: 768px) {
  .triple-item {
    flex-direction: column;
    align-items: stretch;
  }
  .triple-item .form-control {
    margin: 8px 0;
  }
  .triple-relation {
    margin: 8px 0;
    text-align: center;
  }
  .entity-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>