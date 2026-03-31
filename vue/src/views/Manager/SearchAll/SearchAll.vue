<template>
  <div class="node-filter-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">Graph Joint Filtering and Return Types</span>
          <div class="actions">
            <el-button size="small" @click="$router.back()">Back</el-button>
          </div>
        </div>
      </template>

      <!-- Description -->
      <el-alert
          title="Select filter conditions (prerequisites): check node types to use as filters and optionally fill in names; then select return node types. Click search to let the backend jointly evaluate filters and return types."
          type="info"
          :closable="false"
          class="mb-3"
      />

      <!-- Filter conditions (prerequisites) -->
      <el-divider content-position="left">Filter Conditions (Prerequisites)</el-divider>
      <el-form :model="form" label-width="120px" class="filter-form">
        <el-form-item label="Types as conditions">
          <el-checkbox-group v-model="form.selectedTypes">
            <el-checkbox v-for="t in TYPES" :key="t.key" :label="t.key">{{ t.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-row :gutter="16">
          <el-col v-for="t in TYPES" :key="t.key" :lg="12" :md="12" :sm="24" :xs="24">
            <el-form-item :label="t.label + ' Name'">
              <el-input
                  v-model="form.names[t.key]"
                  :placeholder="t.placeholder"
                  :disabled="!form.selectedTypes.includes(t.key)"
                  clearable
                  @keyup.enter.native="onSearch"
              >
                <template #prepend>
                  <el-tag :type="form.selectedTypes.includes(t.key) ? 'success' : 'info'" effect="plain">
                    {{ form.selectedTypes.includes(t.key) ? 'Condition' : 'Not selected' }}
                  </el-tag>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- Return node types and limits -->
<el-divider content-position="left">Return Node Types and Limits</el-divider>
<el-form label-width="120px" class="return-form">
  <el-form-item label="Return types needed">
    <el-checkbox-group v-model="form.returnTypes">
      <el-checkbox v-for="t in TYPES" :key="t.key" :label="t.key">{{ t.label }}</el-checkbox>
    </el-checkbox-group>
  </el-form-item>

  <el-row :gutter="20">
    <el-col :span="12">
      <el-form-item label="Max per page">
        <el-input-number v-model="form.limitPerType" :min="0" :max="1000" />
        <span style="margin-left:8px;color:#999;">0 = unlimited (not recommended without filters)</span>
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="Max hops">
        <el-input-number v-model="form.maxHops" :min="1" :max="5" />
      </el-form-item>
    </el-col>
  </el-row>
</el-form>

      <div class="form-actions">
        <el-button type="primary" :loading="loading" @click="onSearch">Search</el-button>
        <el-button :disabled="loading" @click="onReset">Reset</el-button>
      </div>

      <el-divider />

      <!-- Results area -->
      <div v-loading="loading" element-loading-text="Searching...">
        <template v-if="hasAnyResult || validReturnTypes.length">
          <el-alert
              type="success"
              :closable="false"
              class="mb-3"
              :title="`Condition types: ${selectedTypeLabels}  |  Return types: ${selectedReturnLabels}  |  Total: ${totalCount} results`"
          />

          <el-tabs v-model="activeTab" type="card">
            <el-tab-pane
                v-for="t in tabTypes"
                :key="t.key"
                :label="`${t.label} (${(results[t.key] || []).length})`"
                :name="t.key"
            >
              <template v-if="(results[t.key] || []).length">
                <el-table :data="pagedResults(t.key)" border style="width: 100%">
                  <!-- HUBU ID column (fallback to id if not present) -->
                  <el-table-column prop="hubu_id" label="HUBU ID" width="180">
                    <template #default="{ row }">
                      {{ row.hubu_id || row.id || '--' }}
                    </template>
                  </el-table-column>

                  <!-- Name column (Herb uses herb_cn_name, others use name) -->
                  <el-table-column prop="name" label="Name" min-width="220">
                    <template #default="{ row }">
                      {{ displayName(t.key, row) || '--' }}
                    </template>
                  </el-table-column>

                  <!-- Extra column (pick a common field per type; show only if field exists) -->
                  <el-table-column
                      v-if="extraKeyForType(t.key) && hasField(pagedResults(t.key), extraKeyForType(t.key))"
                      :label="extraLabelForType(t.key)"
                      :prop="extraKeyForType(t.key)"
                      min-width="200"
                      show-overflow-tooltip
                  >
                    <template #default="{ row }">
                      {{ row[extraKeyForType(t.key)] }}
                    </template>
                  </el-table-column>

                  <!-- More info popover -->
                  <el-table-column label="More Info" width="120">
                    <template #default="{ row }">
                      <el-popover placement="top" trigger="click" width="420">
                        <pre class="json-pre">{{ pretty(row) }}</pre>
                        <template #reference>
                          <el-button link type="primary">View</el-button>
                        </template>
                      </el-popover>
                    </template>
                  </el-table-column>
                </el-table>

                <div class="pagination-wrapper">
                  <el-pagination
                      background
                      layout="total, sizes, prev, pager, next, jumper"
                      :total="(results[t.key] || []).length"
                      :page-sizes="[10, 20, 50]"
                      :page-size="pageState[t.key]?.size || 20"
                      :current-page="pageState[t.key]?.page || 1"
                      @size-change="val => setPageSize(t.key, val)"
                      @current-change="val => setPage(t.key, val)"
                  />
                </div>
              </template>
              <el-empty v-else description="No matching results for this type" />
            </el-tab-pane>
          </el-tabs>
        </template>
        <el-empty v-else description="Please select filter conditions and return types before searching" />
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

const TYPES = [
  { key: 'Herb', label: 'Herb', placeholder: 'e.g., Huangqi / Astragalus' },
  { key: 'Protein', label: 'Protein', placeholder: 'e.g., UNIPROT ID / Q15596' },
  { key: 'Ingredients', label: 'Ingredients', placeholder: 'e.g., Quercetin / Quercetin' },
  { key: 'Disease', label: 'Disease', placeholder: 'e.g., Diabetes / Diabetes' },
  { key: 'Pathway', label: 'Pathway', placeholder: 'e.g., NF-kappa B signaling' },
  { key: 'MedicineLevel1Category', label: 'Level 1 Category', placeholder: 'e.g., Tonifying deficiency drugs' },
  { key: 'MedicineLevel2Category', label: 'Level 2 Category', placeholder: 'e.g., Qi-tonifying drugs' }
]
const TYPE_SET = new Set(TYPES.map(t => t.key))

export default {
  name: 'NodeFilterSearch',
  data() {
    return {
      TYPES,
      form: {
        // prerequisites
        selectedTypes: [],
        names: TYPES.reduce((acc, t) => { acc[t.key] = ''; return acc }, {}),
        // return types and limits
        returnTypes: [],
        limitPerType: 50,
        maxHops: 3
      },
      loading: false,
      // results (per type)
      results: TYPES.reduce((acc, t) => { acc[t.key] = []; return acc }, {}),
      // pagination state (per type)
      pageState: TYPES.reduce((acc, t) => { acc[t.key] = { page: 1, size: 20 }; return acc }, {}),
      activeTab: TYPES[0].key
    }
  },
  computed: {
    hasAnyResult() {
      return Object.values(this.results).some(list => (list || []).length > 0)
    },
    totalCount() {
      return Object.values(this.results).reduce((sum, list) => sum + (list ? list.length : 0), 0)
    },
    selectedTypeLabels() {
      if (!this.form.selectedTypes.length) return 'None selected'
      const map = Object.fromEntries(this.TYPES.map(t => [t.key, t.label]))
      return this.form.selectedTypes.map(k => map[k]).join(', ')
    },
    validReturnTypes() {
      return (this.form.returnTypes || []).filter(k => TYPE_SET.has(k))
    },
    selectedReturnLabels() {
      if (!this.validReturnTypes.length) return 'None selected'
      const map = Object.fromEntries(this.TYPES.map(t => [t.key, t.label]))
      return this.validReturnTypes.map(k => map[k]).join(', ')
    },
    tabTypes() {
      if (this.validReturnTypes.length) {
        const set = new Set(this.validReturnTypes)
        return this.TYPES.filter(t => set.has(t.key))
      }
      return this.TYPES.filter(t => (this.results[t.key] || []).length)
    }
  },
  methods: {
    pretty(obj) { return JSON.stringify(obj, null, 2) },
    setPageSize(type, size) {
      this.pageState[type].size = size
      this.pageState[type].page = 1
    },
    setPage(type, page) { this.pageState[type].page = page },
    pagedResults(type) {
      const list = this.results[type] || []
      const { page, size } = this.pageState[type]
      const start = (page - 1) * size
      return list.slice(start, start + size)
    },
    displayName(type, row) {
      if (!row) return ''
      if (type === 'Herb') return row.herb_cn_name || row.name || ''
      return row.name || row.id || row.hubu_id || ''
    },
    // Extra column per type (adjust as needed)
    extraKeyForType(type) {
      const map = {
        Herb: 'herb_latin_name',
        Ingredients: 'Formula',
        Protein: 'uniprot_id',
        Disease: 'description',
        Pathway: 'pathway_name'
      }
      return map[type] || null
    },
    extraLabelForType(type) {
      const map = {
        Herb: 'Latin Name',
        Ingredients: 'Formula',
        Protein: 'UniProt',
        Disease: 'Description',
        Pathway: 'Pathway Name'
      }
      return map[type] || 'Extra'
    },
    hasField(rows, key) {
      if (!key) return false
      return (rows || []).some(r => r && r[key] != null && String(r[key]).length > 0)
    },
    onReset() {
      this.form.selectedTypes = []
      this.form.returnTypes = []
      this.form.limitPerType = 50
      this.form.maxHops = 3
      Object.keys(this.form.names).forEach(k => (this.form.names[k] = ''))
      Object.keys(this.results).forEach(k => (this.results[k] = []))
      Object.keys(this.pageState).forEach(k => (this.pageState[k] = { page: 1, size: 20 }))
      this.activeTab = this.TYPES[0].key
    },
    async onSearch() {
      if (!this.form.selectedTypes.length) {
        this.$message.warning('Please select at least one type in "Filter Conditions"')
        return
      }
      if (!this.validReturnTypes.length) {
        this.$message.warning('Please select at least one "Return Type"')
        return
      }

      const payload = {
        filters: this.form.selectedTypes.map(type => ({
          type,
          name: (this.form.names[type] || '').trim()
        })),
        returnTypes: this.validReturnTypes,
        limitPerType: this.form.limitPerType,
        maxHops: this.form.maxHops
      }

      this.loading = true
      try {
        const res = await request.post(
            '/api/graph/all/search',
            payload,
            { headers: { 'Content-Type': 'application/json; charset=UTF-8' } }
        )
        if (res.code === '200') {
          const data = res.data || {}
          this.TYPES.forEach(t => {
            this.results[t.key] = Array.isArray(data[t.key]) ? data[t.key] : []
            this.pageState[t.key].page = 1
          })
          const first = this.tabTypes[0]
          if (first) this.activeTab = first.key
          this.$message.success('Search completed')
        } else {
          this.$message.error(res.msg || 'Search failed')
        }
      } catch (e) {
        console.error(e)
        this.$message.error('Request failed')
      } finally {
        this.loading = false
      }
    },
    goDetail(type, id) {
      const map = {
        Herb: id => `/herb/detail/${id}`,
        Protein: id => `/protein/detail/${id}`,
        Ingredients: id => `/ingredient/detail/${id}`,
        Disease: id => `/disease/detail/${id}`,
        Pathway: id => `/pathway/detail/${id}`,
        MedicineLevel1Category: id => `/medicine/level1/${id}`,
        MedicineLevel2Category: id => `/medicine/level2/${id}`
      }
      const to = map[type] ? map[type](id) : ''
      if (to) this.$router.push(to)
    }
  }
}
</script>

<style scoped>
.node-filter-page { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 20px; font-weight: 700; color: #166d88; }
.mb-3 { margin-bottom: 16px; }
.filter-form, .return-form { margin-bottom: 12px; }
.form-actions { margin: 12px 0 4px; }
.pagination-wrapper { text-align: center; margin-top: 12px; }
.json-pre { margin: 0; max-height: 300px; overflow: auto; font-size: 12px; background: #f8f8f8; padding: 8px; border-radius: 6px; }
</style>